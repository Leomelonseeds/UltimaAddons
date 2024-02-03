package com.leomelonseeds.ultimaaddons.handlers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.skills.Skills;

public class ScaledAttribute {
    
    private static final int MIN_LVL = 25;
    
    private Skills skill;
    private Attribute attribute;
    private String aname;
    private Operation operation;
    private double amount;
    
    public ScaledAttribute(ConfigurationSection sec) {
        try {
            skill = Skills.valueOf(sec.getString("skill"));
            attribute = Attribute.valueOf(sec.getString("type"));
            aname = sec.getString("name");
            operation = Operation.valueOf(sec.getString("operation"));
            amount = sec.getDouble("amount");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Something went wrong while trying to create scaled attribute for " + sec.getCurrentPath());
            e.printStackTrace();
        }
    }
    
    /**
     * @param p
     * @return an {@link AttributeModifier} determined by the player's aurelium level for this scaled attribute's skill
     */
    public AttributeModifier getModifier(Player p, EquipmentSlot slot) {
        // If player lvl <= 25 don't do anything
        int multiplier = AureliumAPI.getSkillLevel(p, skill) - MIN_LVL;
        if (multiplier <= 0) {
            return null;
        }
        
        // Add back default armor toughness
        double famt = amount * multiplier;
        if (attribute == Attribute.GENERIC_ARMOR_TOUGHNESS) {
            famt += ArmorSetManager.DEFAULT_TOUGHNESS;
        }
        
        return new AttributeModifier(UUID.randomUUID(), aname, famt, operation, slot);
    }
    
    public Attribute getAttribute() {
        return attribute;
    }
}
