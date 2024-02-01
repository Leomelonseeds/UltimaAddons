package com.leomelonseeds.ultimaaddons.handlers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;

public class ArmorSetManager implements Listener {
    
    private Map<String, AttributeModifier> attrs;
    
    public ArmorSetManager() {
        this.attrs = new HashMap<>();
    }
    
    public void createArmorSet(ConfigurationSection sec) {
        // TODO
    }
    
    public void clearAttrs() {
        attrs.clear();
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
