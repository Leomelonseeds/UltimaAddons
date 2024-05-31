package com.leomelonseeds.ultimaaddons.objects.enchant;

import org.apache.commons.lang.WordUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimaaddons.utils.Utils;

public class UVanillaEnchant implements UEnchantment {
    
    private Enchantment ench;
    
    public UVanillaEnchant(Enchantment ench) {
        this.ench = ench;
    }

    @Override
    public int getMaxLevel() {
        return ench.getMaxLevel();
    }

    @Override
    public void applyEnchant(ItemStack item, int level) {
        item.addUnsafeEnchantment(ench, level);
    }

    @Override
    public String getDisplayName(int level) {
        String key = ench.getKey().value();
        key = WordUtils.capitalizeFully(key.replace('_', ' '));
        return "&f" + key + " " + roman(level);
    }
    
    @Override
    public boolean isCompatible(ItemStack item) {
        return ench.canEnchantItem(item);
    }

    @Override
    public void getInfo(Player player) {
        String key = ench.getKey().value();
        String link = "https://minecraft.wiki/w/" + key;
        player.sendMessage(Utils.toComponent("&6Check the official Minecraft Wiki: &e&n" + link));
    }
    
    private String roman(int i) {
        switch (i) {
        case 0:
            return "";
        case 1:
            return "I";
        case 2:
            return "II";
        case 3:
            return "III";
        case 4:
            return "IV";
        case 5:
            return "V";
        }
        return null;
    }
}
