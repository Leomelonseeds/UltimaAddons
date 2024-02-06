package com.leomelonseeds.ultimaaddons.invs;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.CraftingRecipe;

import com.leomelonseeds.ultimaaddons.UltimaAddons;

public class Recipes extends UAInventory {
    
    public Recipes(Player player) {
        super(player, 36, "Recipes");
    }

    @Override
    public void updateInventory() {
        Collection<CraftingRecipe> recipes = UltimaAddons.getPlugin().getItems().getRecipes();
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        // TODO Auto-generated method stub

    }
}
