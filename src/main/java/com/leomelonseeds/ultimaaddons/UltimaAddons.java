package com.leomelonseeds.ultimaaddons;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kingdoms.constants.metadata.KingdomMetadataHandler;
import org.kingdoms.constants.metadata.StandardKingdomMetadataHandler;
import org.kingdoms.constants.namespace.Namespace;

import com.leomelonseeds.ultimaaddons.ae.CaptureEffect;
import com.leomelonseeds.ultimaaddons.command.UAChallenge;
import com.leomelonseeds.ultimaaddons.command.UAGive;
import com.leomelonseeds.ultimaaddons.command.UAReload;
import com.leomelonseeds.ultimaaddons.handlers.ItemManager;
import com.leomelonseeds.ultimaaddons.handlers.KingdomsListener;
import com.leomelonseeds.ultimaaddons.handlers.UAUnclaimProcessor;
import com.leomelonseeds.ultimaaddons.invs.InventoryManager;
import com.leomelonseeds.ultimaaddons.utils.UAPlaceholders;

import net.advancedplugins.ae.api.AEAPI;


public class UltimaAddons extends JavaPlugin {

    private static UltimaAddons plugin;
    
    public static final long CHALLENGE_COOLDOWN_TIME = 1 * 24 * 3600 * 1000; // 1 day
    public static KingdomMetadataHandler lckh;
    public static KingdomMetadataHandler shield_time;
    public static KingdomMetadataHandler outpost_id;
    public static NamespacedKey itemKey;
    
    private InventoryManager invManager;
    private ItemManager itemManager;
    
    @Override
    public void onEnable() {
        plugin = this;
        
        // Load config
        saveDefaultConfig();
        
        // Register commands
        getCommand("uchallenge").setExecutor(new UAChallenge());
        getCommand("ugive").setExecutor(new UAGive());
        getCommand("ureload").setExecutor(new UAReload());
        
        // Define kingdoms namespaces
        lckh = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "LCK")); // Last challenged kingdom, Last challenged date
        shield_time = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "SHIELD_TIME"));  // (long) Next available time a kingdom can buy a shield
        outpost_id = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "OUTPOST_ID"));  // (long) id of outpost/outpost land
        itemKey = new NamespacedKey(plugin, "uaitem");
        
        // Register managers and stuff
        invManager = new InventoryManager();
        itemManager = new ItemManager(this);
        UAUnclaimProcessor.register();
        new UAPlaceholders().register();
        AEAPI.registerEffect(plugin, new CaptureEffect(plugin));
        
        // Register listener
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new KingdomsListener(), this);
        pm.registerEvents(invManager, this);
        pm.registerEvents(itemManager, this);
    }

    @Override
    public void onDisable() {
    }
    
    public static UltimaAddons getPlugin() {
        return plugin;
    }
    
    public InventoryManager getInvs() {
        return invManager;
    }
    
    public ItemManager getItems() {
        return itemManager;
    }
}
