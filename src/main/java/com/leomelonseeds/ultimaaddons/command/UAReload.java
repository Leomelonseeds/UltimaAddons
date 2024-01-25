package com.leomelonseeds.ultimaaddons.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;

public class UAReload implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!CommandUtils.isAdmin(sender)) {
            return true;
        }
        
        UltimaAddons plugin = UltimaAddons.getPlugin();
        plugin.reloadConfig();
        plugin.getItems().loadItems();
        CommandUtils.sendSuccessMsg(sender, "UltimaAddons config reloaded");
        return true;
    }

}
