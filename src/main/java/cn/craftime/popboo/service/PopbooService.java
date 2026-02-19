package cn.craftime.popboo.service;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import cn.craftime.popboo.compat.MaterialMatcher;
import cn.craftime.popboo.compat.ScheduledHandle;
import cn.craftime.popboo.compat.SchedulerFacade;
import cn.craftime.popboo.config.PopbooConfig;

public class PopbooService {
    private final Logger logger;
    private final SchedulerFacade scheduler;
    private final MaterialMatcher materialMatcher;
    private final Random random;

    private volatile PopbooConfig config;
    private final Map<BlockKey, ScheduledHandle> armed = new ConcurrentHashMap<>();
    private final Map<BlockKey, Long> furnaceCooldownUntilMs = new ConcurrentHashMap<>();

    public PopbooService(Logger logger, SchedulerFacade scheduler, MaterialMatcher materialMatcher, PopbooConfig config) {
        this.logger = logger;
        this.scheduler = scheduler;
        this.materialMatcher = materialMatcher;
        this.config = config;
        this.random = new Random();
    }

    public void reloadConfig(PopbooConfig config) {
        this.config = config;
    }

    public void shutdown() {
        for (ScheduledHandle handle : armed.values()) {
            try {
                handle.cancel();
            } catch (Exception ignored) {
            }
        }
        armed.clear();
        furnaceCooldownUntilMs.clear();
    }

    public void tryArmBurningBlock(Block block) {
        PopbooConfig cfg = this.config;
        if (!cfg.enabled()) return;
        if (block == null) return;
        String reason = materialMatcher.bambooReason(block.getType());
        if (reason == null) return;

        BlockKey key = BlockKey.of(block.getLocation());
        if (key == null) return;
        if (armed.containsKey(key)) {
            debug("block_arm_skip already_armed type=" + block.getType().name());
            return;
        }
        if (!roll(cfg.chanceBlockIgnite())) {
            debug("block_arm_skip roll_miss chance=" + cfg.chanceBlockIgnite() + " type=" + block.getType().name() + " reason=" + reason);
            return;
        }

        long delay = randomDelay(cfg.delayMinTicks(), cfg.delayMaxTicks());
        Location location = block.getLocation();
        ScheduledHandle handle = scheduler.runLater(location, () -> explodeIfStillValid(key, location, true), delay);
        ScheduledHandle prev = armed.putIfAbsent(key, handle);
        if (prev != null) {
            handle.cancel();
            debug("block_arm_race_cancel type=" + block.getType().name());
            return;
        }
        debug("block_armed delayTicks=" + delay + " type=" + block.getType().name() + " reason=" + reason);
    }

    public void handleBlockBurn(Block block) {
        PopbooConfig cfg = this.config;
        if (!cfg.enabled()) return;
        if (block == null) return;

        BlockKey key = BlockKey.of(block.getLocation());
        if (key == null) return;
        ScheduledHandle handle = armed.remove(key);
        if (handle != null) {
            try {
                handle.cancel();
            } catch (Exception ignored) {
            }
            debug("block_burn_triggered type=" + block.getType().name());
            scheduler.runLater(block.getLocation(), () -> explodeAt(block.getLocation()), 0L);
            return;
        }

        if (!cfg.blockBurnFallbackEnabled()) return;
        String reason = materialMatcher.bambooReason(block.getType());
        if (reason == null) return;
        if (!roll(cfg.chanceBlockBurnFallback())) {
            debug("block_burn_fallback_skip roll_miss chance=" + cfg.chanceBlockBurnFallback() + " type=" + block.getType().name() + " reason=" + reason);
            return;
        }
        debug("block_burn_fallback_triggered type=" + block.getType().name() + " reason=" + reason);
        scheduler.runLater(block.getLocation(), () -> explodeAt(block.getLocation()), 0L);
    }

    public void disarm(Block block) {
        if (block == null) return;
        BlockKey key = BlockKey.of(block.getLocation());
        if (key == null) return;
        ScheduledHandle handle = armed.remove(key);
        if (handle == null) return;
        try {
            handle.cancel();
        } catch (Exception ignored) {
        }
    }

    public void debugFurnaceBurnEvent(Block furnaceBlock, Material fuelType) {
        if (furnaceBlock == null) return;
        debug("furnace_event blockType=" + furnaceBlock.getType().name() + " fuel=" + (fuelType == null ? "null" : fuelType.name()));
    }

