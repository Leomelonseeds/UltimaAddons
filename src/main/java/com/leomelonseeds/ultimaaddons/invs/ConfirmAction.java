package com.leomelonseeds.ultimaaddons.invs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.leomelonseeds.ultimaaddons.utils.Utils;

public class ConfirmAction extends UAInventory {
    
    private ConfirmCallback callback;
    private UAInventory mwinv;
    private Player player;
    
    public ConfirmAction(String action, Player player, UAInventory mwinv, ConfirmCallback callback) {
        super(player, 27, "Confirm: " + action);
        this.player = player;
        this.callback = callback;
        this.mwinv = mwinv;
    }

    @Override
    public void updateInventory() {
        // galaxy brain inventory filling
        for (int i = 0; i < 27; i++) {
            int mod = i % 9;
            Material material;
            String name = "";
            if (0 <= mod && mod <= 3) {
                material = Material.EMERALD_BLOCK;
                name = "&aConfirm";
            } else if (5 <= mod && mod <= 8) {
                material = Material.REDSTONE_BLOCK;
                name = "&cCancel";
            } else {
                material = Material.IRON_BARS;
            }
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Utils.toComponent(name));
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        Material material = inv.getItem(slot).getType();
        
        if (material == Material.IRON_BARS) {
            return;
        }
        
        if (mwinv != null) {
            manager.registerInventory(player, mwinv);
        } else {
            player.closeInventory();
        }
        
        if (material == Material.EMERALD_BLOCK) {
            callback.onConfirm(true);
        } else if (material == Material.REDSTONE_BLOCK) {
            callback.onConfirm(false);
        }
    }
}
