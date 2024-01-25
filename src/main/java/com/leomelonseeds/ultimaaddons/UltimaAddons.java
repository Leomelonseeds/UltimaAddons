package com.leomelonseeds.ultimaaddons;

import org.bukkit.plugin.java.JavaPlugin;
import org.kingdoms.constants.metadata.KingdomMetadataHandler;
import org.kingdoms.constants.metadata.StandardKingdomMetadataHandler;
import org.kingdoms.constants.namespace.Namespace;

import com.leomelonseeds.ultimaaddons.ae.ShootFireball;
import com.leomelonseeds.ultimaaddons.handlers.UAUnclaimProcessor;
import com.leomelonseeds.ultimaaddons.invs.InventoryManager;
import com.leomelonseeds.ultimaaddons.utils.UAPlaceholders;

import net.advancedplugins.ae.api.AEAPI;


public class UltimaAddons extends JavaPlugin {

    public static final long CHALLENGE_COOLDOWN_TIME = 1 * 24 * 3600 * 1000; // 1 day
    private static UltimaAddons plugin;
    private InventoryManager invManager;
    public static KingdomMetadataHandler lckh;
    public static KingdomMetadataHandler shield_time;
    public static KingdomMetadataHandler outpost_id;
    
    @Override
    public void onEnable() {
        plugin = this;
        
        // Load config
        saveDefaultConfig();
        
        // Register managers and stuff
        invManager = new InventoryManager();
        UAUnclaimProcessor.register();
        new UAPlaceholders().register();
        
        // Register listener
        getServer().getPluginManager().registerEvents(new UAListener(), this);
        
        // Register commands
        getCommand("uchallenge").setExecutor(new UAChallenge());
        
        // Define kingdoms namespaces
        lckh = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "LCK")); // Last challenged kingdom, Last challenged date
        shield_time = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "SHIELD_TIME"));  // (long) Next available time a kingdom can buy a shield
        outpost_id = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "OUTPOST_ID"));  // (long) id of outpost/outpost land
        
        // Register AE custom effects
        AEAPI.registerEffect(plugin, new ShootFireball(plugin, "SHOOT_FIREBALL"));
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
}
