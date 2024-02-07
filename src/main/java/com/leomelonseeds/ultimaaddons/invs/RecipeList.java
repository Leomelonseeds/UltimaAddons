package com.leomelonseeds.ultimaaddons.invs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
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

public class RecipeList extends UAInventory {
    
    private static final int size = 27;
    private static NamespacedKey recipeKey;
    private Map<NamespacedKey, Pair<CraftingRecipe, String>> recipes;
    private UltimaAddons plugin;
    private int page;
    private Player player;
    private ConfigurationSection sec;
    
    public RecipeList(Player player) {
        super(player, size + 9, "Recipes");
        
        this.plugin = UltimaAddons.getPlugin();
        if (recipeKey == null) {
            recipeKey = new NamespacedKey(plugin, "recipegui");
        }

        this.player = player;
        this.page = 0;
        this.recipes = plugin.getItems().getRecipes();
        this.sec = plugin.getConfig().getConfigurationSection("recipegui");
    }

    @Override
    public void updateInventory() {
        inv.clear();
        
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
        List<ItemStack> results = new ArrayList<>();
        recipes.keySet().forEach(k -> keys.add(k));
        keys.sort(Comparator.comparing(NamespacedKey::getKey));
        keys.forEach(k -> {
            // Do not add if player does not have permission for recipe
            Pair<CraftingRecipe, String> pair = recipes.get(k);
            String perm = pair.getRight();
            if (!perm.isEmpty() && !player.hasPermission(pair.getRight())) {
                results.add(Utils.createItem(sec.getConfigurationSection("locked")));
                return;
            }
            
            ItemStack result = new ItemStack(pair.getLeft().getResult()); 
            ItemMeta rmeta = result.getItemMeta();
            rmeta.getPersistentDataContainer().set(recipeKey, PersistentDataType.STRING, k.getKey());
            result.setItemMeta(rmeta);
            results.add(result);
        });

        // Loops through all available slots, and sets recipe items
        for (int i = page * size; i < Math.min(amt, page * size + size); i++) {
            inv.setItem(i % size, results.get(i));
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        ItemStack item = inv.getItem(slot);
        if (item == null) {
            return;
        }
        
        if (item.equals(Utils.createItem(sec.getConfigurationSection("prev")))) {
            page--;
            updateInventory();
            return;
        }
        
        if (item.equals(Utils.createItem(sec.getConfigurationSection("next")))) {
            page++;
            updateInventory();
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        
        if (!meta.getPersistentDataContainer().has(recipeKey)) {
            return;
        }
        
        String key = meta.getPersistentDataContainer().get(recipeKey, PersistentDataType.STRING);
        CraftingRecipe recipe = recipes.get(new NamespacedKey(UltimaAddons.getPlugin(), key)).getLeft();
        if (recipe == null) {
            return;
        }
        
        new CraftingRecipeInv(recipe, player);
    }
}
