package com.leomelonseeds.ultimaaddons.handlers.shopkeeper;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.data.Save;
import com.leomelonseeds.ultimaaddons.objects.RegionData;
import com.leomelonseeds.ultimaaddons.objects.RotatingShopkeeper;
import com.leomelonseeds.ultimaaddons.objects.UAShopkeeper;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.TimeParser;
import com.leomelonseeds.ultimaaddons.utils.Utils;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.events.*;
import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.ShopkeeperCreateException;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopCreationData;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopType;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.trade.TradingPlayerShopkeeper;
import com.nisovin.shopkeepers.api.ui.DefaultUITypes;
import net.alex9849.arm.AdvancedRegionMarket;
import net.alex9849.arm.events.PreBuyEvent;
import net.alex9849.arm.events.RestoreRegionEvent;
import net.alex9849.arm.events.UnsellRegionEvent;
import net.alex9849.arm.regions.Region;
import net.alex9849.arm.regions.RegionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

import java.util.List;
import java.util.UUID;

public class ShopkeeperListener implements Listener {
    private final UltimaAddons plugin = UltimaAddons.getPlugin();

    @EventHandler
    public void onRegionPurchase(PreBuyEvent e) {
        if (!e.isPlayerInLimit()) {
            return;
        }

        Region r = e.getRegion();
        Player buyer = e.getBuyer();
        if (plugin.getEconomy().getBalance(buyer) < r.getPricePerPeriod()) {
            return;
        }

        TradingPlayerShopkeeper sk = getRegionShopkeeper(r);
        if (sk == null) {
            Utils.msg(buyer, "&cThis region does not have a shopkeeper! Please contact an admin to fix this.");
        }

        sk.setOwner(buyer);
        sk.setForHire(null);

        RegionData data = new RegionData(sk, sk.getLocation());
        String id = r.getRegion().getId();
        plugin.getRegionLinker().addLink(id, data);
        new Save(id, data);
        plugin.getRegionsFile().save();
    }

    @EventHandler
    public void onRegionUnsell(UnsellRegionEvent e) {
        Region r = e.getRegion();
        Player buyer = Bukkit.getPlayer(r.getOwner());
        String id = r.getRegion().getId();
        PlayerShopkeeper nsk = resetShopkeeper(id, buyer);
        if (nsk == null) {
            return;
        }

        nsk.setOwner(UUID.randomUUID(), "None");
        nsk.setForHire(plugin.getItems().getItem("shopkeeperhire"));

        plugin.getRegionLinker().deleteLink(id);
        plugin.getRegionsFile().getConfig().set(id, null);
        plugin.getRegionsFile().save();
    }

    @EventHandler
    public void onRegionReset(RestoreRegionEvent e) {
        // This event is called after unselling as well
        // Since unselling clears shopkeeper and makes it unsold,
        // we don't need to do anything here
        Region r = e.getRegion();
        if (!r.isSold()) {
            return;
        }

        String id = r.getRegion().getId();
        Player buyer = Bukkit.getPlayer(r.getOwner());
        PlayerShopkeeper nsk = resetShopkeeper(id, buyer);
        if (nsk == null) {
            return;
        }

        nsk.setOwner(buyer);
        RegionData data = new RegionData(nsk, nsk.getLocation());
        plugin.getRegionLinker().addLink(r.getRegion().getId(), data);
        new Save(id, data);
        plugin.getRegionsFile().save();
    }

    private PlayerShopkeeper resetShopkeeper(String id, Player buyer) {
        RegionData data = plugin.getRegionLinker().getShopkeeperFromRegion(id);
        if (data == null) {
            Bukkit.getLogger().warning("Could not find or load for region " + id);
            Utils.msg(buyer, "&cAn error occured with this region! Please contact an admin to fix this.");
            return null;
        }

        // Save shop creation data
        PlayerShopkeeper sk = (PlayerShopkeeper) data.getSk();
        ShopCreationData scd = PlayerShopCreationData.create(buyer, (PlayerShopType<?>) sk.getType(),
                sk.getShopObject().getType(), data.getOrigin(), null, sk.getContainer());

        // Remove old shopkeeper
        sk.delete();

        // Create new shopkeeper
        try {
            return (PlayerShopkeeper) ShopkeepersAPI.getShopkeeperRegistry().createShopkeeper(scd);
        } catch (ShopkeeperCreateException err) {
            Bukkit.getLogger().warning("Could not reset shopkeeper for region " + id);
            Utils.msg(buyer, "&cCould not reset shopkeeper for this region! Please contact an admin to fix this.");
            err.printStackTrace();
            return null;
        }
    }

    @EventHandler
    public void onCreate(PlayerCreatePlayerShopkeeperEvent e) {
        ShopCreationData cd = e.getShopCreationData();
        Player creator = cd.getCreator();
        if (creator.hasPermission("shopkeeper.admin")) {
            return;
        }

        Location spawnLoc = cd.getSpawnLocation();
        RegionManager rm = AdvancedRegionMarket.getInstance().getRegionManager();
        if (!rm.getRegionsByLocation(spawnLoc).isEmpty()) {
            e.setCancelled(true);
            Utils.msg(creator, "&cYou cannot place a shopkeeper in this area.");
        }

        // If we get here, player is attempting to create a shopkeeper somewhere in the world
        // We add 1 to the limit for any shop that is already inside a region (since those don't count)
        int extraLimit = e.getMaxShopsLimit();
        for (Shopkeeper sk : ShopkeepersAPI.getShopkeeperRegistry().getPlayerShopkeepersByOwner(creator.getUniqueId())) {
            if (!rm.getRegionsByLocation(sk.getLocation()).isEmpty()) {
                extraLimit++;
            }
        }

        e.setMaxShopsLimit(extraLimit);
    }

