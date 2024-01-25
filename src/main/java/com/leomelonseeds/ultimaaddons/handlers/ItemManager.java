package com.leomelonseeds.ultimaaddons.handlers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class ItemManager implements Listener {
    
    private UltimaAddons plugin;
    private Map<String, ItemStack> items;
    private Map<NamespacedKey, CraftingRecipe> recipes;
    ConfigurationSection itemConfig;
    
    public ItemManager(UltimaAddons plugin) {
        this.plugin = plugin;
        items = new HashMap<>();
        recipes = new HashMap<>();
        loadItems();
    }
    
    /**
     * Load all items and recipes from config
     * Recipes are currently hard-coded
     */
    public void loadItems() {
        // Add all config items
        items.clear();
        itemConfig = UltimaAddons.getPlugin().getConfig().getConfigurationSection("items");
        for (String key : itemConfig.getKeys(false)) {
            try {
                ItemStack i = Utils.createItem(itemConfig.getConfigurationSection(key));
                items.put(key, i);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Something went wrong trying to create item " + key + ":");
                e.printStackTrace();
            }
        }
        
        // Remove currently loaded recipes
        for (NamespacedKey key : recipes.keySet()) {
            Bukkit.removeRecipe(key);
        }
        
        // Add new, hardcoded recipes
        // All namespacedkeys follow the format:
        // [lowercase item result (enum if mc material, key if ua item)]_index
        // index starts at 0, add more if more crafting recipes for a single item added
        
        // Diamonds -> Chips
        ItemStack dchipresult = getItem("dchip");
        dchipresult.setAmount(9);
        ShapelessRecipe dchip = new ShapelessRecipe(new NamespacedKey(plugin, "dchip_0"), dchipresult);
        dchip.addIngredient(1, Material.DIAMOND);
        addRecipe(dchip);
        
        // Chips -> Diamonds
        ShapedRecipe chipToDiamond = new ShapedRecipe(new NamespacedKey(plugin, "diamond_0"), new ItemStack(Material.DIAMOND));
        chipToDiamond.shape("CCC", "CCC", "CCC");
        chipToDiamond.setIngredient('C', getItem("dchip"));
        addRecipe(chipToDiamond);
    }
    
    /**
     * Return a copy of the specified itemstack
     * 
     * @param key
     * @return
     */
    public ItemStack getItem(String key) {
        ItemStack res = items.get(key);
        if (res == null) {
            return null;
        }
        
        return new ItemStack(res);
    }
    
    /**
     * @return an unmodifiable collection of all custom recipes
     */
    public Collection<CraftingRecipe> getRecipes(){
        return Collections.unmodifiableCollection(recipes.values());
    }
    
    /**
     * @return an unmodifiable collection of all custom items
     */
    public Collection<ItemStack> getItems(){
        return Collections.unmodifiableCollection(items.values());
    }
    
    private void addRecipe(CraftingRecipe r) {
        Bukkit.addRecipe(r);
        recipes.put(r.getKey(), r);
    }
    
    // Stop custom items being used for non-custom recipes
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent e) {
        Recipe r = e.getRecipe();
        if (r == null) {
            return;
        }

        if (!(r instanceof CraftingRecipe)) {
            return;
        }
        
        // Return if this is a custom recipe
        CraftingRecipe cr = (CraftingRecipe) r;
        if (recipes.containsKey(cr.getKey())) {
            return;
        }

        // Get all ingredients and results
        // Kill result if any item has no item ID
        CraftingInventory ci = e.getInventory();
        if (Arrays.asList(ci.getContents()).stream().anyMatch(i -> Utils.getItemID(i) != null)) {
            e.getInventory().setResult(null);
        }
    }
    
    // Update custom items if necessary
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        ItemStack cur = e.getCurrentItem();
        if (cur == null) {
            return;
        }
        
        String data = Utils.getItemID(cur);
        if (data == null) {
            return;
        }
        
        if (!items.containsKey(data)) {
            Player p = (Player) e.getWhoClicked();
            p.sendMessage(Utils.toComponent("&cThe custom item '" + data + "' no longer exists, and may be automatically removed in the future. "
                    + "Please contact an admin if you have any further questions."));
            return;
        }

        // Make sure item has the update key
        String path = data + ".update";
        if (!itemConfig.contains(path)) {
            return;
        }

        // No need if item already corresponds
        ItemStack actual = getItem(data);
        if (cur.isSimilar(actual)) {
            return;
        }

        // UPDATE MODES:
        // 0 (default): No updating
        // 1: Update everything
        // 2: Update type, lore, and custom model data only
        switch (itemConfig.getInt(path)) {
        case 1:
            cur.setType(actual.getType());
            cur.setItemMeta(actual.getItemMeta());
            break;
        case 2:
            cur.setType(actual.getType());
            ItemMeta curMeta = cur.getItemMeta();
            curMeta.lore(actual.getItemMeta().lore());
            if (itemConfig.contains(data + ".custom-model-data")) {
                curMeta.setCustomModelData(itemConfig.getInt(data + ".custom-model-data"));
            }
            cur.setItemMeta(curMeta);
            break;
        }
    }

}
