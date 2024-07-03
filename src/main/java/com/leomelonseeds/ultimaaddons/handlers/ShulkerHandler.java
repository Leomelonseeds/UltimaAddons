package com.leomelonseeds.ultimaaddons.handlers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

/**
 * THANKS TO https://github.com/JustKato/BetterShulkers FOR THE CODE, MODIFIED TO WORK ON RIGHT CLICK
 */
public class ShulkerHandler implements Listener {
    
    private Map<Player, Pair<Inventory, ItemStack>> openShulkers;
    
    public ShulkerHandler() {
        this.openShulkers = new HashMap<>();
    }
    
    public void saveAll() {
        openShulkers.values().forEach(s -> saveShulker(s));
    }

    // React each time a shulker is dropped
    @EventHandler(priority = EventPriority.LOWEST)
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
        openShulkers.put(p, Pair.of(inv, item));
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 0.5F, 1F);
        e.setCancelled(true);
    }

    @EventHandler
    public void onShulkerInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) {
            return;
        }
        
        Pair<Inventory, ItemStack> shulker = openShulkers.get(p);
        if (shulker == null) {
            return;
        }
        
        // Add a small cooldown to stop opening in the same tick
        Utils.schedule(10, () -> openShulkers.remove(p));
        saveShulker(shulker);
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 0.5F, 1F);
    }
    
    
    @EventHandler
    public void onPlayerDropOpenedShulker(PlayerDropItemEvent e) {
        if (!openShulkers.containsKey(e.getPlayer())) {
            return;
        }
        
        if (e.getItemDrop().getItemStack().getType().equals(Material.SHULKER_BOX)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMoveItem(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) {
            return;
        }
        
        if (!openShulkers.containsKey(p)) {
            return;
        }
        
        ItemStack item = openShulkers.get(p).getRight();
        if (e.getCurrentItem() != null && e.getCurrentItem().equals(item)) {
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

    private static void saveShulker(Pair<Inventory, ItemStack> shulker) {
        ItemStack item = shulker.getRight();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        
        ShulkerBox sm = getShulkerMeta(meta);
        if (sm == null) {
            return;
        }
        
        sm.getInventory().setContents(shulker.getLeft().getContents());
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