    public void tryArmFurnace(Block furnaceBlock, Material fuelType) {
        PopbooConfig cfg = this.config;
        if (!cfg.enabled()) return;
        if (furnaceBlock == null) return;
        String reason = materialMatcher.bambooReason(fuelType);
        if (reason == null) {
            debug("furnace_arm_skip not_bamboo_fuel fuel=" + (fuelType == null ? "null" : fuelType.name()));
            return;
        }

        BlockKey key = BlockKey.of(furnaceBlock.getLocation());
        if (key == null) return;

        long now = System.currentTimeMillis();
        Long until = furnaceCooldownUntilMs.get(key);
        if (until != null && until > now) {
            debug("furnace_arm_skip cooldown fuel=" + fuelType.name());
            return;
        }

        if (armed.containsKey(key)) {
            debug("furnace_arm_skip already_armed fuel=" + fuelType.name());
            return;
        }
        if (!roll(cfg.chanceFurnaceFuel())) {
            debug("furnace_arm_skip roll_miss chance=" + cfg.chanceFurnaceFuel() + " fuel=" + fuelType.name() + " reason=" + reason);
            return;
        }

        furnaceCooldownUntilMs.put(key, now + (long) cfg.furnaceCooldownTicks() * 50L);

        long delay = randomDelay(cfg.delayMinTicks(), cfg.delayMaxTicks());
        Location location = furnaceBlock.getLocation();
        ScheduledHandle handle = scheduler.runLater(location, () -> explodeIfStillValid(key, location, false), delay);
        ScheduledHandle prev = armed.putIfAbsent(key, handle);
        if (prev != null) {
            handle.cancel();
            debug("furnace_arm_race_cancel fuel=" + fuelType.name());
            return;
        }
        debug("furnace_armed delayTicks=" + delay + " fuel=" + fuelType.name() + " reason=" + reason);
    }

    private void explodeIfStillValid(BlockKey key, Location location, boolean validateBamboo) {
        armed.remove(key);
        if (location == null) return;
        World world = location.getWorld();
        if (world == null) return;
        if (validateBamboo) {
            Block block = world.getBlockAt(location);
            if (!materialMatcher.isBambooBlock(block.getType())) return;
            if (block.getType() == Material.AIR) return;
        }
        explodeAt(location);
    }

    public void triggerChain(Location origin) {
        PopbooConfig cfg = this.config;
        if (!cfg.enabled()) return;
        if (!cfg.chainEnabled()) return;
        if (origin == null) return;
        World originWorld = origin.getWorld();
        if (originWorld == null) return;

        int radius = cfg.chainRadius();
        if (radius <= 0) return;
        int maxCount = cfg.chainMaxCount();
        if (maxCount <= 0) return;

        UUID worldId = originWorld.getUID();
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();
        int r2 = radius * radius;

        int triggered = 0;
        for (Map.Entry<BlockKey, ScheduledHandle> entry : armed.entrySet()) {
            BlockKey key = entry.getKey();
            if (!worldId.equals(key.worldId())) continue;

            int dx = key.x() - ox;
            int dy = key.y() - oy;
            int dz = key.z() - oz;
            if (dx * dx + dy * dy + dz * dz > r2) continue;

            ScheduledHandle handle = armed.remove(key);
            if (handle == null) continue;
            try {
                handle.cancel();
            } catch (Exception ignored) {
            }

            World world = Bukkit.getWorld(key.worldId());
            if (world == null) continue;
            Location location = new Location(world, key.x(), key.y(), key.z());
            scheduler.runLater(location, () -> explodeAt(location), 0L);

            triggered++;
            if (triggered >= maxCount) break;
        }
        if (triggered > 0) {
            debug("chain_triggered count=" + triggered + " radius=" + radius);
        }
    }

    private void explodeAt(Location location) {
        PopbooConfig cfg = this.config;
        World world = location.getWorld();
        if (world == null) return;
        Location center = new Location(
                world,
                location.getBlockX() + 0.5,
                location.getBlockY() + 0.5,
                location.getBlockZ() + 0.5
        );
        world.createExplosion(center, cfg.explosionPower(), cfg.explosionCreateFire(), cfg.explosionBreakBlocks());
    }

    private boolean roll(double chance) {
        if (chance <= 0.0) return false;
        if (chance >= 1.0) return true;
        return random.nextDouble() < chance;
    }

    private long randomDelay(int min, int max) {
        if (max <= min) return Math.max(0, min);
        int span = max - min + 1;
        return Math.max(0, min) + random.nextInt(span);
    }

    private void debug(String message) {
        PopbooConfig cfg = this.config;
        if (cfg == null || !cfg.debug()) return;
        if (logger == null) return;
        logger.info(message);
    }
}
