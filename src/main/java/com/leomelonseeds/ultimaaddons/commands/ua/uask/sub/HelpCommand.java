package com.leomelonseeds.ultimaaddons.commands.ua.uask.sub;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HelpCommand extends Command {
    public HelpCommand(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        super(name, aliases, permission, description, arguments);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        if (hasInvalidArgs(sender, args))
            return;

        CommandUtils.sendMsg(sender, "&a---+ RSK +---");
        CommandUtils.sendMsg(sender, "&a/uask debug&7 - Get internal debug info");
        CommandUtils.sendMsg(sender, "&a/uask reload&7 - Reload plugin");
        CommandUtils.sendMsg(sender, "&a/uask sync {child id} {parent id}&7 - Create a RSK");
        CommandUtils.sendMsg(sender, "&a/uask unsync {child id}&7 - Delete a RSK");
        CommandUtils.sendMsg(sender, "&a/uask info {child id}&7 - Get parent id of a RSK");
        CommandUtils.sendMsg(sender, "&a/uask forcerotate {child id}&7 - Rotate trades of a RSK");
        CommandUtils.sendMsg(sender, "&a-------------");
    }
}
