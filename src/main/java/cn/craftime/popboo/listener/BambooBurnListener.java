package cn.craftime.popboo.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;

import cn.craftime.popboo.service.PopbooService;

public class BambooBurnListener implements Listener {
    private final PopbooService service;
    private static final BlockFace[] ADJACENT = new BlockFace[]{
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST
    };

    public BambooBurnListener(PopbooService service) {
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        service.tryArmBurningBlock(event.getBlock());
        armAdjacent(event.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent event) {
        Material type = event.getNewState().getType();
        if (!isFire(type)) return;
        service.tryArmBurningBlock(event.getBlock());
        armAdjacent(event.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        service.handleBlockBurn(event.getBlock());
    }

    private void armAdjacent(Block fireBlock) {
        if (fireBlock == null) return;
        for (BlockFace face : ADJACENT) {
            Block near = fireBlock.getRelative(face);
            service.tryArmBurningBlock(near);
        }
    }

    private boolean isFire(Material material) {
        if (material == null) return false;
        String name = material.name();
        return "FIRE".equals(name) || name.endsWith("_FIRE");
    }
}
