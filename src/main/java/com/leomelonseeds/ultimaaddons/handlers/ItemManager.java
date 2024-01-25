package com.leomelonseeds.ultimaaddons.handlers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class ItemManager {
    
    private UltimaAddons plugin;
    private Map<String, ItemStack> items;
    private Map<NamespacedKey, CraftingRecipe> recipes;
    
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
        ConfigurationSection sec = UltimaAddons.getPlugin().getConfig().getConfigurationSection("items");
        for (String key : sec.getKeys(false)) {
            try {
                ItemStack i = Utils.createItem(sec.getConfigurationSection(key));
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

}
