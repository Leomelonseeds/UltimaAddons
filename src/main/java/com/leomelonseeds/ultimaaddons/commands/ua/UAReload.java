package com.leomelonseeds.ultimaaddons.commands.ua;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;

public class UAReload extends Command {
    public UAReload(String name, List<String> aliases, String permission, String description) {
        super(name, aliases, permission, description, Collections.emptyList());
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        this.plugin.reload();
        CommandUtils.sendSuccessMsg(sender, "UltimaAddons reloaded");
    }
}
