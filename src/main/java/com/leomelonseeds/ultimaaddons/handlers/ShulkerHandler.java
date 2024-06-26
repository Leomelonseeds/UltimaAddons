package com.leomelonseeds.ultimaaddons.handlers;

import java.util.ArrayList;
import java.util.List;

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
    
    public static List<Inventory> shulkerInventoryBinds = new ArrayList<>();

    // React each time a shulker is dropped
    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(PlayerInteractEvent ev) {
        // Get a reference to the player
        Player p = ev.getPlayer();
        if (ev.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        
        ItemStack item = ev.getItem();
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

        // Get a reference to the inventory
        Inventory inv = sm.getInventory();
        // Mark the inventory as a shulker listener
        shulkerInventoryBinds.add(inv);

        // Open the inventory of the shulker for the player
        p.openInventory(inv);

        // Play open sound
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 0.5F, 1F);
        // Cancel the throw event
        ev.setCancelled(true);
    }

    @EventHandler
    public void onShulkerInventoryClose(InventoryCloseEvent ev) {
        Inventory inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
        if (!(ev.getPlayer() instanceof Player p)) return;

        handleInventoryShananigans(inv, item);
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 0.5F, 1F);
    }

    @EventHandler
    public void onShulkerInventoryClick(InventoryClickEvent ev) {
        Inventory inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        if (!(ev.getWhoClicked() instanceof Player p)) return;
        ItemStack item = p.getInventory().getItemInMainHand();

        handleInventoryShananigans(inv, item);
    }

    @EventHandler
    public void onShulkerInventoryInteract(InventoryInteractEvent ev) {
        Inventory inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        if (!(ev.getWhoClicked() instanceof Player p)) return;
        ItemStack item = p.getInventory().getItemInMainHand();

        handleInventoryShananigans(inv, item);
    }
    
    
    @EventHandler
    public void onPlayerDropOpenedShulker(PlayerDropItemEvent ev) {
        // Get the currently open inventory
        InventoryView openedInventory = ev.getPlayer().getOpenInventory();

        // Check if the currently open inventory is a shulker
        if ( openedInventory.getType().equals(InventoryType.SHULKER_BOX) )
            // Cancel the event
            ev.setCancelled(true);
    }

    @EventHandler
    public void onMoveOpenedShulkers(InventoryClickEvent ev) {
        if (!(ev.getWhoClicked() instanceof Player p) )
            return;

        ItemStack itemRef = p.getInventory().getItemInMainHand();
        Inventory invRef = ev.getInventory();

        // Check if this is even a shulker inventory
        if (!invRef.getType().equals(InventoryType.SHULKER_BOX) )
            return;

        // Make sure the item that is being moved is not the shulker.
        if (itemRef.equals(ev.getCurrentItem()) ) {
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onShulkerPlace(BlockPlaceEvent ev) {
        var blockPlaced = ev.getBlockPlaced();

        // Check if the block being placed is even a shulker
        if (!(blockPlaced.getType().equals(Material.SHULKER_BOX)) )
            return;

        // Get the currently open inventory
        InventoryView openedInventory = ev.getPlayer().getOpenInventory();

        // Check if the currently open inventory is a shulker
        if (openedInventory.getType().equals(InventoryType.SHULKER_BOX) )
            // Cancel the event
            ev.setCancelled(true);

    }

    private static void handleInventoryShananigans(Inventory inv, ItemStack item) {

        // Get the meta of the item
        ItemMeta meta = item.getItemMeta();

        // Check if the meta of the item is null, if it is simply ignore this event.
        if (meta == null) return;
        // Get the ShulkerBox meta
        ShulkerBox sm = getShulkerMeta(meta);
        // Check if the meta is indeed a ShulkerBox meta
        if (sm == null) return;
        // Set the shulker box's inventory contents
        sm.getInventory().setContents(inv.getContents());
        // Important: Update the BlockStateMeta with the modified ShulkerBox
        BlockStateMeta bsm = (BlockStateMeta) meta;
        bsm.setBlockState(sm);
        // Finally, apply the updated BlockStateMeta back to the original item
        item.setItemMeta(bsm);
    }

    private static ShulkerBox getShulkerMeta(@Nonnull ItemMeta meta) {
        if (!(meta instanceof BlockStateMeta bsm)) return null;
        BlockState csm = bsm.getBlockState();
        if (!(csm instanceof ShulkerBox sb)) return null;

        return sb;
    }
}
