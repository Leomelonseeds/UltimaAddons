package com.leomelonseeds.ultimaaddons.objects.enchant;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface UEnchantment {
    
    public int getMaxLevel();
    
    public void applyEnchant(ItemStack item, int level);
    
    public String getDisplayName(int level);
    
    public boolean isCompatible(ItemStack item);
    
    public void getInfo(Player player);

}
