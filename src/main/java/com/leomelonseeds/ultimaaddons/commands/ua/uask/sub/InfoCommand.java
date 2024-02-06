package com.leomelonseeds.ultimaaddons.commands.ua.uask.sub;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;

public class InfoCommand extends Command {
    public InfoCommand(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        super(name, aliases, permission, description, arguments);
    }

    @Override
    public boolean hasInvalidArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        // Check for valid number of arguments
        if (args.length < 1) {
            CommandUtils.sendErrorMsg(sender, "Usage: /uask rotate [shopkeeper id]");
            return true;
        }
        if (super.hasInvalidArgs(sender, args)) return true;
        int id = NumberUtils.toInt(args[0]);
        if (!this.plugin.getSKLinker().hasShopkeeper(id)) {
            CommandUtils.sendErrorMsg(sender, "Not a RSK (no link)");
            return true;
        }
        return false;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        // Safe to parse since we already checked for valid integers & link
        int child = NumberUtils.toInt(args[0]);
        CommandUtils.sendMsg(sender, "Parent ID: " + this.plugin.getSKLinker().getRotatingShopkeeper(child).getParentID());
    }
}
