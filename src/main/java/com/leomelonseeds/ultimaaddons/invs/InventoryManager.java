package com.leomelonseeds.ultimaaddons.invs;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class InventoryManager {
    
    private Map<Player, UAInventory> inventoryCache;
    
    public InventoryManager() {
        inventoryCache = new HashMap<>();
    }
    
    public UAInventory getInventory(Player player) {
        return inventoryCache.get(player);
    }
    
    // Registers and opens an inventory
    public void registerInventory(Player player, UAInventory inv) {
        inv.updateInventory();
        player.openInventory(inv.getInventory());
        inventoryCache.put(player, inv);
    }
    
    public void removePlayer(Player player) {
        inventoryCache.remove(player);
    }
}
