package com.leomelonseeds.ultimaaddons.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.kyori.adventure.text.Component;

public class ArmorSetManager implements Listener {
    
    public static String ARMOR_INDICATOR = "scaled-attribute";
    
    /**
     * Key: Armor piece type. Value: Pair of EquipmentSlot and Integer representing generic armor value
     * This stores the default armor values corresponding to a set of diamond armor
     */
    private static Map<String, Pair<EquipmentSlot, Integer>> slots;
    
    public ArmorSetManager() {
        slots = new HashMap<>();
        slots.put("helmet", ImmutablePair.of(EquipmentSlot.HEAD, 3));
        slots.put("chestplate", ImmutablePair.of(EquipmentSlot.CHEST, 8));
        slots.put("leggings", ImmutablePair.of(EquipmentSlot.LEGS, 6));
        slots.put("boots", ImmutablePair.of(EquipmentSlot.FEET, 3));
    }
    
    /**
     * Creates an armor set from a configuration section.
     * The section MUST have "scaled-attribute", "helmet",
     * "chestplate", "leggings", "boots" sections that
     * represent actual items.
     * 
     * @param sec
     */
    public Map<String, ItemStack> createArmorSet(ConfigurationSection sec) {
        Map<String, ItemStack> res = new HashMap<>();
        List<Component> lore = Utils.toComponent(sec.getStringList("lore"));

        // Create actual items
        for (String slot : slots.keySet()) {
            String key = sec.getName() + "." + slot;
            ConfigurationSection cursec = sec.getConfigurationSection(slot);
            ItemStack cur = Utils.createItem(cursec, key);
            
            // Set general lore
            ItemMeta curMeta = cur.getItemMeta();
            curMeta.lore(lore);
            
            // Adds common armor attributes
            EquipmentSlot equipSlot = slots.get(slot).getLeft();
            curMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(
                    UUID.randomUUID(), 
                    "Armor", 
                    slots.get(slot).getRight(), 
                    Operation.ADD_NUMBER, 
                    equipSlot));

            curMeta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(
                    UUID.randomUUID(), 
                    "Armor Toughness", 3, 
                    Operation.ADD_NUMBER, 
                    equipSlot));
            
            cur.setItemMeta(curMeta);
            res.put(key, cur);
        }
        
        return res;
    }
    
    @EventHandler
    public void onPlayerEquip(PlayerArmorChangeEvent e) {
        
    }

    @EventHandler
    public void onDispenserEquip(BlockDispenseArmorEvent e) {
        
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        
    }
}
