package com.leomelonseeds.ultimaaddons.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.invs.RecipeList;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import org.bukkit.entity.Player;

@CommandAlias("urecipes")
public class UARecipes extends BaseCommand {
    private final UltimaAddons plugin;

    public UARecipes(UltimaAddons plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandPermission("ua.recipes")
    @Description("Shows all the custom recipes obtainable through crafting")
    public void onRecipes(Player p) {
        this.plugin.reload();
        CommandUtils.sendMsg(p, "&bNote: There is currently an issue where the resultant slot in recipe displays "
                + "may not show the correct item. In this case, the recipe list still shows the correct item.");
        new RecipeList(p);
    }
}
