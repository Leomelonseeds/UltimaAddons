package com.leomelonseeds.ultimaaddons.handlers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * THANKS TO https://github.com/JustKato/BetterShulkers FOR THE CODE, MODIFIED TO WORK ON RIGHT CLICK
 */
public class ShulkerHandler implements Listener {
    
    private Map<Inventory, ItemStack> openShulkers;
    
    public ShulkerHandler() {
        this.openShulkers = new HashMap<>();
    }

    // React each time a shulker is dropped
    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        
        ItemStack item = e.getItem();
        if (item == null) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        // Get the ShulkerBox meta
        ShulkerBox sm = getShulkerMeta(meta);
        if (sm == null) {
            return;
        }

        Inventory inv = sm.getInventory();
        openShulkers.put(inv, item);
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 0.5F, 1F);
        e.setCancelled(true);
    }

    @EventHandler
    public void onShulkerInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) {
            return;
        }
        
        Inventory inv = e.getInventory();
        ItemStack item = openShulkers.remove(inv);
        if (item == null) {
            return;
        }
        
        handleInventoryShananigans(inv, item);
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 0.5F, 1F);
    }

    @EventHandler
    public void onShulkerInventoryInteract(InventoryInteractEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Inventory inv = e.getInventory();
        ItemStack item = openShulkers.get(inv);
        if (item == null) {
            return;
        }
        
        handleInventoryShananigans(inv, item);
    }
    
    
    @EventHandler
    public void onPlayerDropOpenedShulker(PlayerDropItemEvent e) {
        Inventory inv = e.getPlayer().getOpenInventory().getTopInventory();
        ItemStack item = openShulkers.get(inv);
        if (item == null) {
            return;
        }

        // Check if the currently open inventory is a shulker
        if (e.getItemDrop().getItemStack().equals(item)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMoveOpenedShulkers(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) {
            return;
        }
        
        // Get the currently open inventory
        Inventory inv = p.getOpenInventory().getTopInventory();
        if (!openShulkers.containsKey(inv)) {
            return;
        }

        if (e.getCurrentItem().equals(openShulkers.get(inv))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onShulkerPlace(BlockPlaceEvent e) {
        var blockPlaced = e.getBlockPlaced();
        if (!(blockPlaced.getType().equals(Material.SHULKER_BOX))) {
            return;
        }
        
        InventoryView openedInventory = e.getPlayer().getOpenInventory();
        if (openedInventory.getType().equals(InventoryType.SHULKER_BOX)) {
            e.setCancelled(true);
        }
    }

    /**
     * Make sure that 
     * 
     * @param inv
     */
    private static void handleInventoryShananigans(Inventory inv, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        
        ShulkerBox sm = getShulkerMeta(meta);
        if (sm == null) {
            return;
        }
        
        sm.getInventory().setContents(inv.getContents());
        BlockStateMeta bsm = (BlockStateMeta) meta;
        bsm.setBlockState(sm);
        item.setItemMeta(bsm);
    }

    private static ShulkerBox getShulkerMeta(@Nonnull ItemMeta meta) {
        if (!(meta instanceof BlockStateMeta bsm)) {
            return null;
        }
        
        BlockState csm = bsm.getBlockState();
        if (!(csm instanceof ShulkerBox sb)) {
            return null;
        }

        return sb;
    }
}