    @EventHandler
    public void onDelete(PlayerDeleteShopkeeperEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("shopkeeper.admin")) {
            return;
        }

        Location delLoc = e.getShopkeeper().getLocation();
        if (AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByLocation(delLoc).isEmpty()) {
            return;
        }

        e.setCancelled(true);
        Utils.msg(p, "&7You cannot delete shops in this area.");
    }

    @EventHandler
    public void onTrade(ShopkeeperTradeEvent e) {
        if (e.isCancelled())
            return;
        UAShopkeeper usk = plugin.getSKLinker().getUAShopkeeper(e.getShopkeeper().getId());

        if (usk instanceof RotatingShopkeeper rsk) {
            if (!rsk.isValid())
                return;

            int childIndex = e.getShopkeeper().getTradingRecipes(null).indexOf(e.getTradingRecipe());
            int parentIndex = getParentIndex(e.getTradingRecipe(), rsk.getParentShopkeeper());
            if (parentIndex == -1)
                e.setCancelled(true);

            int[] counts = rsk.getUses(e.getPlayer());

            // First time trade, other event will handle it.
            if (counts == null || counts.length == 0)
                return;

            int limit = rsk.getLimits()[parentIndex];
            int playerUsage = rsk.getUses(e.getPlayer())[childIndex];

            // If trade is possible
            if (playerUsage < limit)
                return;

            e.setCancelled(true);
            String countdown = TimeParser.timeUntil(this.plugin.getConfigFile().restock_time);
            CommandUtils.sendActionbarMsg(e.getPlayer(), "&cUnavailable. &7Restock in: &b(" + countdown + ")");
        } else {
            //todo
        }
    }

    @EventHandler
    public void onTradeComplete(ShopkeeperTradeCompletedEvent e) {
        UAShopkeeper usk = plugin.getSKLinker().getUAShopkeeper(e.getShopkeeper().getId());

        if (usk instanceof RotatingShopkeeper rsk) {
            if (!rsk.isValid())
                return;

            int childIndex = e.getShopkeeper().getTradingRecipes(null).indexOf(e.getCompletedTrade().getTradingRecipe());
            int parentIndex = getParentIndex(e.getCompletedTrade().getTradingRecipe(), rsk.getParentShopkeeper());

            if (!rsk.logUse(e.getCompletedTrade().getPlayer(), childIndex))
                return;

            int usage = rsk.getUses(e.getCompletedTrade().getPlayer())[childIndex];
            int limit = rsk.getLimits()[parentIndex];
            if (limit - usage <= plugin.getConfigFile().limit_warn_start)
                CommandUtils.sendActionbarMsg(e.getCompletedTrade().getPlayer(),
                        "&7Remaining Stock&7: &b" + (limit - usage));

            updateTrades(e.getCompletedTrade().getPlayer(), rsk);
            plugin.writeTradesFile();
        } else {
            //todo
        }
    }

    @EventHandler
    public void onShopkeeperOpen(ShopkeeperOpenUIEvent e) {
        if (e.isCancelled())
            return;
        if (e.getUIType() != DefaultUITypes.TRADING())
            return;

        UAShopkeeper usk = plugin.getSKLinker().getUAShopkeeper(e.getShopkeeper().getId());
        if (usk instanceof RotatingShopkeeper rsk) {
            if (!rsk.isValid())
                return;
            updateTrades(e.getPlayer(), rsk);
        } else {
            //todo
        }
    }

    private int getParentIndex(TradingRecipe recipe, Shopkeeper parent) {
        int parentIndex = -1;
        for (TradingRecipe rec : parent.getTradingRecipes(null)) {
            if (rec.areItemsEqual(recipe))
                parentIndex = parent.getTradingRecipes(null).indexOf(rec);
        }
        return parentIndex;
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

            int[] limits = rsk.getLimits();
            int[] uses = rsk.getUses(p);

            for (int i = 0; i < recipes.size(); i++) {
                MerchantRecipe recipe = recipes.get(i);
                TradingRecipe skRecipe = rsk.getShopkeeper().getTradingRecipes(null).get(i);

                int parentIndex = getParentIndex(skRecipe, rsk.getParentShopkeeper());

                int limit = limits[parentIndex];
                int usage = uses == null || uses.length == 0 ? 0 : uses[i];

                recipe.setMaxUses(limit);
                recipe.setUses(usage);
            }

            // Send packet
            sendOffers(p);
        });
    }

    // Fetch first playertradingshopkeepers in region
    private TradingPlayerShopkeeper getRegionShopkeeper(Region region) {
        for (PlayerShopkeeper shopkeeper : ShopkeepersAPI.getPlugin().getShopkeeperRegistry().getAllPlayerShopkeepers()) {
            if (!(shopkeeper instanceof TradingPlayerShopkeeper)) {
                continue;
            }

            Location loc = shopkeeper.getLocation();
            if (region.getRegion().contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                return (TradingPlayerShopkeeper) shopkeeper;
            }
        }

        Bukkit.getLogger().severe("No trading shopkeeper found in region " + region.getRegion().getId());
        return null;
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
