package com.leomelonseeds.ultimaaddons.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class UAGive implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (args.length < 2) {
            CommandUtils.sendErrorMsg(sender, "Usage: /ugive [player] [item] <amount>");
            return true;
        }
        
        Player p = Bukkit.getPlayer(args[0]);
        if (p == null) {
            CommandUtils.sendErrorMsg(sender, "Player was not found!");
            return true;
        }
        
        FileConfiguration config = UltimaAddons.getPlugin().getConfig();
        if (!config.getConfigurationSection("items").getKeys(false).contains(args[1])) {
            CommandUtils.sendErrorMsg(sender, "Item was not found!");
            return true;
        }
        
        try {
            ItemStack i = Utils.createItem(config.getConfigurationSection("items." + args[1]));
            int amt = 1;
            if (args.length == 3) {
                amt = Integer.parseInt(args[2]);
            }
            
            i.setAmount(amt == 0 ? 1 : amt);
            p.getInventory().addItem(i);
        } catch (Exception e) {
            CommandUtils.sendErrorMsg(sender, "Error with creating item!");
            return true;
        }
        
        return true;
    }

}
