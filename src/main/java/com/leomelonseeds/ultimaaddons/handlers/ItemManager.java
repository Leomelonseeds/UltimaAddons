package com.leomelonseeds.ultimaaddons.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.leomelonseeds.ultimaaddons.ability.Ability;
import com.leomelonseeds.ultimaaddons.ability.BlazeFireball;
import com.leomelonseeds.ultimaaddons.ability.Blink;
import com.leomelonseeds.ultimaaddons.ability.DualWield;
import com.leomelonseeds.ultimaaddons.ability.Lifesteal;
import com.leomelonseeds.ultimaaddons.ability.Shiruken;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.api.AEAPI;
import net.kyori.adventure.text.Component;

public class ItemManager implements Listener {

    ConfigurationSection itemConfig;
    private UltimaAddons plugin;
    private Map<String, ItemStack> items;
    private Map<NamespacedKey, CraftingRecipe> recipes;
    private AbilityManager abilityManager;
    private ArmorSetManager armorManager;

    public ItemManager(UltimaAddons plugin) {
        this.plugin = plugin;
        items = new HashMap<>();
        recipes = new HashMap<>();
        abilityManager = new AbilityManager();
        armorManager = new ArmorSetManager();
        loadItems();
    }

    /**
     * Load all items and recipes from config,
     * adding abilities as necessary.
     * Recipes are currently hard-coded.
     */
    public void loadItems() {
        // Clear current stuff
        items.clear();
        abilityManager.clearAbilities();
        armorManager.clearAttrs();

        // Add all config items
        itemConfig = UltimaAddons.getPlugin().getConfig().getConfigurationSection("items");
        for (String key : itemConfig.getKeys(false)) {
            try {
                ConfigurationSection sec = itemConfig.getConfigurationSection(key);
                if (sec.contains("uattribute")) {
                    continue;
                }
                
                ItemStack i = Utils.createItem(sec);
                items.put(key, i);

                // Update ability if exists
                if (!sec.contains("ability")) {
                    continue;
                }

                ConfigurationSection asec = sec.getConfigurationSection("ability");
                Ability a = null;
                switch (key) {
                    case "blazesword":
                        a = new BlazeFireball(asec.getInt("yield"), asec.getInt("randomness"));
                        break;
                    case "orcus":
                        a = new Lifesteal(asec.getInt("percent"));
                        break;
                    case "shadowblade":
                        a = new Blink(asec.getInt("distance"));
                        break;
                    case "oxtailsaber":
                        a = new DualWield(key);
                        break;
                    case "shiruken":
                        a = new Shiruken(asec.getDouble("speed"), asec.getDouble("damage"), asec.getInt("ticks"));
                        break;
                }

                if (a == null) {
                    continue;
                }

                a.setCooldown(asec.getInt("cooldown"));
                a.setDisplayName(asec.getString("name"));
                abilityManager.addAbility(key, a);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Something went wrong trying to create item " + key + ":");
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
    public Collection<CraftingRecipe> getRecipes() {
        return Collections.unmodifiableCollection(recipes.values());
    }

    /**
     * @return an unmodifiable collection of all custom items
     */
    public Collection<ItemStack> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    /**
     * @return an unmodifiable collection of all custom items names
     */
    public Collection<String> getItemNames() {
        return Collections.unmodifiableCollection(items.keySet());
    }

    private void addRecipe(CraftingRecipe r) {
        Bukkit.addRecipe(r);
        recipes.put(r.getKey(), r);
    }

    public AbilityManager getAbilities() {
        return abilityManager;
    }
    
    public ArmorSetManager getArmor() {
        return armorManager;
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
        // 1: Update everything (only use for items that shouldn't be edited)
        // 2: Update attributes, and all below
        // 3: Update type, lore, and all below
        // 4: Update custom model data only
        ItemMeta curMeta = cur.getItemMeta();
        ItemMeta actualMeta = actual.getItemMeta();
        switch (itemConfig.getInt(path)) {
            case 1:
                cur.setType(actual.getType());
                cur.setItemMeta(actualMeta);
                break;
            case 2:
                curMeta.getAttributeModifiers().keySet().forEach(a -> curMeta.removeAttributeModifier(a));
                actualMeta.getAttributeModifiers().entries().forEach(a -> curMeta.addAttributeModifier(a.getKey(), a.getValue()));
            case 3:
                cur.setType(actual.getType());

                // Update lore without removing enchantments
                List<Component> updated = new ArrayList<>();
                for (Component c : curMeta.lore()) {
                    if (!AEAPI.isEnchantLine(Utils.toPlain(c))) {
                        break;
                    }
                    updated.add(c);
                }

                updated.addAll(actualMeta.lore());
                curMeta.lore(updated);
            case 4:
                if (itemConfig.contains(data + ".custom-model-data")) {
                    curMeta.setCustomModelData(itemConfig.getInt(data + ".custom-model-data"));
                }
                cur.setItemMeta(curMeta);
        }
    }

}
