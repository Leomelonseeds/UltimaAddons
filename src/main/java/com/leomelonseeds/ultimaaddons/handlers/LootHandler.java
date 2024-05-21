package com.leomelonseeds.ultimaaddons.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.handlers.item.ItemManager;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.user.SkillsUser;
import net.advancedplugins.ae.api.AEAPI;

public class LootHandler implements Listener {

    private UltimaAddons plugin;
    private Map<Integer, String> groups;
    private Random rand;
    private ConfigurationSection lootConfig;
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
        this.rand = new Random();
    }
    
    /**
     * Generates a random piece of gear of type.
     * Group should not be 1 if type is a melee weapon.
     * 
     * @param ring
     * @param type
     * @return
     */
    public ItemStack randomGear(int ring, String type) {
        // TODO: Check for upgrade chance
        
        // Group = ring + 1 (1st ring = uncommon) 
        int tier = ring + 1;
        String group = groups.get(tier);
        if (group == null) {
            return null;
        }
        
        // Determine exact gear material to use
        String matName = type;
        if (type.equalsIgnoreCase("sword") || type.equalsIgnoreCase("axe")) {
            matName = lootConfig.getString("main." + group + ".weapon") + "_" + matName;
        } else if (!type.equalsIgnoreCase("bow")) {
            matName = lootConfig.getString("main." + group + ".armor") + "_" + matName;
        }
        matName = matName.toUpperCase();
        
        if (!EnumUtils.isValidEnum(Material.class, matName)) {
            return null;
        }
        
        // TODO: Assign vanilla enchants using Server.getItemFactory()
        // TODO: Assign custom enchants using getMaxLevel
        return null;
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
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void lootOres(BlockBreakEvent e) {
        String type = e.getBlock().getType().toString();
        if (!type.contains("ORE") || e.getExpToDrop() <= 0) {
            return;
        }
        
        double base;
        String fullpath = "ores.chance." + type;
        if (lootConfig.contains(fullpath)) {
            base = lootConfig.getDouble(fullpath);
        } else if (type.contains("DEEPSLATE_")) {
            fullpath = fullpath.replace("DEEPSLATE_", "");
            base = lootConfig.getDouble(fullpath, 0);
        } else {
            return;
        }
        
        if (base <= 0) {
            return;
        }
        
        Location loc = e.getBlock().getLocation();
        int group = getGroup(loc);
        if (group == -1) {
            return;
        }


        // Apply fortune buff
        Player player = e.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        double fadd = lootConfig.getDouble("ores.fortune-add");
        base += fadd * tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        
        // Apply aurelium mining luck buff
        AuraSkillsApi auraSkills = AuraSkillsApi.get();
        SkillsUser user = auraSkills.getUser(player.getUniqueId());
        double ladd = lootConfig.getDouble("ores.luck-add");
        base += ladd * user.getEffectiveTraitLevel(Traits.MINING_LUCK);
        
        // Get final dust level and drop item
        int dustLvl = getMaxLevel(group, base);
        if (dustLvl <= 0) {
            return;
        }
        
        ItemManager items = UltimaAddons.getPlugin().getItems();
        ItemStack toDrop = items.getItem(groups.get(dustLvl) + "dust");
        loc.getWorld().dropItemNaturally(loc, toDrop);
    }
    
    @EventHandler
    public void onLoot(LootGenerateEvent e) {
        ItemManager items = UltimaAddons.getPlugin().getItems();
        List<ItemStack> loot = e.getLoot();

        // Find location and get corresponding group
        int group = getGroup(e.getLootContext().getLocation());
        if (group == -1) {
            return;
        }
        
        // Generate dusts
        String tier = groups.get(group);
        int amt = rand.nextInt(lootConfig.getInt("main." + tier + ".maxdust") + 1);
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
     * Given a location, check which rarity group it corresponds to
     * 
     * @param loc
     * @return -1 if group not found
     */
    private int getGroup(Location loc) {
        int dist = Math.max(Math.abs(loc.getBlockX()), Math.abs(loc.getBlockZ()));
        int multiplier = loc.getWorld().getEnvironment() == Environment.NETHER ? 2 : 1;
        ConfigurationSection groupConfig = lootConfig.getConfigurationSection("main");
        int group = 1;
        for (String key : groupConfig.getKeys(false)) {
            if (!groups.containsKey(group)) {
                return -1;
            }
            
            // Find first group such that the location is within bounds
            // If in nether, multiply coord by 2 to correspond to overworld
            if (dist * multiplier < groupConfig.getInt(key + ".distance")) {
                break;
            }
            
            group++;
        }
        
        return group;
    }
    
    private int getMaxLevel(int max) {
        return getMaxLevel(max, 0);
    }
    
    /**
     * Iterate through percentage chances and determine what level
     * for a loot to generate at
     * 
     * @param max minimum 1
     * @param the base chance. Set to 0 to use common base chance
     * @return
     */
    private int getMaxLevel(int max, double base) {
        int lvl = 0;
        while (lvl < max) {
            lvl++;
            
            double chance;
            if (lvl == 1 && base != 0) {
                chance = base / 100.0;
            } else {
                chance = lootConfig.getInt("main." + groups.get(lvl) + ".chance") / 100.0;
            }
            
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
