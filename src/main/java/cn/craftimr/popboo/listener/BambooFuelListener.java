package cn.craftimr.popboo.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;

import cn.craftimr.popboo.service.PopbooService;

public class BambooFuelListener implements Listener {
    private final PopbooService service;

    public BambooFuelListener(PopbooService service) {
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        ItemStack fuel = event.getFuel();
        Material type = fuel == null ? null : fuel.getType();
        service.debugFurnaceBurnEvent(event.getBlock(), type);
        service.tryArmFurnace(event.getBlock(), type);
    }
}
