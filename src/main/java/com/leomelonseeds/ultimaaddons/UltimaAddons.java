package com.leomelonseeds.ultimaaddons;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.kingdoms.constants.metadata.KingdomMetadataHandler;
import org.kingdoms.constants.metadata.StandardKingdomMetadataHandler;
import org.kingdoms.constants.namespace.Namespace;

import com.leomelonseeds.ultimaaddons.ability.ae.CaptureEffect;
import com.leomelonseeds.ultimaaddons.ability.ae.CooldownEffect;
import com.leomelonseeds.ultimaaddons.ability.ae.RecuperateEffect;
import com.leomelonseeds.ultimaaddons.ability.ae.UAddDurabilityArmor;
import com.leomelonseeds.ultimaaddons.ability.ae.UAddDurabilityCurrentItem;
import com.leomelonseeds.ultimaaddons.commands.CommandManager;
import com.leomelonseeds.ultimaaddons.data.Load;
import com.leomelonseeds.ultimaaddons.data.Save;
import com.leomelonseeds.ultimaaddons.data.file.ConfigFile;
import com.leomelonseeds.ultimaaddons.data.file.Data;
import com.leomelonseeds.ultimaaddons.handlers.LootHandler;
import com.leomelonseeds.ultimaaddons.handlers.MiscListener;
import com.leomelonseeds.ultimaaddons.handlers.ParryListener;
import com.leomelonseeds.ultimaaddons.handlers.aurelium.AureliumRegistry;
import com.leomelonseeds.ultimaaddons.handlers.item.ItemManager;
import com.leomelonseeds.ultimaaddons.handlers.kingdom.KingdomsListener;
import com.leomelonseeds.ultimaaddons.handlers.kingdom.UAUnclaimProcessor;
import com.leomelonseeds.ultimaaddons.handlers.shopkeeper.LinkManager;
import com.leomelonseeds.ultimaaddons.handlers.shopkeeper.RegionManager;
import com.leomelonseeds.ultimaaddons.handlers.shopkeeper.ShopkeeperListener;
import com.leomelonseeds.ultimaaddons.invs.InventoryManager;
import com.leomelonseeds.ultimaaddons.utils.UAPlaceholders;

import co.aikar.commands.PaperCommandManager;
import net.advancedplugins.ae.api.AEAPI;
import net.advancedplugins.ae.impl.effects.effects.effects.AdvancedEffect;
import net.milkbowl.vault.economy.Economy;


public class UltimaAddons extends JavaPlugin {

    public static final long CHALLENGE_COOLDOWN_TIME = 1 * 24 * 3600 * 1000; // 1 day

    public static KingdomMetadataHandler lckh;
    public static KingdomMetadataHandler shield_time;
    public static KingdomMetadataHandler outpost_id;

    public static NamespacedKey itemKey;
    public static NamespacedKey duraKey;

    private static UltimaAddons plugin;
    private PaperCommandManager cmdManager;
    private ParryListener parryListener;
    private LinkManager linkManager;
    private RegionManager regionManager;
    private InventoryManager invManager;
    private ItemManager itemManager;
    private ConfigFile configFile;
    private Data tradesFile;
    private Data regionsFile;
    private Economy econ;
    private AureliumRegistry aureliumRegistry;
    private LootHandler lootHandler;


    public static UltimaAddons getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        // Load config
        saveDefaultConfig();
        configFile = new ConfigFile();

        // Register commands
        cmdManager = new PaperCommandManager(this);
        new CommandManager();

        // Define kingdoms namespaces
        lckh = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "LCK")); // Last challenged kingdom, Last challenged date
        shield_time = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "SHIELD_TIME"));  // (long) Next available time a kingdom can buy a shield
        outpost_id = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "OUTPOST_ID"));  // (long) id of outpost/outpost land
        itemKey = new NamespacedKey(plugin, "uaitem");
        duraKey = new NamespacedKey(plugin, "uadura");

        // Register economy
        RegisteredServiceProvider<Economy> rspE = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rspE.getProvider();

        // Register managers and stuff
        linkManager = new LinkManager();
        regionManager = new RegionManager();
        invManager = new InventoryManager();
        itemManager = new ItemManager(this);
        lootHandler = new LootHandler(this);
        parryListener = new ParryListener();
        UAUnclaimProcessor.register();
        new UAPlaceholders().register();
        registerAE(new CaptureEffect(plugin));
        registerAE(new RecuperateEffect(plugin));
        registerAE(new UAddDurabilityCurrentItem(plugin));
        registerAE(new UAddDurabilityArmor(plugin));
        registerAE(new CooldownEffect(plugin));

        // Register listener
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new KingdomsListener(), this);
        pm.registerEvents(invManager, this);
        pm.registerEvents(itemManager, this);
        pm.registerEvents(itemManager.getAbilities(), this);
        pm.registerEvents(itemManager.getArmor(), this);
        pm.registerEvents(itemManager.getRecipes(), this);
        pm.registerEvents(itemManager.getTotems(), this);
        pm.registerEvents(new ShopkeeperListener(), this);
        pm.registerEvents(new MiscListener(), this);
        pm.registerEvents(lootHandler, this);
        pm.registerEvents(parryListener, this);

        // Register and Load Data Files
        tradesFile = new Data("trades.yml", "data");
        regionsFile = new Data("regions.yml", "data");
        new Load();

        // Register Aurelium Hook
        aureliumRegistry = new AureliumRegistry();
    }

    @Override
    public void onDisable() {
        writeTradesFile();
        writeRegionsFile();
        itemManager.getAbilities().cancelTasks();
    }

    public InventoryManager getInvs() {
        return invManager;
    }

    public ItemManager getItems() {
        return itemManager;
    }

    public PaperCommandManager getCommandManager() {
        return cmdManager;
    }

    public LinkManager getSKLinker() {
        return linkManager;
    }

    public RegionManager getRegionLinker() {
        return regionManager;
    }

    public Data getTradesFile() {
        return tradesFile;
    }

    public Data getRegionsFile() {
        return regionsFile;
    }

    public ConfigFile getConfigFile() {
        return configFile;
    }

    public ParryListener getParry() {
        return parryListener;
    }

    public Economy getEconomy() {
        return econ;
    }

    public AureliumRegistry getAureliumRegistry() {
        return aureliumRegistry;
    }

    public void writeTradesFile() {
        for (Integer id : linkManager.keySet()) {
            new Save(id, linkManager.getRotatingShopkeeper(id));
        }
        tradesFile.save();
    }

    public void writeRegionsFile() {
        for (String region : regionManager.keySet()) {
            new Save(region, regionManager.getShopkeeperFromRegion(region));
        }
        regionsFile.save();
    }

    public void reload() {
        reloadConfig();
        getConfigFile().reload();
        getItems().reload();
        getTradesFile().reload();
        getRegionsFile().reload();
        getSKLinker().clear();
        lootHandler.reload();
        new Load();
    }

    private void registerAE(AdvancedEffect ae) {
        try {
            AEAPI.registerEffect(plugin, ae);
        } catch (RuntimeException e) {
            // This happens if /reload or plugman is used.
            // It is unsafe to use those on production.
        }
    }
}
