package com.leomelonseeds.ultimaaddons.invs;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import com.leomelonseeds.ultimaaddons.UltimaAddons;

public interface UAInventory {
    InventoryManager manager = UltimaAddons.getPlugin().getInvs();
    
    public void updateInventory();
    
    public void registerClick(int slot, ClickType type);
    
    public Inventory getInventory();
}
