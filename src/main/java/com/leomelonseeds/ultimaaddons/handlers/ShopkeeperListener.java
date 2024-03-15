package com.leomelonseeds.ultimaaddons.handlers;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.skaddon.RotatingShopkeeper;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.TimeParser;
import com.nisovin.shopkeepers.api.events.ShopkeeperOpenUIEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeCompletedEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeEvent;
import com.nisovin.shopkeepers.api.ui.DefaultUITypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftAbstractVillager;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftMerchant;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopkeeperListener implements Listener {
    private final UltimaAddons plugin = UltimaAddons.getPlugin();

    @EventHandler
    public void onTrade(ShopkeeperTradeEvent e) {
        if (e.isCancelled())
            return;
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

        // If trade is possible
        if (playerUsage < limit)
            return;

        e.setCancelled(true);
        String countdown = TimeParser.timeUntil(this.plugin.getConfigFile().restock_time);
        CommandUtils.sendActionbarMsg(e.getPlayer(), "&cUnavailable. &7Restock in: &b(" + countdown + ")");
    }

    @EventHandler
    public void onTradeComplete(ShopkeeperTradeCompletedEvent e) {
        RotatingShopkeeper rsk = plugin.getSKLinker().getRotatingShopkeeper(e.getShopkeeper().getId());
        if (rsk == null)
            return;
        if (rsk.isBroken())
            return;

        int index = e.getShopkeeper().getTradingRecipes(null).indexOf(e.getCompletedTrade().getTradingRecipe());
        List<Integer> uses = rsk.getUses(e.getCompletedTrade().getPlayer());

        // First time trade
        if (uses.isEmpty()) {
            uses = new ArrayList<>(Collections.nCopies(e.getShopkeeper().getTradingRecipes(null).size(), 0));
            rsk.setUses(e.getCompletedTrade().getPlayer(), uses);
        }

        int usage = uses.get(index) + 1;
        uses.set(index, usage);

        int limit = rsk.getLimits().get(index);
        if (limit - usage <= plugin.getConfigFile().limit_warn_start)
            CommandUtils.sendActionbarMsg(e.getCompletedTrade().getPlayer(),
                    "&7Remaining Stock&7: &b" + (limit - usage));

        updateTrades(e.getCompletedTrade().getPlayer(), rsk);
        plugin.writeTradesFile();
    }

    @EventHandler
    public void onShopkeeperOpen(ShopkeeperOpenUIEvent e) {
        if (e.isCancelled())
            return;
        if (e.getUIType() != DefaultUITypes.TRADING())
            return;

        RotatingShopkeeper rsk = plugin.getSKLinker().getRotatingShopkeeper(e.getShopkeeper().getId());
        if (rsk == null)
            return;
        if (rsk.isBroken())
            return;

        updateTrades(e.getPlayer(), rsk);
    }

    private void updateTrades(Player p, RotatingShopkeeper rsk) {
        Bukkit.getScheduler().runTask(plugin, () -> {
                    InventoryView openInventory = p.getOpenInventory();
                    assert openInventory.getTopInventory().getType() == InventoryType.MERCHANT;
                    MerchantInventory merchantInventory = (MerchantInventory) openInventory.getTopInventory();
                    Merchant merchant = merchantInventory.getMerchant();
                    List<MerchantRecipe> recipes = merchant.getRecipes();

                    if (recipes.isEmpty())
                        return;

                    List<Integer> limits = rsk.getLimits();
                    List<Integer> uses = rsk.getUses(p);

                    for (int i = 0; i < recipes.size(); i++) {
                        MerchantRecipe recipe = recipes.get(i);
                        int limit = limits.get(i);
                        int usage = uses.isEmpty() ? 0 : uses.get(i);

                        recipe.setMaxUses(limit);
                        recipe.setUses(usage);
                    }

                    // Send packet
                    sendOffers(p);
                }
        );
    }

    /**
     * @see <a href="https://github.com/Shopkeepers/Shopkeepers/blob/06be9152718f4454e8a154af2ccbf21ccda9a8b7/modules/v1_20_R4/src/main/java/com/nisovin/shopkeepers/compat/v1_20_R4/NMSHandler.java#L197">...</a>
     */
    private void sendOffers(@NotNull Player player) {
        Inventory openInventory = player.getOpenInventory().getTopInventory();
        if (!(openInventory instanceof MerchantInventory merchantInventory))
            return;
        merchantInventory.setItem(0, merchantInventory.getItem(0));

        Merchant merchant = merchantInventory.getMerchant();
        net.minecraft.world.item.trading.Merchant nmsMerchant;
        boolean regularVillager = false;
        boolean canRestock = false;
        int merchantLevel = 1;
        int merchantExperience = 0;
        if (merchant instanceof Villager villager) {
            nmsMerchant = ((CraftVillager) merchant).getHandle();
            regularVillager = true;
            canRestock = true;
            merchantLevel = villager.getVillagerLevel();
            merchantExperience = villager.getVillagerExperience();
        } else if (merchant instanceof AbstractVillager)
            nmsMerchant = ((CraftAbstractVillager) merchant).getHandle();
        else {
            nmsMerchant = ((CraftMerchant) merchant).getMerchant();
            merchantLevel = 0;
        }

        MerchantOffers merchantRecipeList = nmsMerchant.getOffers();
        if (merchantRecipeList == null)
            merchantRecipeList = new MerchantOffers();

        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.sendMerchantOffers(
                nmsPlayer.containerMenu.containerId,
                merchantRecipeList,
                merchantLevel,
                merchantExperience,
                regularVillager,
                canRestock
        );
    }
}
