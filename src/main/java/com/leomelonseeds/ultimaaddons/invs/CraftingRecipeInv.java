package com.leomelonseeds.ultimaaddons.invs;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class CraftingRecipeInv extends UAInventory {
    
    private CraftingRecipe recipe;
    private Player player;
    
    public CraftingRecipeInv(CraftingRecipe recipe, Player player) {
        super(player, InventoryType.WORKBENCH, "Click any item to exit");
        this.player = player;
        this.recipe = recipe;
    }

    @Override
    public void updateInventory() {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shaped = (ShapedRecipe) recipe;
            String[] shape = shaped.getShape();
            Map<Character, RecipeChoice> choicemap = shaped.getChoiceMap();
            
            for (int i = 0; i < shape.length; i++) {
                String cs = shape[i];
                for (int j = 0; j < cs.length(); j++) {
                    char c = cs.charAt(j);
                    int slot = 3 * i + j + 1;
                    inv.setItem(slot, parseChoice(choicemap.get(c)));
                }
            }
        } else {
            ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
            List<RecipeChoice> choicelist = shapeless.getChoiceList();
            for (int i = 0; i < choicelist.size(); i++) {
                inv.setItem(i + 1, parseChoice(choicelist.get(i)));
            }
        }
        
        inv.setItem(0, recipe.getResult());
    }

    // Go back to main if click
    @Override
    public void registerClick(int slot, ClickType type) {
        if (inv.getItem(slot) != null) {
            new RecipeList(player);
        }
    }
    
    // Get recipe choice from a recipe choice
    // Separate method because getItemStack might get removed
    @SuppressWarnings("deprecation")
    private ItemStack parseChoice(RecipeChoice choice) {
        if (choice == null) {
            return null;
        }
        
        return choice.getItemStack();
    }
}
