package com.leomelonseeds.ultimaaddons.invs;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import com.leomelonseeds.ultimaaddons.UltimaAddons;

public class InventoryManager implements Listener {
    
    private Map<Player, UAInventory> inventoryCache;
    
    public InventoryManager() {
        inventoryCache = new HashMap<>();
    }
    
    public UAInventory getInventory(Player player) {
        return inventoryCache.get(player);
    }
    
    // Registers and opens an inventory
    public void registerInventory(Player player, UAInventory inv) {
        // Run on next tick to give time for constructors to assign values
        Bukkit.getScheduler().runTask(UltimaAddons.getPlugin(), () -> {
            player.openInventory(inv.getInventory());
            inventoryCache.put(player, inv);
            inv.updateInventory();
        });
    }
    
    public void removePlayer(Player player) {
        inventoryCache.remove(player);
    }
    
    /** Handle clicking of custom GUIs */
    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event) {
        // Check if inventory is custom
        Player player = (Player) event.getWhoClicked();
        if (!(getInventory(player) instanceof UAInventory)) {
            return;
        }
        
        // Handle clicking outside GUI
        Inventory inv = event.getClickedInventory();
        if (inv == null) {
            return;
        }
        
        // Do not allow shift clicking bottom inventory 
        if (inv.equals(event.getView().getBottomInventory()) && event.getClick().isShiftClick()) {
            event.setCancelled(true);
            return;
        }
        
        // Allow clicking bottom inventory
        if (!inv.equals(event.getView().getTopInventory())){
            return; 
        }
        
        event.setCancelled(true);
        getInventory(player).registerClick(event.getSlot(), event.getClick());
    }
    
    /** Unregister custom inventories when they are closed. */
    @EventHandler
    public void unregisterCustomInventories(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        
        // Unregister
        if (getInventory(player) instanceof UAInventory) {
            removePlayer(player);
        }
    }
}
