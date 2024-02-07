package com.leomelonseeds.ultimaaddons.handlers;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.leomelonseeds.ultimaaddons.UltimaAddons;

public class MiscListener implements Listener {
    
    private UltimaAddons plugin;
    
    public MiscListener(UltimaAddons plugin) {
        this.plugin = plugin;
    }
    
    // Drop obsidian ingot when obsidian is blown up
    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        Random random = new Random();
        e.blockList().forEach(b -> {
            if (b.getType() != Material.OBSIDIAN) {
                return;
            }
            
            double chance = plugin.getConfig().getDouble("items.obsidianingot.chance");
            if (random.nextDouble() < chance) {
                return;
            }
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                b.getWorld().dropItem(b.getLocation().toCenterLocation(), 
                        plugin.getItems().getItem("obsidianingot"));
            });
        });
    }
}
