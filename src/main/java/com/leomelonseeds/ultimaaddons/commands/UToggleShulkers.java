package com.leomelonseeds.ultimaaddons.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;

@CommandAlias("utoggleshulkers")
public class UToggleShulkers extends BaseCommand {
    
    private static String setting = "enable-shulkers";
    
    @Default
    @CommandPermission("ua.toggleshulkers")
    public void onToggle(CommandSender sender) {
        FileConfiguration config = UltimaAddons.getPlugin().getConfig();
        if (config.getBoolean(setting)) {
            config.set(setting, false);
            CommandUtils.sendSuccessMsg(sender, "Right-click shulkers are now disabled");
        } else {
            config.set(setting, true);
            CommandUtils.sendSuccessMsg(sender, "Right-click shulkers have been re-enabled");
        }
        UltimaAddons.getPlugin().saveConfig();
    }

}
