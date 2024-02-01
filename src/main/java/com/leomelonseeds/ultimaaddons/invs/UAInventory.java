package com.leomelonseeds.ultimaaddons.invs;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public interface UAInventory {
    InventoryManager manager = UltimaAddons.getPlugin().getInvManager();

    public void updateInventory();

    public void registerClick(int slot, ClickType type);

    public Inventory getInventory();
}
