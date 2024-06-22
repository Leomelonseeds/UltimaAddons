package com.leomelonseeds.ultimaaddons.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.data.Save;
import com.leomelonseeds.ultimaaddons.objects.RotatingShopkeeper;
import com.leomelonseeds.ultimaaddons.objects.UAShopkeeper;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.RandomCollection;
import com.leomelonseeds.ultimaaddons.utils.TimeParser;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;
import java.util.Random;

@CommandAlias("usk")
public class UASk extends BaseCommand {
    private final UltimaAddons plugin;

    public UASk(UltimaAddons plugin) {
        this.plugin = plugin;
    }

    @Default
    @Subcommand("help")
    @CommandPermission("ua.help")
    @Description("Help for Shopkeeper addon commands")
    public void onHelp(CommandSender sender) {
        CommandUtils.sendMsg(sender, "&a---+ RSK +---");
        CommandUtils.sendMsg(sender, "&a/uask debug&7 - Get internal debug info");
        CommandUtils.sendMsg(sender, "&a/uask discount&7 - wip");
        CommandUtils.sendMsg(sender, "&a/uask help&7 - Show this menu");
        CommandUtils.sendMsg(sender, "&a/uask info {child id}&7 - Get parent id of a RSK");
        CommandUtils.sendMsg(sender, "&a/uask limit&7 - wip");
        CommandUtils.sendMsg(sender, "&a/uask rotate {child id}&7 - Rotate trades of a RSK");
        CommandUtils.sendMsg(sender, "&a/uask sync {child id} {parent id}&7 - Create a RSK");
        CommandUtils.sendMsg(sender, "&a/uask unsync {child id}&7 - Delete a RSK");
        CommandUtils.sendMsg(sender, "&a-------------");
    }

    @Subcommand("debug")
    @CommandPermission("ua.sk.debug")
    @Description("Debug information for Shopkeeper addon internals")
    public void onDebug(CommandSender sender) {
        CommandUtils.sendMsg(sender, "&aTotal RSKs: &f" + this.plugin.getSKLinker().getSize());
        CommandUtils.sendMsg(sender, "&aConfig Values: &f");
        CommandUtils.sendMsg(sender, "&a->  Limit Warn Start: &f" + this.plugin.getConfigFile().limit_warn_start);
        CommandUtils.sendMsg(sender, "&a->  Restock Time: &f" + TimeParser.format(this.plugin.getConfigFile().restock_time));
        CommandUtils.sendMsg(sender, "&aNext Restock: &f" + TimeParser.timeUntil(this.plugin.getConfigFile().restock_time));
    }

    @Subcommand("discount")
    @CommandPermission("ua.sk.discount")
    @Description("WIP discount command")
    public void onDiscount(CommandSender sender) {
        CommandUtils.sendErrorMsg(sender, "WIP.");
    }

    @Subcommand("info")
    @CommandPermission("ua.sk.info")
    @CommandCompletion("@ua_sk")
    @Description("Get the parent SK of a child SK")
    @Syntax("<child id>")
    public void onInfo(CommandSender sender, int childID) {
        if (!this.plugin.getSKLinker().hasShopkeeper(childID)) {
            CommandUtils.sendErrorMsg(sender, "Not a RSK (no link)");
            return;
        }
        CommandUtils.sendMsg(sender, "Parent ID: " + this.plugin.getSKLinker().getUAShopkeeper(childID).getParentID());
    }

    @Subcommand("limit")
    @CommandPermission("ua.sk.limit")
    @Description("WIP limit command")
    public void onLimitSet(CommandSender sender) {
        CommandUtils.sendErrorMsg(sender, "WIP.");
    }

    @Subcommand("rotate all")
    @CommandPermission("ua.sk.rotate")
    @Description("Rotate the trades of all shopkeepers")
    public void onRotateAll(CommandSender sender) {
        for (UAShopkeeper usk : this.plugin.getSKLinker().getValues())
            if (usk instanceof RotatingShopkeeper rsk)
                if (!rotateTrades(rsk))
                    CommandUtils.sendErrorMsg(sender, "A fatal error occurred for child shopkeeper id: " + rsk.getChildID());
        CommandUtils.sendMsg(sender, "Items were successfully rotated for all RSKs");
    }

