package com.leomelonseeds.ultimaaddons.handlers;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.skaddon.RotatingShopkeeper;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.TimeParser;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeCompletedEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopkeeperTrade implements Listener {
    private final UltimaAddons plugin = UltimaAddons.getPlugin();

    @EventHandler
    public void onTrade(ShopkeeperTradeEvent e) {
        RotatingShopkeeper rsk = plugin.getSKLinker().getRotatingShopkeeper(e.getShopkeeper().getId());
        if (rsk == null)
            return;
        if (rsk.isBroken())
            return;

        int index = e.getShopkeeper().getTradingRecipes(null).indexOf(e.getTradingRecipe());
        List<Integer> counts = rsk.getUses(e.getPlayer());

        // First time trade, other event will handle it.
        if (counts.isEmpty())
            return;

        int limit = rsk.getLimits().get(index);
        int playerUsage = counts.get(index);

        if (playerUsage < limit)
            return;

        e.setCancelled(true);
        String countdown = TimeParser.timeUntil(this.plugin.getConfigFile().restock_time);
        CommandUtils.sendMsg(e.getPlayer(), "&cOut of stock!");
        CommandUtils.sendMsg(e.getPlayer(), "&7Restock in: &b(" + countdown + ")");
    }

    @EventHandler
    public void onTradeComplete(ShopkeeperTradeCompletedEvent e) {
        RotatingShopkeeper rsk = plugin.getSKLinker().getRotatingShopkeeper(e.getShopkeeper().getId());
        if (rsk == null)
            return;
        if (rsk.isBroken())
            return;

        int size = e.getShopkeeper().getTradingRecipes(null).size();
        int index = e.getShopkeeper().getTradingRecipes(null).indexOf(e.getCompletedTrade().getTradingRecipe());

        List<Integer> counts = rsk.getUses(e.getCompletedTrade().getPlayer());

        // First time trade
        if (counts.isEmpty())
            counts = new ArrayList<>(Collections.nCopies(size, 0));

        int usage = counts.get(index) + 1;
        counts.set(index, usage);
        rsk.setUses(e.getCompletedTrade().getPlayer(), counts);

        int limit = rsk.getLimits().get(index);
        if (limit - usage <= plugin.getConfigFile().limit_warn_start && limit - usage > 0)
            CommandUtils.sendMsg(e.getCompletedTrade().getPlayer(),
                    "&7Remaining Stock&7: &b" + (limit - usage));
        else if (limit - usage == 0) {
            String countdown = TimeParser.timeUntil(this.plugin.getConfigFile().restock_time);
            CommandUtils.sendMsg(e.getCompletedTrade().getPlayer(),
                    "&7Item is now out of stock! Restock in: &b(" + countdown + ")");
        }

        plugin.writeTradesFile();
    }
}
