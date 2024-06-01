package com.leomelonseeds.ultimaaddons.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.advancedplugins.ae.api.AEAPI;

@CommandAlias("utest")
public class UATest extends BaseCommand {
    
    @Default
    @CommandPermission("ua.test")
    public void onTest(Player p, boolean b1, boolean b2, String ench) {
        ItemStack item = p.getInventory().getItemInMainHand();
        AEAPI.applyEnchant(ench, 1, b1, b2, item);
    }
}