    @Subcommand("rotate")
    @CommandPermission("ua.sk.rotate")
    @CommandCompletion("@ua_sk|all")
    @Description("Rotate the trades of a shopkeeper")
    @Syntax("<shopkeeper id>")
    public void onRotate(CommandSender sender, int skID) {
        if (!this.plugin.getSKLinker().hasShopkeeper(skID)) {
            CommandUtils.sendErrorMsg(sender, "Not a linked child RSK");
            return;
        }
        UAShopkeeper usk = this.plugin.getSKLinker().getUAShopkeeper(skID);
        if (usk instanceof RotatingShopkeeper rsk) {
            if (rotateTrades(rsk))
                CommandUtils.sendMsg(sender, "Items were successfully rotated");
            else CommandUtils.sendErrorMsg(sender, "A fatal error occurred");
        } else {
            //todo
        }
    }

    private boolean rotateTrades(RotatingShopkeeper rsk) {
        // Rotate Trades
        RegularAdminShopkeeper ask = (RegularAdminShopkeeper) rsk.getShopkeeper();
        RegularAdminShopkeeper parentAsk = (RegularAdminShopkeeper) rsk.getParentShopkeeper();

        // Check if broken before rotating
        if (!rsk.isValid()) {
            ask.clearOffers();
            return false;
        }

        // Start rotating, clear current offers
        ask.clearOffers();
        List<? extends TradeOffer> offers = parentAsk.getOffers();
        RandomCollection<Integer> rc = new RandomCollection<>();
        for (int i = 0; i < rsk.getWeights().length; i++)
            rc.add(rsk.getWeights()[i], i);

        Random rand = new Random();

        String id = String.valueOf(rsk.getChildID());
        int minTrades = Objects.requireNonNull(UltimaAddons.getPlugin().getTradesFile().getConfig().getConfigurationSection(id)).getInt("min_trades");
        int maxTrades = Objects.requireNonNull(UltimaAddons.getPlugin().getTradesFile().getConfig().getConfigurationSection(id)).getInt("max_trades");
        int numTrades = rand.nextInt(minTrades, maxTrades + 1);
        for (int i = 0; i < numTrades; i++)
            ask.addOffer(offers.get(rc.next()));

        rsk.clearUses();
        this.plugin.getTradesFile().getConfig().set(rsk.getChildID() + ".uses", null);
        this.plugin.getTradesFile().save();

        rsk.getShopkeeper().abortUISessionsDelayed();
        return true;
    }

    @Subcommand("sync")
    @CommandPermission("ua.sk.syncing")
    @CommandCompletion("@sk @sk rsk|ask")
    @Description("Sync a parent to a child")
    @Syntax("<parent shopkeeper id> <child shopkeeper id> <type>")
    public void onSync(CommandSender sender, int parentID, int childID, String type) {
        // See if either are already linked
        if (this.plugin.getSKLinker().hasShopkeeper(childID)) {
            CommandUtils.sendErrorMsg(sender, "Child already linked!");
            return;
        }
        if (this.plugin.getSKLinker().hasShopkeeper(parentID)) {
            CommandUtils.sendErrorMsg(sender, "Parent is linked as a child (chain linking is not allowed)!");
            return;
        }
        // Check that it isn't itself
        if (childID == parentID) {
            CommandUtils.sendErrorMsg(sender, "Cannot link shopkeeper to itself!");
            return;
        }
        if (!type.equals("ask") && !type.equals("rsk")) {
            CommandUtils.sendErrorMsg(sender, "Invalid shopkeeper type!");
            return;
        }

        RotatingShopkeeper rsk = new RotatingShopkeeper(childID, parentID);
        this.plugin.getSKLinker().addLink(childID, rsk);
        new Save(childID, rsk);
        this.plugin.getTradesFile().save();

        CommandUtils.sendMsg(sender, "Successfully synced both shopkeepers");
    }

    @Subcommand("unsync")
    @CommandPermission("ua.sk.syncing")
    @CommandCompletion("@ua_sk")
    @Description("Unsync a child from its parent")
    @Syntax("<shopkeeper id>")
    public void onUnsync(CommandSender sender, int childID) {
        if (!this.plugin.getSKLinker().hasShopkeeper(childID)) {
            CommandUtils.sendErrorMsg(sender, "Not a linked child RSK");
            return;
        }
        this.plugin.getSKLinker().deleteLink(childID);
        this.plugin.getTradesFile().getConfig().set(String.valueOf(childID), null);
        this.plugin.getTradesFile().save();
        CommandUtils.sendMsg(sender, "Successfully unsynced shopkeeper");
    }
}
