package com.leomelonseeds.ultimaaddons.objects;

import org.apache.commons.lang.WordUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.api.AEAPI;

public class EnchantResult {
    
    private String name;
    private int level;
    private Enchantment vanillaEnchant;
    private int cost;
    
    public EnchantResult(String name, int level, Enchantment vanillaEnchant, int cost, String rarity) {
        this.name = name;
        this.level = level;
        this.vanillaEnchant = vanillaEnchant;
        this.cost = cost;
    }

    /**
     * Use for custom enchants only
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }
    
    public boolean isVanilla() {
        return vanillaEnchant != null;
    }

    public Enchantment getVanillaEnchant() {
        return vanillaEnchant;
    }

    public int getCost() {
        return cost;
    }

    /**
     * Gets the display name, ready to use for the name of an item
     * 
     * @return
     */
    public String getDisplayName() {
        if (isVanilla()) {
            String key = vanillaEnchant.getKey().value();
            key = WordUtils.capitalizeFully(key.replace('_', ' '));
            return "&f" + key + " " + roman(level);
        } else {
            ItemStack book = AEAPI.createEnchantmentBook(name, level, 100, 0, null);
            return Utils.toPlain(book.getItemMeta().displayName());
        }
    }
    
    /**
     * Translate integer to roman, only for first 5 numbers lol
     * 
     * @param i
     * @return
     */
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
