package com.leomelonseeds.ultimaaddons.handlers.shopkeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.data.Save;
import com.leomelonseeds.ultimaaddons.objects.RegionData;
import com.leomelonseeds.ultimaaddons.objects.RotatingShopkeeper;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.TimeParser;
import com.leomelonseeds.ultimaaddons.utils.Utils;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.events.PlayerCreatePlayerShopkeeperEvent;
import com.nisovin.shopkeepers.api.events.PlayerDeleteShopkeeperEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperOpenUIEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeCompletedEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeEvent;
import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.api.shopkeeper.ShopkeeperCreateException;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.trading.MerchantOffers;

public class ShopkeeperListener implements Listener {
    private final UltimaAddons plugin = UltimaAddons.getPlugin();
    
    @EventHandler
    public void onRegionPurchase(PreBuyEvent e) {
        Region r = e.getRegion();
        TradingPlayerShopkeeper sk = getRegionShopkeeper(r);
        Player buyer = e.getBuyer();
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
        TradingPlayerShopkeeper sk = getRegionShopkeeper(r);
        Player buyer = Bukkit.getPlayer(r.getOwner());
        if (sk == null) {
            Utils.msg(buyer, "&cThis region does not have a shopkeeper! Please contact an admin to fix this.");
        }
        
        String id = r.getRegion().getId();
        RegionData data = plugin.getRegionLinker().getShopkeeperFromRegion(id);
        if (data == null) {
            Bukkit.getLogger().warning("Could not find a shopkeeper for region " + id);
            return;
        }
        
        PlayerShopkeeper nsk = resetShopkeeper(sk, r, buyer);
        if (nsk != null) {
            nsk.setOwner(UUID.randomUUID(), "None");
            nsk.setForHire(plugin.getItems().getItem("shopkeeperhire"));
        }
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
        
        TradingPlayerShopkeeper sk = getRegionShopkeeper(r);
        Player buyer = Bukkit.getPlayer(r.getOwner());
        if (sk == null) {
            Utils.msg(buyer, "&cThis region does not have a shopkeeper! Please contact an admin to fix this.");
        }
        
        PlayerShopkeeper nsk = resetShopkeeper(sk, r, buyer);
        if (nsk != null) {
            nsk.setOwner(buyer);
        }
    }
    
    private PlayerShopkeeper resetShopkeeper(TradingPlayerShopkeeper sk, Region r, Player buyer) {
        String id = r.getRegion().getId();
        RegionData data = plugin.getRegionLinker().getShopkeeperFromRegion(id);
        if (data == null) {
            Bukkit.getLogger().warning("Could not find a shopkeeper for region " + id);
            return null;
        }
        
        // Save shop creation data
        ShopCreationData scd = PlayerShopCreationData.create(buyer, (PlayerShopType<?>) sk.getType(), 
                sk.getShopObject().getType(), data.getOrigin(), null, sk.getContainer());
        
        // Remove old shopkeeper
        sk.delete();
        
        // Create new shopkeeper
        try {
            return (PlayerShopkeeper) ShopkeepersAPI.getShopkeeperRegistry().createShopkeeper(scd);
        } catch (ShopkeeperCreateException err) {
            Bukkit.getLogger().warning("Failed resetting shopkeeper in region " + id);
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
        if (AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByLocation(spawnLoc).isEmpty()) {
            return;
        }
        
        e.setCancelled(true);
        Utils.msg(creator, "&cYou cannot place a shopkeeper in this area.");
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
