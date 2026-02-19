package cn.craftimr.popboo;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import cn.craftimr.popboo.command.PopbooCommand;
import cn.craftimr.popboo.compat.MaterialMatcher;
import cn.craftimr.popboo.compat.SchedulerFacade;
import cn.craftimr.popboo.config.PopbooConfig;
import cn.craftimr.popboo.listener.BambooBurnListener;
import cn.craftimr.popboo.listener.BambooFuelListener;
import cn.craftimr.popboo.listener.ExplosionChainListener;
import cn.craftimr.popboo.service.PopbooService;

public final class Popboo extends JavaPlugin {

    private PopbooConfig popbooConfig;
    private PopbooService service;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadPopbooConfig();

        SchedulerFacade scheduler = new SchedulerFacade(this);
        MaterialMatcher matcher = new MaterialMatcher();
        this.service = new PopbooService(getLogger(), scheduler, matcher, popbooConfig);

        getServer().getPluginManager().registerEvents(new BambooBurnListener(service), this);
        getServer().getPluginManager().registerEvents(new BambooFuelListener(service), this);
        getServer().getPluginManager().registerEvents(new ExplosionChainListener(service), this);

        PluginCommand command = getCommand("popboo");
        if (command != null) {
            command.setExecutor(new PopbooCommand(this));
        }
    }

    @Override
    public void onDisable() {
        if (service != null) {
            service.shutdown();
        }
    }

    public PopbooConfig getPopbooConfig() {
        return popbooConfig;
    }

    public PopbooService getService() {
        return service;
    }

    public void reloadPopbooConfig() {
        reloadConfig();
        this.popbooConfig = PopbooConfig.from(getConfig());
        if (service != null) {
            service.reloadConfig(popbooConfig);
        }
    }
}
