package com.leomelonseeds.ultimaaddons.invs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.objects.enchant.EnchantResult;
import com.leomelonseeds.ultimaaddons.objects.enchant.UCustomEnchant;
import com.leomelonseeds.ultimaaddons.objects.enchant.UEnchantment;
import com.leomelonseeds.ultimaaddons.objects.enchant.UVanillaEnchant;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.api.AEAPI;
import net.kyori.adventure.text.Component;

public class Cindersmith extends UAInventory {
    
    private static final List<Integer> reservedSlots = List.of(2, 3, 11, 12, 15, 38, 40, 42); 
    private static final List<Integer> resultSlots = List.of(38, 40, 42);
    private static final List<String> tiers = List.of("common", "uncommon", "rare", "epic", "legendary");
    
    private static Map<UUID, Pair<Long, Integer>> data = new HashMap<>();
    private static List<Enchantment> vanillaEnchants;
    
    private UltimaAddons plugin;
    private UUID uuid;
    private EnchantResult[] results;
    private BukkitTask displayRevolver;
    
    public Cindersmith(Player player) {
        super(player, 54, "Cindersmith");
        this.plugin = UltimaAddons.getPlugin();
        this.uuid = player.getUniqueId();
        this.results = new EnchantResult[3];
        
        if (!data.containsKey(uuid)) {
            data.put(uuid, Pair.of(System.currentTimeMillis(), 0));
        }
        
        if (vanillaEnchants == null) {
            vanillaEnchants = new ArrayList<>();
            Registry.ENCHANTMENT.forEach(ve -> {
                if (!ve.isTreasure() && !ve.isCursed()) {
                    vanillaEnchants.add(ve);
                }
            });
        }
    }

    // Consider making the gear/dust indicator cycle in the future
    @Override
    public void updateInventory() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("enchantgui");
        
        // Pane fill
        for (int i = 0; i < 54; i++) {
            if (reservedSlots.contains(i)) {
                continue;
            }
            
            Material mat;
            if (i % 9 == 0 || i % 9 == 8) {
                mat = Material.BLACK_STAINED_GLASS_PANE;
            } else if (i < 27) {
                mat = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            } else {
                mat = Material.PINK_STAINED_GLASS_PANE;
            }
            
            ItemStack pane = new ItemStack(mat);
            ItemMeta pmeta = pane.getItemMeta();
            pmeta.displayName(Utils.toComponent(""));
            pane.setItemMeta(pmeta);
            inv.setItem(i, pane);
        }
        
        // Revolving gear/dust indicators
        if (displayRevolver == null) {
            // Gear indicator icon
            ItemStack gearInfo = Utils.createItem(sec.getConfigurationSection("gear"));
            gearInfo.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            inv.setItem(2, gearInfo);
            
            // Dust indicator icon
            ItemStack dustInfo = Utils.createItem(sec.getConfigurationSection("dust"));
            inv.setItem(3, dustInfo);
            
            // Revolve 
            List<String> gearMaterials = sec.getStringList("gear.materials");
            List<String> dustMaterials = sec.getStringList("dust.materials");
            displayRevolver = new BukkitRunnable() {
                
                int iter = 100;
                
                @Override
                public void run() {
                    ItemStack gearInfoNew = gearInfo.withType(Material.valueOf(gearMaterials.get(iter % gearMaterials.size())));
                    ItemStack dustInfoNew = dustInfo.withType(Material.valueOf(dustMaterials.get(iter % dustMaterials.size())));
                    inv.setItem(2, gearInfoNew);
                    inv.setItem(3, dustInfoNew);
                    iter++;
                }
            }.runTaskTimerAsynchronously(plugin, 20, 20);
        }
        
        // Reroll button
        ItemStack reroll = Utils.createItem(sec.getConfigurationSection("reroll"));
        int rerollCost = getRerollCost();
        reroll.setAmount(Math.min(rerollCost, 64));
        replaceMeta(reroll, Map.of("%cost%", rerollCost + ""));
        inv.setItem(15, reroll);

        // States:
        // None ("none"): No items in gear slot, less than 2 enchanted dust in dust slot
        // Already Exists ("already"): Already enchanted with this item
        // NA ("na"): No enchants available for gear item and dust combo. Can also
        // occur if less than 3 enchants are available for some combo.
        ItemStack gear = inv.getItem(11);
        ItemStack dust = inv.getItem(12);
        String rarity = checkItems(gear, dust);
        if (rarity == null) {
            ItemStack none = Utils.createItem(sec.getConfigurationSection("locked-none"));
            setResults(none, none, none);
            return;
        }
        
        if (alreadyEnchanted(gear, rarity)) {
            ItemStack already = Utils.createItem(sec.getConfigurationSection("locked-already"));
            setResults(already, already, already);
            return;
        }
        
        getResults(gear, dust.getAmount(), rarity);
        for (int i = 0; i < 3; i++) {
            EnchantResult er = results[i];
            ItemStack display;
            int slot = resultSlots.get(i);
            if (er == null) {
                display = Utils.createItem(sec.getConfigurationSection("locked-na"));
                inv.setItem(slot, display);
                continue;
            }
            
            display = Utils.createItem(sec.getConfigurationSection("ench"));
            replaceMeta(display, Map.of("%enchant%", er.getDisplayName(), "%cost%", er.getCost() + ""));
            inv.setItem(slot, display);
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        if (isPlaceableSlot(slot)) {
            asyncUpdate();
            return;
        }
        
        // TODO: Reroll/enchant/get info mechanics
    }
    
