package com.leomelonseeds.ultimaaddons.objects.enchant;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.api.AEAPI;

public class UCustomEnchant implements UEnchantment {
    
    private String ench;
    private List<String> required;
    private List<String> incompatible;
    
    public UCustomEnchant(String ench, FileConfiguration aeConfig) {
        this.ench = ench;
        // TODO: Load incompatible/required enchants from AE config
    }

    @Override
    public int getMaxLevel() {
        return AEAPI.getHighestEnchantmentLevel(ench);
    }

    @Override
    public void applyEnchant(ItemStack item, int level) {
        // TODO: Remove required enchants
        AEAPI.applyEnchant(ench, level, item);
    }

    @Override
    public String getDisplayName(int level) {
        ItemStack book = AEAPI.createEnchantmentBook(ench, level, 100, 0, null);
        return Utils.toPlain(book.getItemMeta().displayName());
    }
    
    @Override
    public boolean isCompatible(ItemStack item) {
        // TODO: Implement checking for incompatible/required enchants
        return AEAPI.isApplicable(item.getType(), ench);
    }

    @Override
    public void getInfo(Player player) {
        player.performCommand("ae info " + ench);
    }

}
