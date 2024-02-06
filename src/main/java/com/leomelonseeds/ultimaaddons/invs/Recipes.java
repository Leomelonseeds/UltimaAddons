package com.leomelonseeds.ultimaaddons.invs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class Recipes extends UAInventory {
    
    private static final int size = 27;
    private static NamespacedKey recipeKey;
    private Map<NamespacedKey, CraftingRecipe> recipes;
    private UltimaAddons plugin;
    private int page;
    
    public Recipes(Player player) {
        super(player, size + 9, "Recipes");
        
        this.plugin = UltimaAddons.getPlugin();
        if (recipeKey == null) {
            recipeKey = new NamespacedKey(plugin, "recipegui");
        }

        this.page = 0;
        this.recipes = plugin.getItems().getRecipes();
    }

    @Override
    public void updateInventory() {
        inv.clear();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("recipegui");
        
        // Epic pagination
        for (int i = size; i < size + 9; i++) {
            inv.setItem(i, Utils.createItem(sec.getConfigurationSection("fill")));
        }
        
        int amt = recipes.size();
        double maxPages = Math.ceil((double) amt / size);
        if (page > 0) {
            inv.setItem(size, Utils.createItem(sec.getConfigurationSection("prev")));
        }
        
        if (page < maxPages - 1) {
            inv.setItem(size + 8, Utils.createItem(sec.getConfigurationSection("next")));
        }

        // Gather all resultant itemstacks
        List<NamespacedKey> keys = new ArrayList<>();
        Queue<ItemStack> results = new LinkedList<>();
        recipes.keySet().forEach(k -> keys.add(k));
        keys.sort(Comparator.comparing(NamespacedKey::getKey));
        keys.forEach(k -> {
           ItemStack result = new ItemStack(recipes.get(k).getResult()); 
           ItemMeta rmeta = result.getItemMeta();
           rmeta.getPersistentDataContainer().set(recipeKey, PersistentDataType.STRING, k.getKey());
           result.setItemMeta(rmeta);
           results.add(result);
        });

        // Loops through all available slots, and sets recipe items
        for (int i = page * size; i < Math.min(amt, page * size + size); i++) {
            inv.setItem(i % size, results.poll());
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        // TODO Auto-generated method stub

    }
}
