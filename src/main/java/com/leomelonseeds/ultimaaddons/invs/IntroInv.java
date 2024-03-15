package com.leomelonseeds.ultimaaddons.invs;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.handlers.TutorialQuiz;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class IntroInv extends UAInventory {
    
    private Player player;
    
    public IntroInv(Player player) {
        super(player, InventoryType.HOPPER, "Ultima Guide");
        this.player = player;
    }

    @Override
    public void updateInventory() {
        ConfigurationSection sec = UltimaAddons.getPlugin().getConfig().getConfigurationSection("introgui");
        for (String key : sec.getKeys(false)) {
            ItemStack i = Utils.createItem(sec.getConfigurationSection(key));
            inv.setItem(sec.getInt(key + ".slot"), i);
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        ItemStack item = inv.getItem(slot);
        if (item == null) {
            return;
        }
        
        if (item.getType() == Material.WRITTEN_BOOK) {
            player.openBook(item);
            return;
        }
        
        if (item.getType() == Material.DIAMOND) {
            new TutorialQuiz(player);
            inv.close();
        }
    }

}
