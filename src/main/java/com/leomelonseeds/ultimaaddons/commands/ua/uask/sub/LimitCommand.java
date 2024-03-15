package com.leomelonseeds.ultimaaddons.commands.ua.uask.sub;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;

public class LimitCommand extends Command {
    public LimitCommand(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        super(name, aliases, permission, description, arguments);
    }

    @Override
    public boolean hasInvalidArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        return false;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        CommandUtils.sendMsg(sender, "WIP.");
    }
}
