package com.leomelonseeds.ultimaaddons.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import org.bukkit.command.CommandSender;

@CommandAlias("ureload")
public class UAReload extends BaseCommand {
    private final UltimaAddons plugin;

    public UAReload(UltimaAddons plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandPermission("ua.reload")
    @Description("Reloads UltimaAddons")
    public void onCommand(CommandSender sender) {
        this.plugin.reload();
        CommandUtils.sendSuccessMsg(sender, "UltimaAddons reloaded");
    }
}
