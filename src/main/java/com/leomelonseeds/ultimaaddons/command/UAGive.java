package com.leomelonseeds.ultimaaddons.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;

public class UAGive implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!CommandUtils.isAdmin(sender)) {
            return true;
        }
        
        if (args.length < 2) {
            CommandUtils.sendErrorMsg(sender, "Usage: /ugive [player] [item] <amount>");
            return true;
        }
        
        Player p = Bukkit.getPlayer(args[0]);
        if (p == null) {
            CommandUtils.sendErrorMsg(sender, "Player was not found!");
            return true;
        }
        
        ItemStack i = UltimaAddons.getPlugin().getItems().getItem(args[1]);
        if (i == null) {
            CommandUtils.sendErrorMsg(sender, "Item was not found!");
            return true;
        }
        
        if (args.length == 3) {
            i.setAmount(Integer.parseInt(args[2]));
        }
        
        p.getInventory().addItem(i);
        return true;
    }

}
