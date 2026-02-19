package cn.craftime.popboo.service;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class BlockKey {
    private final UUID worldId;
    private final int x;
    private final int y;
    private final int z;

    public BlockKey(UUID worldId, int x, int y, int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockKey of(Location location) {
        World world = location.getWorld();
        if (world == null) return null;
        return new BlockKey(world.getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public UUID worldId() {
        return worldId;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockKey)) return false;
        BlockKey that = (BlockKey) o;
        if (x != that.x) return false;
        if (y != that.y) return false;
        if (z != that.z) return false;
        return worldId.equals(that.worldId);
    }

    @Override
    public int hashCode() {
        int result = worldId.hashCode();
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }
}
