package com.leomelonseeds.ultimaaddons.commands.ua;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.invs.Recipes;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;

public class UARecipes extends Command {

    public UARecipes(String name, List<String> aliases, String permission, String description) {
        super(name, aliases, permission, description, Collections.emptyList());
    }
    
    @Override
    public boolean hasInvalidArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            CommandUtils.sendConsoleMsg("You have to be a player to use this.");
            return true;
        }
        
        return super.hasInvalidArgs(sender, args);
    }

    @Override
    public void execute(CommandSender sender, org.bukkit.command.Command cmd, String name, String[] args) {
        new Recipes((Player) sender);
    }

}
