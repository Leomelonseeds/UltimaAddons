package com.leomelonseeds.ultimaaddons.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.leomelonseeds.ultimaaddons.invs.Cindersmith;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;

@CommandAlias("uenchant")
public class UAEnchant extends BaseCommand {
    
    @Default
    @CommandPermission("ua.enchant")
    @CommandCompletion("@players")
    @Description("Open the cindersmith GUI for a player")
    @Syntax("<player>")
    public void onUEnchant(CommandSender sender, @Flags("other") Player target) {
        new Cindersmith(target);
    }

}
