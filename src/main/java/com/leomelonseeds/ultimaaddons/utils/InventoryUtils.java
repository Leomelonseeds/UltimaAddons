package com.leomelonseeds.ultimaaddons.utils;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

    public static boolean isInventoryFull(Player player) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }
        }
        return true;
    }

    /**
     * Close inventory player which contain titles
     * 
     * @param k
     * @param titles
     */
    public static void closeInventory(Player p, String... titles) {
        if (p == null || !p.isOnline()) {
            return;
        }
        
        String ctitle = Utils.toPlain(p.getOpenInventory().title());
        for (String t : titles) {
            // Stop chat confirm from happening
            if (t.equals("Challenge")) {
                ChatConfirm pc = ChatConfirm.instances.get(p);
                if (pc != null && pc.getReq().equals("confirm")) {
                    pc.stop();
                }
            }
            
            if (!ctitle.contains(t)) {
                continue;
            }
            
            p.closeInventory();
            return;
        }
    }

    /**
     * Give players items, dropping if necessary
     * 
     * @param player
     * @param items
     */
    public static void giveItems(Player player, ItemStack... items) {
        for (ItemStack drop : player.getInventory().addItem(items).values()) {
            Item dropped = player.getWorld().dropItem(player.getLocation(), drop);
            dropped.setOwner(player.getUniqueId());
        }
    }
}
