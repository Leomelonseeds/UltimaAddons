package com.leomelonseeds.ultimaaddons.commands;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.google.common.collect.ImmutableList;
import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.objects.RotatingShopkeeper;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;

import co.aikar.commands.PaperCommandManager;

public class CommandManager {
    private UltimaAddons plugin = UltimaAddons.getPlugin();
    private PaperCommandManager cmdManager = UltimaAddons.getPlugin().getCommandManager();

    public CommandManager() {
        // Create command completions
        cmdManager.getCommandCompletions().registerCompletion("ua_item", c -> ImmutableList.copyOf(plugin.getItems().getItemNames()));
        cmdManager.getCommandCompletions().registerCompletion("ua_coordinates", c -> {
            Block target = c.getPlayer().getTargetBlockExact(30);
            Location loc = target == null ? c.getPlayer().getLocation() : target.getLocation();
            String stringLoc = loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
            String worldLoc = loc.getWorld().getName() + "," + stringLoc;
            return ImmutableList.of(worldLoc, stringLoc);
        });
        cmdManager.getCommandCompletions().registerCompletion("ua_sk", c -> ImmutableList.copyOf(
                plugin.getSKLinker().getValues().stream().map(RotatingShopkeeper::getId).map(String::valueOf).toList()
        ));
        cmdManager.getCommandCompletions().registerCompletion("sk", c -> ImmutableList.copyOf(
                ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllShopkeepers().stream().filter(sk ->
                        sk instanceof RegularAdminShopkeeper).map(Shopkeeper::getId).map(String::valueOf).toList()
        ));

        // Register commands
        cmdManager.registerCommand(new UAReload(plugin));
        cmdManager.registerCommand(new UAGive(plugin));
        cmdManager.registerCommand(new UARecipes(plugin));
        cmdManager.registerCommand(new UAIntro());
        cmdManager.registerCommand(new UAChallenge());
        cmdManager.registerCommand(new UASk(plugin));
    }
}
