package com.leomelonseeds.ultimaaddons.commands.ua.uask.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.skaddon.RotatingShopkeeper;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.RandomCollection;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;

public class RotateCommand extends Command {
    public RotateCommand(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        super(name, aliases, permission, description, arguments);
    }

    @Override
    public boolean hasInvalidArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        // Check for valid number of arguments
        if (args.length < 1) {
            CommandUtils.sendErrorMsg(sender, "Usage: /uask rotate [shopkeeper id]");
            return true;
        }
        if (!args[0].equals("-a")) {
            if (super.hasInvalidArgs(sender, args)) return true;
            int id = NumberUtils.toInt(args[0]);
            if (!this.plugin.getSKLinker().hasShopkeeper(id)) {
                CommandUtils.sendErrorMsg(sender, "Not a RSK (no link)");
                return true;
            }
        }
        return false;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        if (args[0].equals("-a")) {
            for (RotatingShopkeeper rsk : this.plugin.getSKLinker().getValues())
                if (!rotateTrades(rsk))
                    CommandUtils.sendErrorMsg(sender, "A fatal error occurred for shopkeeper id: " + rsk.getId());
            CommandUtils.sendMsg(sender, "Items were successfully rotated for all RSKs");
        } else {
            int child = NumberUtils.toInt(args[0]);
            RotatingShopkeeper rsk = this.plugin.getSKLinker().getRotatingShopkeeper(child);
            if (rotateTrades(rsk)) CommandUtils.sendMsg(sender, "Items were successfully rotated");
            else CommandUtils.sendErrorMsg(sender, "A fatal error occurred");
        }
    }

    private boolean rotateTrades(RotatingShopkeeper rsk) {
        // Rotate Trades
        if (!(rsk.getShopkeeper() instanceof RegularAdminShopkeeper ask))
            return false;
        if (!(rsk.getParentShopkeeper() instanceof RegularAdminShopkeeper parentAsk))
            return false;

        // Check if broken before rotating
        if (rsk.isBroken()) {
            ask.clearOffers();
            return false;
        }

        // Start rotating, clear current offers
        ask.clearOffers();
        List<? extends TradeOffer> offers = parentAsk.getOffers();
        RandomCollection<Integer> rc = new RandomCollection<>();
        for (int i = 0; i < rsk.getWeights().size(); i++)
            rc.add(rsk.getWeights().get(i), i);

        Random rand = new Random();
        int numTrades = rand.nextInt(rsk.getMinTrades(), rsk.getMaxTrades() + 1);
        for (int i = 0; i < numTrades; i++)
            ask.addOffer(offers.get(rc.next()));

        rsk.getAllUses().clear();
        this.plugin.getTradesFile().getConfig().set(rsk.getId() + ".uses", null);
        this.plugin.getTradesFile().save();

        return true;
    }

    @Override
    public List<String> handleTab(@NotNull String[] args) {
        List<String> options = super.handleTab(args);
        if (options.size() < 2)
            return options;
        // Argument returns immutable list, we must create a new List object
        options = new ArrayList<>(options);
        options.add("-a");
        return options;
    }
}
