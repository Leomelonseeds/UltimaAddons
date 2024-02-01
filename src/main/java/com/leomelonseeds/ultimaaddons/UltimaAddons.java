package com.leomelonseeds.ultimaaddons;

import com.leomelonseeds.ultimaaddons.ability.ae.CaptureEffect;
import com.leomelonseeds.ultimaaddons.ability.ae.RecuperateEffect;
import com.leomelonseeds.ultimaaddons.commands.BaseCommand;
import com.leomelonseeds.ultimaaddons.data.Load;
import com.leomelonseeds.ultimaaddons.data.Save;
import com.leomelonseeds.ultimaaddons.data.file.ConfigFile;
import com.leomelonseeds.ultimaaddons.data.file.Data;
import com.leomelonseeds.ultimaaddons.handlers.*;
import com.leomelonseeds.ultimaaddons.invs.InventoryManager;
import com.leomelonseeds.ultimaaddons.utils.UAPlaceholders;
import net.advancedplugins.ae.api.AEAPI;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kingdoms.constants.metadata.KingdomMetadataHandler;
import org.kingdoms.constants.metadata.StandardKingdomMetadataHandler;
import org.kingdoms.constants.namespace.Namespace;


public class UltimaAddons extends JavaPlugin {

    public static final long CHALLENGE_COOLDOWN_TIME = 1 * 24 * 3600 * 1000; // 1 day
    public static KingdomMetadataHandler lckH;
    public static KingdomMetadataHandler shield_time;
    public static KingdomMetadataHandler outpost_id;
    public static NamespacedKey itemKey;
    private static UltimaAddons plugin;
    private LinkManager linkManager;
    private InventoryManager invManager;
    private ItemManager itemManager;
    private ConfigFile configFile;
    private Data tradesFile;


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
        new BaseCommand();

        // Define kingdoms namespaces
        lckH = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "LCK")); // Last challenged kingdom, Last challenged date
        shield_time = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "SHIELD_TIME"));  // (long) Next available time a kingdom can buy a shield
        outpost_id = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "OUTPOST_ID"));  // (long) id of outpost/outpost land
        itemKey = new NamespacedKey(plugin, "uaitem");

        // Register managers and stuff
        linkManager = new LinkManager();
        invManager = new InventoryManager();
        itemManager = new ItemManager(this);
        UAUnclaimProcessor.register();
        new UAPlaceholders().register();
        AEAPI.registerEffect(plugin, new CaptureEffect(plugin));
        AEAPI.registerEffect(plugin, new RecuperateEffect(plugin));

        // Register listener
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new KingdomsListener(), this);
        pm.registerEvents(invManager, this);
        pm.registerEvents(itemManager, this);
        pm.registerEvents(itemManager.getAbilities(), this);
        pm.registerEvents(new ShopkeeperTrade(), this);

        // Register and Load Data File
        tradesFile = new Data("trades.yml");
        new Load();
    }

    @Override
    public void onDisable() {
        writeTradesFile();
        itemManager.getAbilities().cancelTasks();
    }

    public InventoryManager getInvManager() {
        return invManager;
    }

    public ItemManager getItems() {
        return itemManager;
    }

    public LinkManager getSKLinker() {
        return linkManager;
    }

    public Data getTradesFile() {
        return tradesFile;
    }

    public ConfigFile getConfigFile() {
        return configFile;
    }

    public void writeTradesFile() {
        for (Integer id : linkManager.keySet()) {
            new Save(id, linkManager.getRotatingShopkeeper(id));
        }
        tradesFile.save();
    }

    public void reload() {
        reloadConfig();
        getConfigFile().reload();
        getItems().loadItems();
        getTradesFile().reload();
        getSKLinker().clear();
        new Load();
    }
}
