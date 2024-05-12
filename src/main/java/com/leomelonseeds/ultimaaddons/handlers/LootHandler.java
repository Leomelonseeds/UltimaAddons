package com.leomelonseeds.ultimaaddons.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
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
    
    private UltimaAddons plugin;
    private ConfigurationSection lootConfig;
    private Map<Integer, String> groups;
    private Set<Player> canBreak;
    
    public LootHandler(UltimaAddons plugin) {
        this.groups = Map.of(
                1, "common",
                2, "uncommon",
                3, "rare",
                4, "epic",
                5, "legendary"
            );
        this.canBreak = new HashSet<>();
        this.plugin = plugin;
        reload();
    }
    
    public void reload() {
        this.lootConfig = plugin.getConfig().getConfigurationSection("loot");
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
        List<ItemStack> loot = e.getLoot();

        // Find location and get corresponding group
        Location loc = e.getLootContext().getLocation();
        int dist = Math.max(Math.abs(loc.getBlockX()), Math.abs(loc.getBlockZ()));
        int multiplier = loc.getWorld().getEnvironment() == Environment.NETHER ? 2 : 1;
        int group = 1;
        for (String key : lootConfig.getKeys(false)) {
            if (!groups.containsKey(group)) {
                return;
            }
            
            // Find first group such that the location is within bounds
            // If in nether, multiply coord by 2 to correspond to overworld
            if (dist * multiplier < lootConfig.getInt(key + ".distance")) {
                break;
            }
            
            group++;
        }
        
        // Generate dusts
        String tier = groups.get(group);
        int amt = new Random().nextInt(lootConfig.getInt(tier + ".maxdust") + 1);
        for (int i = 0; i < amt; i++) {
            int dustLvl = getMaxLevel(group);
            if (dustLvl > 0) {
                loot.add(items.getItem(groups.get(dustLvl) + "dust"));
            }
        }
        
        // Randomly enchant gear
        if (group <= 1) {
            return;
        }
        
        for (ItemStack gear : loot) {
            if (gear.getType().getMaxStackSize() > 1) {
                continue;
            }
            
            int enchLvl = getMaxLevel(group);
            for (int i = enchLvl; i > 1; i--) {
                if (randomlyEnchant(gear, i)) {
                    break;
                }
            }
        }
    }
    
    /**
     * Iterate through percentage chances and determine what level
     * for a loot to generate at
     * 
     * @param max minimum 1
     * @return
     */
    private int getMaxLevel(int max) {
        Random rand = new Random();
        int lvl = 0;
        while (lvl < max) {
            lvl++;
            double chance = lootConfig.getInt(groups.get(lvl) + ".chance") / 100.0;
            
            // Keep going if RNG succeeds. Otherwise, return prev level
            if (rand.nextDouble() < chance) {
                continue;
            }
            
            return lvl - 1;
        }
        
        return lvl;
    }
    
    /**
     * Puts a random compatible enchantment of the rarity on the item.
     * If no compatible enchantments are found, nothing happens.
     * If the item is an enchanted book, replaces with a random ench book of this group.
     * 
     * @param item
     * @param group
     * @return true if item got enchanted
     */
    private boolean randomlyEnchant(ItemStack item, int group) {
        Random rand = new Random();
        List<String> available = AEAPI.getEnchantmentsByGroup(groups.get(group));
        if (item.getType() == Material.ENCHANTED_BOOK) {
            String enchant = available.get(rand.nextInt(available.size()));
            int lvl = rand.nextInt(AEAPI.getHighestEnchantmentLevel(enchant)) + 1;
            ItemStack book = AEAPI.createEnchantmentBook(enchant, lvl, 100, 0, null);
            item.setItemMeta(book.getItemMeta());
            return true;
        }
        
        List<String> compatible = new ArrayList<>();
        for (String enchant : available) {
            if (AEAPI.getMaterialsForEnchantment(enchant).contains(item.getType().toString())) {
                compatible.add(enchant);
            }
        }
        
        if (compatible.isEmpty()) {
            return false;
        }
        
        String enchant = compatible.get(rand.nextInt(compatible.size()));
        int lvl = rand.nextInt(AEAPI.getHighestEnchantmentLevel(enchant)) + 1;
        AEAPI.applyEnchant(enchant, lvl, item);
        return true;
    }

}
