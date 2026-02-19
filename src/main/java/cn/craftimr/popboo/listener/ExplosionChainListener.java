package cn.craftimr.popboo.listener;

import cn.craftimr.popboo.service.PopbooService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionChainListener implements Listener {
    private final PopbooService service;

    public ExplosionChainListener(PopbooService service) {
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        service.triggerChain(event.getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        service.triggerChain(event.getBlock().getLocation());
    }
}

