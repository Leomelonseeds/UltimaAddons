package com.leomelonseeds.ultimaaddons.invs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public abstract class UAInventory {
    
    protected static InventoryManager manager = UltimaAddons.getPlugin().getInvs();
    protected Inventory inv;
    
    public UAInventory(Player player, int size, String title) {
        inv = Bukkit.createInventory(null, size, Utils.toComponent(title));
        register(player);
    }
    
    public UAInventory(Player player, InventoryType type, String title) {
        inv = Bukkit.createInventory(null, type, Utils.toComponent(title));
        register(player);
    }
    
    private void register(Player player) {
        manager.registerInventory(player, this);
    }
    
    public abstract void updateInventory();
    
    public abstract void registerClick(int slot, ClickType type);
    
    public Inventory getInventory() {
        return inv;
    }
}
