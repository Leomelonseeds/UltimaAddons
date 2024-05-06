package com.leomelonseeds.ultimaaddons.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.handlers.item.ItemManager;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.api.AEAPI;

public class LootHandler implements Listener {
    
    private Map<Integer, String> groups;
    private Set<Player> canBreak;
    
    public LootHandler() {
        this.canBreak = new HashSet<>();
        this.groups = Map.of(
            1, "common",
            2, "uncommon",
            3, "rare",
            4, "epic",
            5, "legendary"
        );
    }
    
    // Stop players from breaking blocks with loot 
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof Lootable lootable)) {
            return;
        }
        
        if (!lootable.hasLootTable()) {
            return;
        }
        
        Player player = e.getPlayer();
        if (canBreak.remove(player)) {
            return;
        }
        
        e.setCancelled(true);
        player.sendMessage(Utils.toComponent("&cThis chest will regenerate its loot in the future! "
                + "Please break it again if you are sure you want to do so."));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
        canBreak.add(player);
        Utils.schedule(30 * 20, () -> canBreak.remove(player));
    }
    
    @EventHandler
    public void onLoot(LootGenerateEvent e) {
        ItemManager items = UltimaAddons.getPlugin().getItems();
        // List<ItemStack> loot = e.getLoot();
        // loot.add(items.getItem("commondust"));
    }
    
    /**
     * Puts a random compatible enchantment of the rarity on the item.
     * If no compatible enchantments are found, nothing happens
     * 
     * @param item
     * @param group
     */
    private void randomlyEnchant(ItemStack item, int group) {
        List<String> compatible = new ArrayList<>();
        for (String enchant : AEAPI.getEnchantmentsByGroup(groups.get(group))) {
            if (AEAPI.getMaterialsForEnchantment(enchant).contains(item.getType().toString())) {
                compatible.add(enchant);
            }
        }
        
        Bukkit.getLogger().info("Compatible enchants found: " + compatible);
        if (compatible.isEmpty()) {
            return;
        }
        
        Random rand = new Random();
        String enchant = compatible.get(rand.nextInt(compatible.size()));
        int lvl = rand.nextInt(AEAPI.getHighestEnchantmentLevel(enchant)) + 1;
        AEAPI.applyEnchant(enchant, lvl, item);
    }

}
