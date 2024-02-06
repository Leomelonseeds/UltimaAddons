package com.leomelonseeds.ultimaaddons.invs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public abstract class UAInventory {
    
    protected static InventoryManager manager = UltimaAddons.getPlugin().getInvs();
    protected Inventory inv;
    
    public UAInventory(Player player, int size, String title) {
        inv = Bukkit.createInventory(null, size, Utils.toComponent(title));
        manager.registerInventory(player, this);
    }
    
    public abstract void updateInventory();
    
    public abstract void registerClick(int slot, ClickType type);
    
    public Inventory getInventory() {
        return inv;
    }
}
