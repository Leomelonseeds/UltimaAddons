package com.leomelonseeds.ultimaaddons.handlers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

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
}
