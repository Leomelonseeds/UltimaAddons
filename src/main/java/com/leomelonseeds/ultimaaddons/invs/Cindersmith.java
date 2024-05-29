package com.leomelonseeds.ultimaaddons.invs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.objects.EnchantResult;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.api.AEAPI;
import net.kyori.adventure.text.Component;

public class Cindersmith extends UAInventory {
    
    private static List<Integer> reservedSlots = List.of(2, 3, 11, 12, 15, 38, 40, 42); 
    private static List<Integer> resultSlots = List.of(38, 40, 42);
    private static List<String> tiers = List.of("common", "uncommon", "rare", "epic", "legendary");
    private static Map<UUID, Pair<Long, Integer>> data;
    
    private UltimaAddons plugin;
    private UUID uuid;
    private EnchantResult[] results;
    
    public Cindersmith(Player player) {
        super(player, 54, "Cindersmith");
        this.plugin = UltimaAddons.getPlugin();
        this.uuid = player.getUniqueId();
        this.results = new EnchantResult[3];
        
        if (data == null) {
            data = new HashMap<>();
        }
        
        if (!data.containsKey(uuid)) {
            data.put(uuid, Pair.of(System.currentTimeMillis(), 0));
        }
    }

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
        
        // Gear indicator icon
        ItemStack gearInfo = Utils.createItem(sec.getConfigurationSection("gear"));
        inv.setItem(2, gearInfo);
        
        // Dust indicator icon
        ItemStack dustInfo = Utils.createItem(sec.getConfigurationSection("dust"));
        inv.setItem(3, dustInfo);
        
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
            
            // TODO: Calculate costs and maximum level in getResults
            // TODO: Figure out what to do for enchants of different max levels
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        // TODO Auto-generated method stub
        
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
    
    private void getResults(ItemStack gear, int dustAmount, String rarity) {
        // TODO
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
}
