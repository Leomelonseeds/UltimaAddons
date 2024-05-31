package com.leomelonseeds.ultimaaddons.objects.enchant;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.api.AEAPI;

public class UCustomEnchant implements UEnchantment {
    
    private String ench;
    
    public UCustomEnchant(String ench) {
        this.ench = ench;
    }

    @Override
    public int getMaxLevel() {
        return AEAPI.getHighestEnchantmentLevel(ench);
    }

    @Override
    public void applyEnchant(ItemStack item, int level) {
        AEAPI.applyEnchant(ench, level, item);
    }

    @Override
    public String getDisplayName(int level) {
        ItemStack book = AEAPI.createEnchantmentBook(ench, level, 100, 0, null);
        return Utils.toPlain(book.getItemMeta().displayName());
    }
    
    @Override
    public boolean isCompatible(ItemStack item) {
        return AEAPI.isApplicable(item.getType(), ench);
    }

    @Override
    public void getInfo(Player player) {
        player.performCommand("ae info " + ench);
    }

}
