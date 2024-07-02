package com.leomelonseeds.ultimaaddons.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataType;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.handlers.item.ItemManager;
import com.leomelonseeds.ultimaaddons.objects.UASkills;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.user.SkillsUser;
import net.advancedplugins.ae.api.AEAPI;

/**
 * Everything to do with dropping enchanted dust, and mob gear
 */
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
        int tier = getMaxLevel(ring, 100);
        if (rand.nextDouble() < 0.3) {
            tier++;
        }
        
        String group = groups.get(tier);
        if (group == null) {
            return null;
        }
        
        // Determine exact gear material to use
        String matName = type;
        ConfigurationSection sec = lootConfig.getConfigurationSection("main." + group);
        if (type.equals("sword") || type.equals("axe") ||
            type.equals("pickaxe") || type.equals("shovel")) {
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
        randomlyEnchant(gear, getMaxLevel(ring + 1, sec.getDouble("enchant.custom-chance")));
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
        
        if (contents.isEmpty()) {
            return;
        }
        
        // Apply looting buffs
        ItemStack weapon = player.getInventory().getItemInMainHand();
        double chance = lootConfig.getDouble("mobs.drop-chance");
        double ladd = lootConfig.getDouble("mobs.looting-add");
        chance += ladd * weapon.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
        
        // Apply AuraSkills buffs
        AuraSkillsApi auraSkills = AuraSkillsApi.get();
        SkillsUser user = auraSkills.getUser(player.getUniqueId());
        int burglar = user.getAbilityLevel(UASkills.BURGLAR);
        if (burglar > 0) {
            chance += UASkills.BURGLAR.getValue(burglar);
        }
        
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
    
    // Drops dust or enchanted books when fishing
    @EventHandler
    public void onFish(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        
        Item caught = (Item) e.getCaught();
        ItemStack loot = caught.getItemStack();
        if (loot.getType() != Material.ENCHANTED_BOOK && loot.getType() != Material.BOW) {
            return;
        }

        int lvl = getMaxLevel(getGroup(caught.getLocation()));
        if (lvl <= 0) {
            return;
        }
        
        if (loot.getType() == Material.BOW) {
            randomlyEnchant(loot, lvl);
            caught.setItemStack(loot);
            return;
        }
        
        // Material must be an enchanted book here
        double dustChance = lootConfig.getDouble("fishing-dust-chance") / 100.0;
        if (rand.nextDouble() > dustChance) {
            randomlyEnchant(loot, lvl);
            caught.setItemStack(loot);
            return;
        }

        ItemManager items = UltimaAddons.getPlugin().getItems();
        ItemStack dust = items.getItem(groups.get(lvl) + "dust");
        caught.setItemStack(dust);
    }
    
    // Drops random amounts of enchanted dust on grindstone disenchant
    // Thanks https://github.com/Archy-X/AuraSkills/blob/1bf7845c347c60bab6da67f58c383017dcf5aa6a/bukkit/src/main/java/dev/aurelium/auraskills/bukkit/skills/forging/ForgingAbilities.java#L42
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisenchant(InventoryClickEvent e) {
        // Check if a valid item is being
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        Inventory inv = e.getClickedInventory();
        if (inv == null) {
            return;
        }

        ClickType click = e.getClick();
        if (click != ClickType.LEFT && click != ClickType.RIGHT && Utils.isInventoryFull(player)) return;
        if (e.getResult() != Result.ALLOW) return;

        // Only give if item was picked up
        InventoryAction action = e.getAction();
        if (action != InventoryAction.PICKUP_ALL && action != InventoryAction.MOVE_TO_OTHER_INVENTORY &&
            action != InventoryAction.PICKUP_HALF && action != InventoryAction.DROP_ALL_SLOT &&
            action != InventoryAction.DROP_ONE_SLOT && action != InventoryAction.HOTBAR_SWAP) {
            return;
        }
        
        if (player.getItemOnCursor().getType() != Material.AIR) {
            if (action == InventoryAction.DROP_ALL_SLOT || action == InventoryAction.DROP_ONE_SLOT) {
                return;
            }
        }
        
        if (e.getClickedInventory().getType() != InventoryType.GRINDSTONE) {
            return;
        }

        if (e.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }
        
        Location location = inv.getLocation();
        if (location == null) return;

        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);
        if (first != null && second != null) { // If two items, make sure items are the same type
            if (first.getType() != second.getType()) {
                return;
            }
        }
        
        List<Pair<Enchantment, Integer>> vanilla = new ArrayList<>();
        List<Pair<String, Integer>> custom = new ArrayList<>();
        for (ItemStack item : new ItemStack[] {first, second}) {
            if (item == null) {
                continue;
            }
            
            for (Entry<Enchantment, Integer> ench : item.getEnchantments().entrySet()) {
                Enchantment ve = ench.getKey();
                if (ve.equals(Enchantment.BINDING_CURSE) || ve.equals(Enchantment.VANISHING_CURSE)) {
                    continue;
                }
                
                vanilla.add(Pair.of(ve, ench.getValue()));
            }
            
            for (Entry<String, Integer> ench : AEAPI.getEnchantmentsOnItem(item).entrySet()) {
                custom.add(Pair.of(ench.getKey(), ench.getValue()));
            }
            
            if (AEAPI.isCustomEnchantBook(item)) {
                custom.add(Pair.of(AEAPI.getBookEnchantment(item), AEAPI.getBookEnchantmentLevel(item)));
            }
        }
        
        if (vanilla.isEmpty() && custom.isEmpty()) {
            return;
        }
        
        // Stores the chance sums of each tier
        Map<String, Double> sums = new HashMap<>();
        
        if (!vanilla.isEmpty()) {
            sums.put("common", 0.0);
            double mult = lootConfig.getDouble("grindstone.vanilla-chance");
            for (Pair<Enchantment, Integer> ev : vanilla) {
                int lvl = ev.getRight();
                int minChance = ev.getLeft().getMinModifiedCost(lvl);
                int maxChance = ev.getLeft().getMaxModifiedCost(lvl);
                double addedChance = rand.nextInt(minChance, maxChance + 1) * mult;
                sums.put("common", sums.get("common") + addedChance);
            }  
        }
        
        if (!custom.isEmpty()) {
            double mult = lootConfig.getDouble("grindstone.custom-multiplier");
            for (Pair<String, Integer> ev : custom) {
                String cench = ev.getLeft();
                String group = AEAPI.getGroup(cench).toLowerCase();
                double addedChance = 100 * ev.getRight() * mult / AEAPI.getHighestEnchantmentLevel(cench);
                sums.putIfAbsent(group, 0.0);
                sums.put(group, sums.get(group) + 100 + addedChance);
            }  
        }

        // Get AuraSkills Disenchanter bonus
        AuraSkillsApi auraSkills = AuraSkillsApi.get();
        SkillsUser user = auraSkills.getUser(player.getUniqueId());
        int disenchanterLvl = user.getAbilityLevel(Abilities.DISENCHANTER);
        double extra = 0;
        if (disenchanterLvl > 0) {
            Ability disenchanter = auraSkills.getGlobalRegistry().getAbility(NamespacedId.fromDefault("disenchanter"));
            extra = disenchanter.getValue(disenchanterLvl);
        }
        
        for (Entry<String, Double> sum : sums.entrySet()) {
            ItemStack dust = plugin.getItems().getItem(sum.getKey() + "dust");
            if (dust == null) {
                continue;
            }
            
            double finalChance = sum.getValue() + extra;
            int amt = (int) Math.floor(finalChance / 100);
            double remainder = finalChance - (amt * 100);
            if (rand.nextDouble() < remainder / 100) {
                amt++;
            }
            
            dust.setAmount(amt);
            player.getWorld().dropItem(location.toCenterLocation(), dust);
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
        e.getInventoryHolder();
        Bukkit.getLogger().info("Loot generated, loot: " + loot);

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
            
            randomlyEnchant(gear, getMaxLevel(group));
        }
    }
    
    /**
     * Given a location, check which rarity group it corresponds to
     * 
     * @param loc
     * @return -1 if group not found
     */
    private int getGroup(Location loc) {
        int dist = (int) Utils.getDistanceFromSpawn(loc);
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
     * Puts a random compatible custom enchantment of the rarity on the item.
     * Iterates from the group provided down until group = 1
     * If no compatible enchantments are found, nothing happens.
     * If the item is an enchanted book, replaces with a random ench book of this group.
     * 
     * @param item
     * @param group
     */
    private void randomlyEnchant(ItemStack item, int group) {
        for (int i = group; i > 1; i--) {
            if (randomEnchantHelper(item, i)) {
                return;
            }
        }
    }
    
    private boolean randomEnchantHelper(ItemStack item, int group) {
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
