package com.leomelonseeds.ultimaaddons.commands.ua;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UAReload extends Command {
    public UAReload(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        super(name, aliases, permission, description, arguments);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        if (hasInvalidArgs(sender, args))
            return;

        this.plugin.reload();
        CommandUtils.sendSuccessMsg(sender, "UltimaAddons reloaded");
    }
}
