package com.leomelonseeds.ultimaaddons.objects.enchant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.api.AEAPI;
import net.advancedplugins.ae.enchanthandler.enchantments.AdvancedEnchantment;

public class UCustomEnchant implements UEnchantment {
    
    private String ench;
    private Map<UEnchantment, Integer> required;       // If item enchantment level < value or is 0, then it is incompatible
    private Map<UEnchantment, Integer> incompatible;   // If item enchantment level >= value, then it is incompatible
    
    /**
     * Very important: Any required enchantments must also be in the removed-enchants
     * section in the AE config!
     * 
     * @param ench
     * @param aeConfig set to null to not load required/incompatible enchants
     */
    public UCustomEnchant(String ench, boolean checkRequirements) {
        this.ench = ench;
        if (!checkRequirements) {
            required = Collections.emptyMap();
            incompatible = Collections.emptyMap();
            return;
        }
        
        AdvancedEnchantment ae = AEAPI.getEnchantmentInstance(ench);
        this.required = compileEnchants(ae.getRequiredEnchants());
        
        Map<String, Integer> aeIncompat = new HashMap<>();
        ae.getBlacklistedEnchants().forEach(s -> aeIncompat.put(s, 1)); // Dummy values, AE does not support incompat levels
        this.incompatible = compileEnchants(aeIncompat);
    }
    
    private Map<UEnchantment, Integer> compileEnchants(Map<String, Integer> list) {
        Map<UEnchantment, Integer> ret = new HashMap<>();
        list.entrySet().forEach(e -> {
            Enchantment ve = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(e.getKey()));
            if (ve != null) {
                ret.put(new UVanillaEnchant(ve), e.getValue());
            } else {
                ret.put(new UCustomEnchant(e.getKey(), false), e.getValue());
            }
        });
        return ret;
    }

    @Override
    public int getMaxLevel() {
        return AEAPI.getHighestEnchantmentLevel(ench);
    }

    @Override
    public ItemStack applyEnchant(ItemStack item, int level) {
        if (item.getType() == Material.BOOK) {
            return AEAPI.createEnchantmentBook(ench, level, 100, 0, null);
        }
        
        for (UEnchantment ue : required.keySet()) {
            ue.removeEnchant(item);
        }
        
        return AEAPI.applyEnchant(ench, level, item);
    }

    @Override
    public String getDisplayName(int level) {
        ItemStack book = AEAPI.createEnchantmentBook(ench, level, 100, 0, null);
        return Utils.toPlain(book.getItemMeta().displayName());
    }
    
    @Override
    public boolean isCompatible(ItemStack item) {
        if (item.getType() == Material.BOOK) {
            return true;
        }
        
        // Basic applicability check
        if (!AEAPI.getMaterialsForEnchantment(ench).contains(item.getType().toString())) {
            return false;
        }
        
        // Check required enchants
        for (Map.Entry<UEnchantment, Integer> ue : required.entrySet()) {
            int level = ue.getKey().getLevel(item);
            if (level < ue.getValue() || level == 0) {
                return false;
            }
        }
        
        // Check incompatible enchants
        for (Map.Entry<UEnchantment, Integer> ue : incompatible.entrySet()) {
            if (ue.getKey().getLevel(item) >= ue.getValue()) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void getInfo(Player player) {
        player.performCommand("ae info " + ench);
    }

    @Override
    public int getLevel(ItemStack item) {
        return AEAPI.getEnchantLevel(ench, item);
    }

    @Override
    public void removeEnchant(ItemStack item) {
        AEAPI.removeEnchantment(item, ench);
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
        UCustomEnchant other = (UCustomEnchant) obj;
        return Objects.equals(ench, other.ench);
    }
}
