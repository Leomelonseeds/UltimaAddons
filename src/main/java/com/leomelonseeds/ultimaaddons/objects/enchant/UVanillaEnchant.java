package com.leomelonseeds.ultimaaddons.objects.enchant;

import java.util.Objects;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

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
    public ItemStack applyEnchant(ItemStack item, int level) {
        if (item.getType() == Material.BOOK) {
            ItemStack ebook = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta emeta = (EnchantmentStorageMeta) ebook.getItemMeta();
            emeta.addStoredEnchant(ench, level, true);
            ebook.setItemMeta(emeta);
            return ebook;
        }
        
        item.addUnsafeEnchantment(ench, level);
        return item;
    }

    @Override
    public String getDisplayName(int level) {
        String key = ench.getKey().value();
        key = WordUtils.capitalizeFully(key.replace('_', ' '));
        return "&f" + key + " " + roman(level);
    }
    
    @Override
    public boolean isCompatible(ItemStack item) {
        return ench.canEnchantItem(item) || item.getType() == Material.BOOK;
    }

    @Override
    public void getInfo(Player player) {
        String key = ench.getKey().value();
        String link = "https://minecraft.wiki/w/" + key;
        player.sendMessage(Utils.toComponent("&6Check the official Minecraft Wiki: &e&n" + link));
    }

    @Override
    public int getLevel(ItemStack item) {
        return item.getEnchantmentLevel(ench);
    }

    @Override
    public void removeEnchant(ItemStack item) {
        item.removeEnchantment(ench);
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

    @Override
    public int hashCode() {
        return Objects.hash(ench);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UVanillaEnchant other = (UVanillaEnchant) obj;
        return Objects.equals(ench, other.ench);
    }
}