    /**
     * Check if the given click from the ICE should be allowed.
     * Updates the inventory if a shift click to the top inventory
     * is detected.
     * 
     * @param e
     */
    public boolean allowClick(InventoryClickEvent e) {
        // Allow clicking top slot of inventory
        if (e.getClickedInventory().equals(e.getView().getTopInventory())){
            if (isPlaceableSlot(e.getSlot())) {
                return true;
            } else {
                return false;
            }
        }
        
        // Update on successful shift click bottom to top
        if (e.getClickedInventory().equals(e.getView().getBottomInventory()) && 
                e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && 
                e.isShiftClick() && 
                e.getResult() == Result.ALLOW) {
            asyncUpdate();
        }
        
        return true;
    }
    
    /**
     * Cancel revolving task and give any remaining items
     * back to the player
     * 
     * @param player
     */
    public void onClose(Player player) {
        if (!player.getUniqueId().equals(uuid)) {
            return;
        }
        
        for (ItemStack item : new ItemStack[] {inv.getItem(11), inv.getItem(12)}) {
            if (item == null) {
                continue;
            }
            
            Map<Integer, ItemStack> drops = player.getInventory().addItem(item);
            for (ItemStack drop : drops.values()) {
                Item ie = player.getWorld().dropItem(player.getLocation(), drop);
                ie.setOwner(uuid);
            }
        }
        
        displayRevolver.cancel();
    }
    
    /**
     * Fill results with the appropriate EnchantResults.
     * This method is guaranteed to return the same results
     * for a certain seed, gear, and dust rarity/amount combo.
     * 
     * To choose enchantments, 3 random doubles are generated.
     * The index of the enchantment is the size of the list
     * multiplied by the double, rounded down.
     * 
     * TODO: Determine incompatible enchants by loading up AE config
     * TODO: Cost formula
     * 
     * Obtainability:
     * Max level 3 and above: 2 enchanted dust
     * Max level 2: 3 enchanted dust
     * Max level 1: 4 enchanted dust
     * Max level of enchantment to appear >= -1 * # dust + 5
     * 
     * Additional Levels:
     * Each additional enchant has a % chance of increasing the level of each enchantment.
     * Max level 5 and above: 100%
     * Max level 4: 75%
     * Max level 3: 50%
     * Max level 2: 33% (on average, 3 dust = full levelup)
     * Function of x remaining dust, and y remaining levels
     * % = Min(remaining levels/(6 - min # dust for ench), 1)
     * 
     * 
     * 
     * 
     * @param gear
     * @param dustAmount
     * @param rarity
     */
    private void getResults(ItemStack gear, int dustAmount, String rarity) {
        // Reset random seed
        long seed = data.get(uuid).getLeft();
        Random rand = new Random(seed);
        
        // Fetch all possible enchantments of this rarity
        List<UEnchantment> enchants = new ArrayList<>();
        if (rarity.equals("common")) {
            vanillaEnchants.forEach(ve -> enchants.add(new UVanillaEnchant(ve)));
        } else {
            AEAPI.getEnchantmentsByGroup(rarity).forEach(ce -> enchants.add(new UCustomEnchant(ce)));
        }
        
        
    }
    
    /**
     * Add the specified placeholders (key -> value) to the name
     * and lore of the item
     * 
     * @param item
     * @param placeholders
     */
    private void replaceMeta(ItemStack item, Map<String, String> placeholders) {
        ItemMeta meta = item.getItemMeta();
        List<Component> display = meta.lore();
        display.add(meta.displayName());
        List<Component> res = new ArrayList<>();
        for (Component c : display) {
            String line = Utils.toPlain(c);
            for (Entry<String, String> ph : placeholders.entrySet()) {
                String replacement = line.replace(ph.getKey(), ph.getValue());
                res.add(Utils.toComponent(replacement));
            }
        }
        
        meta.displayName(res.remove(res.size() - 1));
        meta.lore(res);
        item.setItemMeta(meta);
    }
    
    /**
     * Check if there is an item in the gear slot, and more than 2
     * enchanted dust in the dust slot
     * 
     * @return null if none, the rarity of the dust if yes
     */
    private String checkItems(ItemStack gear, ItemStack dust) {
        if (gear == null || dust == null || dust.getAmount() < 2) {
            return null;
        }
        
        String id = Utils.getItemID(dust);
        if (id == null) {
            return null;
        }
        
        String ret = id.replace("dust", "");
        if (!tiers.contains(ret)) {
            return null;
        }
        
        return ret;
    }
    
    /**
     * Check if the itemstack already has an enchant of rarity
     * 
     * @param gear
     * @param rarity
     * @return true if it does
     */
    private boolean alreadyEnchanted(ItemStack gear, String rarity) {
        // Cannot enchant enchanted books further
        if (gear.getType() == Material.ENCHANTED_BOOK) {
            return true;
        }
        
        if (rarity.equals("common")) {
            return !gear.getEnchantments().isEmpty();
        }
        
        for (String ench : AEAPI.getEnchantmentsOnItem(gear).keySet()) {
            String group = AEAPI.getGroup(ench).toLowerCase();
            if (group.equals(rarity)) {
                return true;
            }
        }
        
        return false;
    }
    
    private int getRerollCost() {
        int rerolls = data.get(uuid).getRight();
        return (int) Math.pow(2, rerolls);
    }
    
    private void setResults(ItemStack one, ItemStack two, ItemStack three) {
        inv.setItem(resultSlots.get(0), one);
        inv.setItem(resultSlots.get(1), two);
        inv.setItem(resultSlots.get(2), three);
    }
    
    private EnchantResult getEnchantResult(int slot) {
        int index = resultSlots.indexOf(slot);
        return index == -1 ? null : results[index];
    }
    
    private boolean isPlaceableSlot(int slot) {
        return slot == 11 || slot == 12;
    }
    
    private void asyncUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> updateInventory());
    }
}
