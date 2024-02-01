package com.leomelonseeds.ultimaaddons.commands.ua.uask.sub;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnsyncCommand extends Command {
    public UnsyncCommand(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        super(name, aliases, permission, description, arguments);
    }

    @Override
    public boolean hasInvalidArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        // Check for valid number of arguments
        if (args.length < 1) {
            CommandUtils.sendErrorMsg(sender, "Usage: /uask unsync [child shopkeeper id]");
            return true;
        }
        if (super.hasInvalidArgs(sender, args))
            return true;
        // We know it can be parsed
        int child = NumberUtils.toInt(args[0]);
        // Check if child is synced
        if (!this.plugin.getSKLinker().hasShopkeeper(child)) {
            CommandUtils.sendErrorMsg(sender, "Child shopkeeper is not an RSK/linked");
            return true;
        }

        return false;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        if (hasInvalidArgs(sender, args))
            return;

        // Safe to parse since we already checked for valid integers and existence in link manager
        int child = NumberUtils.toInt(args[0]);
        this.plugin.getSKLinker().deleteLink(child);
        this.plugin.getTradesFile().getConfig().set(String.valueOf(child), null);
        this.plugin.getTradesFile().save();
        CommandUtils.sendMsg(sender, "Successfully unsynced shopkeeper");
    }
}
