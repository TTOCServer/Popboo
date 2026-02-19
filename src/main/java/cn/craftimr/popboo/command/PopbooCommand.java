package cn.craftimr.popboo.command;

import cn.craftimr.popboo.Popboo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PopbooCommand implements CommandExecutor {
    private final Popboo plugin;

    public PopbooCommand(Popboo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            plugin.reloadPopbooConfig();
            sender.sendMessage("Popboo 配置已重载。");
            return true;
        }
        sender.sendMessage("用法: /" + label + " reload");
        return true;
    }
}
