package com.leomelonseeds.ultimaaddons.handlers.item;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class RecipeManager implements Listener {
    
    private ItemManager im;
    private UltimaAddons plugin;
    private Map<NamespacedKey, Pair<CraftingRecipe, String>> recipes;
    
    public RecipeManager(ItemManager im, UltimaAddons plugin) {
        this.im = im;
        this.plugin = plugin;
        recipes = new HashMap<>();
        loadRecipes();
    }
    
    public void reload() {
        // Remove currently loaded recipes
        for (NamespacedKey key : recipes.keySet()) {
            Bukkit.removeRecipe(key);
        }
        recipes.clear();
        loadRecipes();
    }
    
    /** Reload all recipes, which are currently hardcoded */
    private void loadRecipes() {
        // All namespacedkeys follow the format:
        // [lowercase item result (enum if mc material, key if ua item)]_index
        // index starts at 0, add more if more crafting recipes for a single item added

        // Diamonds -> Chips
        ItemStack dchipresult = im.getItem("dchip");
        dchipresult.setAmount(9);
        ShapelessRecipe dchip = new ShapelessRecipe(new NamespacedKey(plugin, "dchip_0"), dchipresult);
        dchip.addIngredient(1, Material.DIAMOND);
        addRecipe(dchip);

        // Chips -> Diamonds
        ShapedRecipe chipToDiamond = new ShapedRecipe(new NamespacedKey(plugin, "diamond_0"), new ItemStack(Material.DIAMOND));
        chipToDiamond.shape("CCC", "CCC", "CCC");
        chipToDiamond.setIngredient('C', im.getItem("dchip"));
        addRecipe(chipToDiamond);
        
        // Bundle
        ShapedRecipe bundle = new ShapedRecipe(new NamespacedKey(plugin, "bundle_0"), new ItemStack(Material.BUNDLE));
        bundle.shape("SRS", "RXR", "RRR");
        bundle.setIngredient('S', Material.STRING);
        bundle.setIngredient('R', Material.LEATHER);
        addRecipe(bundle);
        
        // Radiant shard
        ShapedRecipe rshard = new ShapedRecipe(new NamespacedKey(plugin, "radiantshard_0"), im.getItem("radiantshard"));
        rshard.shape("SPS", "QLQ", "SPS");
        rshard.setIngredient('S', Material.AMETHYST_CLUSTER);
        rshard.setIngredient('P', Material.PRISMARINE_SHARD);
        rshard.setIngredient('Q', Material.QUARTZ);
        ItemStack lh = new ItemStack(Material.LINGERING_POTION);
        PotionMeta pmeta = (PotionMeta) lh.getItemMeta();
        pmeta.setBasePotionType(PotionType.STRONG_HEALING);
        lh.setItemMeta(pmeta);
        rshard.setIngredient('L', lh);
        addRecipe(rshard);
        
        // Armor sets
        String[] armor = new String[] {"helmet", "chestplate", "leggings", "boots"};
        Map<String, ItemStack> ingredients = new HashMap<>();
        ingredients.put("obsidian", im.getItem("obsidianingot"));
        ingredients.put("shard", im.getItem("radiantshard"));
        ingredients.put("infused", im.getItem("infusedingot"));
        ingredients.put("mithril", im.getItem("mithrilingot"));
        for (String set : ingredients.keySet()) {
            for (int i = 0; i < 4; i++) {
                String a = set + "." + armor[i];
                ShapedRecipe sr = new ShapedRecipe(new NamespacedKey(plugin, a + "_0"), im.getItem(a));
                switch (i) {
                    case 0:
                        sr.shape("III", "IXI");
                        break;
                    case 1:
                        sr.shape("IXI", "III", "III");
                        break;
                    case 2:
                        sr.shape("III", "IXI", "IXI");
                        break;
                    default:
                        sr.shape("IXI", "IXI");
                        break;
                }
                sr.setIngredient('I', ingredients.get(set));
                addRecipe(sr, "ua.recipe." + set + "armor");
            } 
        }
        
        // Totems
        // Totem of Warping
        String stot = TotemManager.TOTEM_INDICATOR + ".";
        String sunsettot = stot + "unset";
        ItemStack iunsettot = im.getItem(sunsettot);
        ShapedRecipe unsettot = new ShapedRecipe(new NamespacedKey(plugin, sunsettot + "_0"), iunsettot);
        unsettot.shape("XCX", "XTX", "XEX");
        unsettot.setIngredient('C', Material.CHORUS_FRUIT);
        unsettot.setIngredient('T', Material.TOTEM_OF_UNDYING);
        unsettot.setIngredient('E', Material.ENDER_EYE);
        addRecipe(unsettot);
        
        // Kingdom home, respawn point, and returning all use basic materials
        shapelessTotemHelper(iunsettot, stot + TotemType.KHOME, Material.WHITE_BED, null);
        shapelessTotemHelper(iunsettot, stot + TotemType.DEATH, Material.CALIBRATED_SCULK_SENSOR, null);
        shapelessTotemHelper(iunsettot, stot + TotemType.HOME, Material.COMPASS, null);
        
        // Recall and player both use recipes with EXAMPLE ITEMS
        shapelessTotemHelper(iunsettot, stot + TotemType.LODESTONE, null, im.getItem("exlodestone"));
        shapelessTotemHelper(iunsettot, stot + TotemType.PLAYER, null, im.getItem("exbook"));
        
        // Totem duplication
        ItemStack totemDupeAny = im.getItem("exampleanytotem");
        ItemStack totemDupeAnyRes = totemDupeAny.clone();
        totemDupeAnyRes.setAmount(2);
        ShapelessRecipe totemDupe = new ShapelessRecipe(new NamespacedKey(plugin, "totemdupe_0"), totemDupeAnyRes);
        totemDupe.addIngredient(iunsettot);
        totemDupe.addIngredient(totemDupeAny);
        addRecipe(totemDupe);
    }
    
    private void shapelessTotemHelper(ItemStack unset, String name, Material m, ItemStack i) {
        ShapelessRecipe sr = new ShapelessRecipe(new NamespacedKey(plugin, name + "_0"), im.getItem(name));
        sr.addIngredient(unset);
        if (i != null) {
            sr.addIngredient(i);
        } else {
            sr.addIngredient(m);
        }
        addRecipe(sr);
    }

    /**
     * @return an unmodifiable map of all custom recipes
     */
    public Map<NamespacedKey, Pair<CraftingRecipe, String>> getRecipes() {
        return Collections.unmodifiableMap(recipes);
    }
    

    private void addRecipe(CraftingRecipe r) {
        addRecipe(r, "");
    }

    private void addRecipe(CraftingRecipe r, String permission) {
        NamespacedKey rkey = r.getKey();
        if (Bukkit.getRecipe(rkey) != null) {
            Bukkit.removeRecipe(rkey);
        }
        Bukkit.addRecipe(r);
        recipes.put(r.getKey(), ImmutablePair.of(r, permission));
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

        // If this is a custom recipe, check permission
        CraftingRecipe cr = (CraftingRecipe) r;
        if (recipes.containsKey(cr.getKey())) {
            Player p = (Player) e.getView().getPlayer();
            String perm = recipes.get(cr.getKey()).getRight();
            if (!perm.isEmpty() && !p.hasPermission(perm)) {
                e.getInventory().setResult(null);
            }
            return;
        }

        // Get all ingredients and results
        // Kill result if any item has no item ID
        CraftingInventory ci = e.getInventory();
        if (Arrays.asList(ci.getContents()).stream().anyMatch(i -> Utils.getItemID(i) != null)) {
            ci.setResult(null);
        }
    }
    
    // Stop custom items being used for smithing table recipes
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSmithing(PrepareSmithingEvent e) {
        if (e.getResult() == null || e.getResult().getType() == Material.AIR) {
            return;
        }
        
        // Allow if smithing table contains no custom items
        SmithingInventory si = e.getInventory();
        if (!Arrays.asList(si.getContents()).stream().anyMatch(i -> Utils.getItemID(i) != null)) {
            return;
        }

        // Only allow if template is not netherite template
        ItemStack template = si.getInputTemplate();
        if (template != null && template.getType() == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
            e.setResult(null);
        }
    }
    
    // Stop custom items being used for anvil table recipes
    // This is mainly to stop repairs of like obsidian armor
    // using leather, etc etc
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvil(PrepareAnvilEvent e) {
        if (e.getResult() == null || e.getResult().getType() == Material.AIR) {
            return;
        }
;
        // Allow if anvil does not contain custom items
        AnvilInventory ai = e.getInventory();
        if (!Arrays.asList(ai.getContents()).stream().anyMatch(i -> Utils.getItemID(i) != null)) {
            return;
        }

        // Only allow if middle item is ench book or instance of damageable
        ItemStack second = ai.getSecondItem();
        if (second == null) {
            return;
        }

        if (second.getType() == Material.ENCHANTED_BOOK) {
            return;
        }

        ItemMeta meta = second.getItemMeta();
        if (meta != null && meta instanceof Damageable) {
            return;
        }

        e.setResult(null);
    }
}
