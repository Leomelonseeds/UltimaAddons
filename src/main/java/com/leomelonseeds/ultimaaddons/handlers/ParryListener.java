package com.leomelonseeds.ultimaaddons.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;

import com.leomelonseeds.ultimaaddons.UltimaAddons;

public class ParryListener implements Listener {
    
    private Map<UUID, Pair<Long, Long>> shieldClick;
    
    public ParryListener() {
        this.shieldClick = new HashMap<>();
    }
    
    /**
     * Check if a player raised their shield
     * a certain ticks ago before now
     * 
     * @param ticks
     * @return
     */
    public boolean canParry(UUID uuid) {
        if (!shieldClick.containsKey(uuid)) {
            return false;
        }
        
        FileConfiguration config = UltimaAddons.getPlugin().getConfig();
        Pair<Long, Long> times = shieldClick.get(uuid);
        long cooldown = config.getInt("parry-cooldown") * 50;
        if (times.getLeft() - times.getRight() < cooldown) {
            return false;
        }

        long duration = config.getInt("parry-duration") * 50;
        if (System.currentTimeMillis() - times.getLeft() > duration) {
            return false;
        }

        return true;
    }
    
    @EventHandler
    public void onShield(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        PlayerInventory pinv = p.getInventory();
        EquipmentSlot hand = e.getHand();
        if (pinv.getItem(hand).getType() != Material.SHIELD) {
            return;
        }
        
        // Check for shield. Break if success, return if bad
        // We do this by making sure player has their hand raised,
        // and is not holding something in main hand that would
        // also cause their hand to be raised.e
        do {
            // We are using main hand, we are blocking for sure
            if (hand == EquipmentSlot.HAND) {
                break;
            }
            
            // Otherwise check if mainhand item causes one to raise hand
            Material main = pinv.getItemInMainHand().getType();
            if (main.isEdible()) {
                return;
            }
            
            switch (main) {
                case BOW:
                case TRIDENT:
                case SPYGLASS:
                case CROSSBOW:
                case POTION:
                case GOAT_HORN:
                    return;
                default:
                    break;
            }
        } while (false);
        
        Bukkit.getScheduler().runTask(UltimaAddons.getPlugin(), () -> {
            if (!p.isHandRaised()) {
                return;
            }
            
            UUID uuid = p.getUniqueId();
            long prev = 0;
            if (shieldClick.containsKey(uuid)) {
                prev = shieldClick.get(uuid).getLeft();
            }
            
            shieldClick.put(uuid, Pair.of(System.currentTimeMillis(), prev));
        });
    }
}
