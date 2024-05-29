package com.leomelonseeds.ultimaaddons.objects;

import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.bukkit.enchantments.Enchantment;

import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.kyori.adventure.text.Component;

public class EnchantResult {

    private static Map<String, String> colors = Map.of(
        "common", "&f",
        "uncommon", "&a",
        "rare", "&9",
        "epic", "&5",
        "legendary", "&6"
    );
    
    private String name;
    private int level;
    private Enchantment vanillaEnchant;
    private int cost;
    private String color;
    
    public EnchantResult(String name, int level, Enchantment vanillaEnchant, int cost, String rarity) {
        this.name = name;
        this.level = level;
        this.vanillaEnchant = vanillaEnchant;
        this.cost = cost;
        this.color = colors.get(rarity);
    }

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

    public Component getDisplayName() {
        String res = WordUtils.capitalizeFully(name.replace(' ', ' '));
        res = color + res + " " + roman(level);
        return Utils.toComponent(res);
    }
    
    /**
     * Translate integer to roman, only for first 10 numbers lol
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
        case 6:
            return "VI";
        case 7:
            return "VII";
        case 8:
            return "VIII";
        case 9:
            return "IX";
        case 10:
            return "X";
        }
        return null;
    }
}
