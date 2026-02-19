package cn.craftime.popboo.compat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class SchedulerFacade {
    private final JavaPlugin plugin;
    private final BukkitScheduler bukkitScheduler;

    private final Method getGlobalRegionScheduler;
    private final Method getRegionScheduler;

    public SchedulerFacade(JavaPlugin plugin) {
        this.plugin = plugin;
        this.bukkitScheduler = Bukkit.getScheduler();

        Method global = null;
        Method region = null;
        try {
            global = Bukkit.class.getMethod("getGlobalRegionScheduler");
        } catch (NoSuchMethodException ignored) {
        }
        try {
            region = Bukkit.class.getMethod("getRegionScheduler");
        } catch (NoSuchMethodException ignored) {
        }
        this.getGlobalRegionScheduler = global;
        this.getRegionScheduler = region;
    }

    public ScheduledHandle runLater(Runnable runnable, long delayTicks) {
        if (getGlobalRegionScheduler != null) {
            ScheduledHandle handle = tryRunGlobalDelayed(runnable, delayTicks);
            if (handle != null) return handle;
        }
        BukkitTask task = bukkitScheduler.runTaskLater(plugin, runnable, Math.max(0L, delayTicks));
        return task::cancel;
    }

    public ScheduledHandle runLater(Location location, Runnable runnable, long delayTicks) {
        if (getRegionScheduler != null) {
            ScheduledHandle handle = tryRunRegionDelayed(location, runnable, delayTicks);
            if (handle != null) return handle;
        }
        BukkitTask task = bukkitScheduler.runTaskLater(plugin, runnable, Math.max(0L, delayTicks));
        return task::cancel;
    }

    private ScheduledHandle tryRunGlobalDelayed(Runnable runnable, long delayTicks) {
        try {
            Object scheduler = getGlobalRegionScheduler.invoke(null);
            Method runDelayed = findMethod(
                    scheduler.getClass(),
                    "runDelayed",
                    Plugin.class,
                    Consumer.class,
                    long.class
            );
            Object scheduledTask = runDelayed.invoke(
                    scheduler,
                    plugin,
                    (Consumer<Object>) task -> runnable.run(),
                    Math.max(0L, delayTicks)
            );
            Method cancel = scheduledTask.getClass().getMethod("cancel");
            return () -> {
                try {
                    cancel.invoke(scheduledTask);
                } catch (Exception ignored) {
                }
            };
        } catch (Exception ignored) {
            return null;
        }
    }

    private ScheduledHandle tryRunRegionDelayed(Location location, Runnable runnable, long delayTicks) {
        if (location == null) return null;
        try {
            Object scheduler = getRegionScheduler.invoke(null);
            Method runDelayed = findMethod(
                    scheduler.getClass(),
                    "runDelayed",
                    Plugin.class,
                    Location.class,
                    Consumer.class,
                    long.class
            );
            Object scheduledTask = runDelayed.invoke(
                    scheduler,
                    plugin,
                    location,
                    (Consumer<Object>) task -> runnable.run(),
                    Math.max(0L, delayTicks)
            );
            Method cancel = scheduledTask.getClass().getMethod("cancel");
            return () -> {
                try {
                    cancel.invoke(scheduledTask);
                } catch (Exception ignored) {
                }
            };
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        try {
            return type.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            if (parameterTypes.length > 0 && parameterTypes[parameterTypes.length - 1] == long.class) {
                Class<?>[] alt = parameterTypes.clone();
                alt[alt.length - 1] = int.class;
                return type.getMethod(name, alt);
            }
            throw e;
        }
    }
}
