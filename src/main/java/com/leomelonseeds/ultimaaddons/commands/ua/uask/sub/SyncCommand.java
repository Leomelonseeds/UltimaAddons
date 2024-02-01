package com.leomelonseeds.ultimaaddons.commands.ua.uask.sub;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.data.Save;
import com.leomelonseeds.ultimaaddons.skaddon.RotatingShopkeeper;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SyncCommand extends Command {
    public SyncCommand(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        super(name, aliases, permission, description, arguments);
    }

    public boolean hasInvalidArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        // Check for valid number of arguments
        if (args.length < 2) {
            CommandUtils.sendErrorMsg(sender, "Usage: /uask sync [child shopkeeper id] [parent shopkeeper id]");
            return true;
        }
        if (super.hasInvalidArgs(sender, args))
            return true;
        // We know it can be parsed
        int child = NumberUtils.toInt(args[0]), parent = NumberUtils.toInt(args[1]);
        // See if either are already linked
        if (this.plugin.getSKLinker().hasShopkeeper(child)) {
            CommandUtils.sendErrorMsg(sender, "Child already linked");
            return true;
        }
        if (this.plugin.getSKLinker().hasShopkeeper(parent)) {
            CommandUtils.sendErrorMsg(sender, "Parent is linked as a child");
            return true;
        }
        // Check that it isn't itself
        if (child == parent) {
            CommandUtils.sendErrorMsg(sender, "Cannot link shopkeeper to itself");
            return true;
        }

        return false;
    }


    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        if (hasInvalidArgs(sender, args))
            return;

        // Safe to parse since we already checked for valid integers
        int child = NumberUtils.toInt(args[0]), parent = NumberUtils.toInt(args[1]);

        Shopkeeper sk = ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getShopkeeperById(parent);
        int size = Objects.requireNonNull(sk).getTradingRecipes(null).size();
        List<Double> weights = new ArrayList<>(Collections.nCopies(size, 1.0));
        List<Integer> limits = new ArrayList<>(Collections.nCopies(size, 10));

        RotatingShopkeeper rsk = new RotatingShopkeeper(child, parent, weights, limits, new HashMap<>(), 0, 0);
        this.plugin.getSKLinker().addLink(child, rsk);
        new Save(child, this.plugin.getSKLinker().getRotatingShopkeeper(child));
        this.plugin.getTradesFile().save();

        CommandUtils.sendMsg(sender, "Successfully synced both shopkeepers");
    }
}
