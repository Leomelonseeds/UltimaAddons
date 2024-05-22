package com.leomelonseeds.ultimaaddons.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataType;

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
    private NamespacedKey ugear;
    
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
        this.ugear = new NamespacedKey(plugin, "ugear");
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
     * @param ring btwn 1-4 inclusive
     * @param type
     * @return
     */
    public ItemStack randomGear(int ring, String type) {
        // Get tier of the random gear.
        // Always give 30% chance to upgrade to the next tier
        // Unless we're already at max tier, then lower chance
        // for diamond tier
        int tier = getMaxLevel(ring, 100);
        if (tier < 4 && rand.nextDouble() < 0.3) {
            tier++;
        } else if (tier == 4 && rand.nextDouble() < 0.1) {
            tier = 5;
        }
        
        String group = groups.get(tier);
        if (group == null) {
            return null;
        }
        
        // Determine exact gear material to use
        String matName = type;
        ConfigurationSection sec = lootConfig.getConfigurationSection("main." + group);
        if (type.equalsIgnoreCase("sword") || type.equalsIgnoreCase("axe")) {
            matName = sec.getString("weapon") + "_" + matName;
        } else if (!type.contains("bow")) {
            matName = sec.getString("armor") + "_" + matName;
        }
        
        matName = matName.toUpperCase();
        if (!EnumUtils.isValidEnum(Material.class, matName)) {
            return null;
        }
        
        // Apply some random damage and add PDC for loot ID
        Material mat = Material.valueOf(matName);
        ItemStack gear = new ItemStack(mat);
        int dur = mat.getMaxDurability();
        Damageable dmeta = (Damageable) gear.getItemMeta();
        double dmgPercent = rand.nextDouble(0.6, 0.95);
        dmeta.setDamage((int) (dur * dmgPercent));
        dmeta.getPersistentDataContainer().set(ugear, PersistentDataType.BOOLEAN, true);
        gear.setItemMeta(dmeta);
        
        // Check for possible enchants
        if (rand.nextDouble() > sec.getDouble("enchant.chance") / 100.0) {
            return gear;
        }
        
        // Otherwise apply ench table enchant and attempt to custom enchant as well
        int levels = rand.nextInt(sec.getInt("enchant.min"), sec.getInt("enchant.max") + 1);
        gear = Bukkit.getItemFactory().enchantWithLevels(gear, levels, false, rand);
        int custom = getMaxLevel(ring + 1);
        if (custom > 1) {
            for (int i = custom; i > 1; i--) {
                if (randomlyEnchant(gear, i)) {
                    break;
                }
            }
        }
        
        return gear;
    }
    
    // Custom handler for mob loot
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity ent = e.getEntity();
        Player player = ent.getKiller();
        if (player == null) {
            return;
        }
        
        // Gather entity equipment
        EntityEquipment equipped = ent.getEquipment();
        Set<ItemStack> contents = new HashSet<>();
        contents.add(equipped.getItemInMainHand());
        contents.add(equipped.getItemInOffHand());
        for (ItemStack item : equipped.getArmorContents()) {
            contents.add(item);
        }
        
        // Apply looting buffs
        ItemStack weapon = player.getInventory().getItemInMainHand();
        double chance = lootConfig.getDouble("mobs.drop-chance");
        double ladd = lootConfig.getDouble("mobs.looting-add");
        chance += ladd * weapon.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
        
        // Final checks before dropping
        for (ItemStack item : contents) {
            if (item == null || !item.hasItemMeta()) {
                continue;
            }
            
            // If has persistent data container, then regular drop chance
            // has already been set to 0!
            ItemMeta meta = item.getItemMeta();
            if (!meta.getPersistentDataContainer().has(ugear)) {
                continue;
            }
            
            meta.getPersistentDataContainer().remove(ugear);
            if (rand.nextDouble() < chance / 100.0) {
                ent.getWorld().dropItem(ent.getLocation(), item);
            }
        }
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
