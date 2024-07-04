package com.leomelonseeds.ultimaaddons.handlers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

/**
 * MODIFIED HEAVILY FROM https://github.com/JustKato/BetterShulkers
 */
public class ShulkerHandler implements Listener {
    
    private NamespacedKey shulkerKey;
    private Map<Player, Pair<Inventory, String>> openShulkers;
    
    public ShulkerHandler(UltimaAddons plugin) {
        this.shulkerKey = new NamespacedKey(plugin, "ushulker");
        this.openShulkers = new HashMap<>();
    }
    
    /**
     * ONLY CALL ON DISABLE!!!
     */
    public void saveAll() {
        openShulkers.keySet().forEach(p -> saveShulker(p, null));
    }

    // React each time a shulker is dropped
    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!UltimaAddons.getPlugin().getConfig().getBoolean("enable-shulkers")) {
            return;
        }
        
        Player p = e.getPlayer();
        if (openShulkers.containsKey(p)) {
            return;
        }
        
        if (e.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        
        ItemStack item = e.getItem();
        ShulkerBox sm = getShulkerMeta(item);
        if (sm == null) {
            return;
        }
        
        String id = Long.toString(System.currentTimeMillis());
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(shulkerKey, PersistentDataType.STRING, id);
        item.setItemMeta(meta);

        Inventory inv = sm.getInventory();
        openShulkers.put(p, Pair.of(inv, id));
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 0.5F, 1F);
    }

    @EventHandler
    public void onShulkerInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) {
            return;
        }
        
        if (!saveShulker(p, e.getInventory())) {
            return;
        }
        
        // Add a small cooldown to stop re-opening in the same tick.
        // Do NOT need to remove the persistent data, since it gets
        // overridden every time a shulker is opened.
        Utils.schedule(10, () -> openShulkers.remove(p));
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 0.5F, 1F);
    }
    
    
    @EventHandler
    public void onPlayerDropOpenedShulker(PlayerDropItemEvent e) {
        Pair<Inventory, String> shulker = openShulkers.get(e.getPlayer());
        if (shulker == null) {
            return;
        }
        
        ItemStack toCheck = e.getItemDrop().getItemStack();
        String checkId = Utils.getItemID(toCheck, shulkerKey);
        if (shulker.getRight().equals(checkId)) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerClickOpenedShulker(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) {
            return;
        }
        
        Pair<Inventory, String> shulker = openShulkers.get(p);
        if (shulker == null) {
            return;
        }
        
        String checkId = Utils.getItemID(e.getCurrentItem(), shulkerKey);
        if (shulker.getRight().equals(checkId)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onShulkerPlace(BlockPlaceEvent e) {
        if (!openShulkers.containsKey(e.getPlayer())) {
            return;
        }
        
        if (e.getBlockPlaced().getType().equals(Material.SHULKER_BOX)) {
            e.setCancelled(true);
        }
    }

    private static ShulkerBox getShulkerMeta(ItemStack item) {
        if (item == null) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        
        if (!(meta instanceof BlockStateMeta bsm)) {
            return null;
        }
        
        BlockState csm = bsm.getBlockState();
        if (!(csm instanceof ShulkerBox sb)) {
            return null;
        }

        return sb;
    }
    
    /**
     * Save a shulker for a player. Optional param
     * inventory to compare the inventory to.
     * 
     * @param p
     * @param inventory
     * @return
     */
    private boolean saveShulker(Player p, Inventory inventory) {
        Pair<Inventory, ItemStack> shulker = getShulker(p);
        if (shulker == null) {
            return false;
        }
        
        Inventory inv = shulker.getLeft();
        if (inventory != null && !inv.equals(inventory)) {
            return false;
        }

        ItemStack item = shulker.getRight();
        ShulkerBox sm = getShulkerMeta(item);
        sm.getInventory().setContents(inv.getContents());
        BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
        bsm.setBlockState(sm);
        item.setItemMeta(bsm);
        return true;
    }
    
    private Pair<Inventory, ItemStack> getShulker(Player player) {
        Pair<Inventory, String> shulker = openShulkers.get(player);
        if (shulker == null) {
            return null;
        }
        
        for (ItemStack item : player.getInventory().getContents()) {
            String toCheck = Utils.getItemID(item, shulkerKey);
            if (shulker.getRight().equals(toCheck)) {
                return Pair.of(shulker.getLeft(), item);
            }
        }

        return null;
    }
}
