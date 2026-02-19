package cn.craftime.popboo.config;

import org.bukkit.configuration.file.FileConfiguration;

public class PopbooConfig {
    private final boolean enabled;
    private final double chanceBlockIgnite;
    private final boolean blockBurnFallbackEnabled;
    private final double chanceBlockBurnFallback;
    private final double chanceFurnaceFuel;
    private final int delayMinTicks;
    private final int delayMaxTicks;
    private final float explosionPower;
    private final boolean explosionCreateFire;
    private final boolean explosionBreakBlocks;
    private final boolean chainEnabled;
    private final int chainRadius;
    private final int chainMaxCount;
    private final int furnaceCooldownTicks;
    private final boolean debug;

    private PopbooConfig(
            boolean enabled,
            double chanceBlockIgnite,
            boolean blockBurnFallbackEnabled,
            double chanceBlockBurnFallback,
            double chanceFurnaceFuel,
            int delayMinTicks,
            int delayMaxTicks,
            float explosionPower,
            boolean explosionCreateFire,
            boolean explosionBreakBlocks,
            boolean chainEnabled,
            int chainRadius,
            int chainMaxCount,
            int furnaceCooldownTicks,
            boolean debug
    ) {
        this.enabled = enabled;
        this.chanceBlockIgnite = chanceBlockIgnite;
        this.blockBurnFallbackEnabled = blockBurnFallbackEnabled;
        this.chanceBlockBurnFallback = chanceBlockBurnFallback;
        this.chanceFurnaceFuel = chanceFurnaceFuel;
        this.delayMinTicks = delayMinTicks;
        this.delayMaxTicks = delayMaxTicks;
        this.explosionPower = explosionPower;
        this.explosionCreateFire = explosionCreateFire;
        this.explosionBreakBlocks = explosionBreakBlocks;
        this.chainEnabled = chainEnabled;
        this.chainRadius = chainRadius;
        this.chainMaxCount = chainMaxCount;
        this.furnaceCooldownTicks = furnaceCooldownTicks;
        this.debug = debug;
    }

    public static PopbooConfig from(FileConfiguration config) {
        boolean enabled = config.getBoolean("enabled", true);
        double chanceBlockIgnite = clamp01(config.getDouble("chance.blockIgnite", 0.6));
        boolean blockBurnFallbackEnabled = config.getBoolean("blockBurnFallback.enabled", false);
        double chanceBlockBurnFallback = clamp01(config.getDouble("blockBurnFallback.chance", 0.2));
        double chanceFurnaceFuel = clamp01(config.getDouble("chance.furnaceFuel", 0.3));

        int delayMin = Math.max(0, config.getInt("delayTicks.min", 10));
        int delayMax = Math.max(delayMin, config.getInt("delayTicks.max", 60));

        float power = (float) Math.max(0.0, config.getDouble("explosion.power", 4.5));
        boolean createFire = config.getBoolean("explosion.createFire", false);
        boolean breakBlocks = config.getBoolean("explosion.breakBlocks", true);
        boolean chainEnabled = config.getBoolean("explosion.chainEnabled", true);
        int chainRadius = Math.max(0, config.getInt("explosion.chainRadius", 6));
        int chainMaxCount = Math.max(0, config.getInt("explosion.chainMaxCount", 20));

        int furnaceCooldownTicks = Math.max(0, config.getInt("furnace.cooldownTicks", 40));
        boolean debug = config.getBoolean("debug", false);

        return new PopbooConfig(
                enabled,
                chanceBlockIgnite,
                blockBurnFallbackEnabled,
                chanceBlockBurnFallback,
                chanceFurnaceFuel,
                delayMin,
                delayMax,
                power,
                createFire,
                breakBlocks,
                chainEnabled,
                chainRadius,
                chainMaxCount,
                furnaceCooldownTicks,
                debug
        );
    }

    private static double clamp01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }

    public boolean enabled() {
        return enabled;
    }

    public double chanceBlockIgnite() {
        return chanceBlockIgnite;
    }

    public boolean blockBurnFallbackEnabled() {
        return blockBurnFallbackEnabled;
    }

    public double chanceBlockBurnFallback() {
        return chanceBlockBurnFallback;
    }

    public double chanceFurnaceFuel() {
        return chanceFurnaceFuel;
    }

    public int delayMinTicks() {
        return delayMinTicks;
    }

    public int delayMaxTicks() {
        return delayMaxTicks;
    }

    public float explosionPower() {
        return explosionPower;
    }

    public boolean explosionCreateFire() {
        return explosionCreateFire;
    }

    public boolean explosionBreakBlocks() {
        return explosionBreakBlocks;
    }

    public boolean chainEnabled() {
        return chainEnabled;
    }

    public int chainRadius() {
        return chainRadius;
    }

    public int chainMaxCount() {
        return chainMaxCount;
    }

    public int furnaceCooldownTicks() {
        return furnaceCooldownTicks;
    }

    public boolean debug() {
        return debug;
    }
}
