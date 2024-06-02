package com.leomelonseeds.ultimaaddons.objects.enchant;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantResult {
    
    private UEnchantment uench;
    private int level;
    private int cost;
    
    public EnchantResult(UEnchantment uench, int level, int cost) {
        this.uench = uench;
        this.level = level;
        this.cost = cost;
    }

    public ItemStack applyEnchant(ItemStack item) {
        return uench.applyEnchant(item, level);
    }
    
    public String getDisplayName() {
        return uench.getDisplayName(level);
    }
    
    public int getCost() {
        return cost;
    }
    
    public void getInfo(Player player) {
        uench.getInfo(player);
    }
}
