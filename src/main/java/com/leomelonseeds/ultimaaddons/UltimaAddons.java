package com.leomelonseeds.ultimaaddons;

import org.bukkit.plugin.java.JavaPlugin;
import org.kingdoms.constants.metadata.KingdomMetadataHandler;
import org.kingdoms.constants.metadata.StandardKingdomMetadataHandler;
import org.kingdoms.constants.namespace.Namespace;

import com.leomelonseeds.ultimaaddons.invs.InventoryManager;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;


public class UltimaAddons extends JavaPlugin {

    public static final long WAR_TIME = 2 * 3600 * 1000; // 2 hours
    public static final long CHALLENGE_COOLDOWN_TIME = 1 * 24 * 3600 * 1000; // 1 day
    public static final long NEWBIE_TIME = 5 * 24 * 3600 * 1000; // 5 days
    private static UltimaAddons plugin;
    private InventoryManager invManager;

    // Last challenged kingdom, Last challenged date
    public static KingdomMetadataHandler lckh = new StandardKingdomMetadataHandler(new Namespace("UltimaAddons", "LCK"));
    
    // Discord war channel
    public static TextChannel warChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("war");
    
	@Override
    public void onEnable() {
	    plugin = this;
        saveDefaultConfig();
	    invManager = new InventoryManager();
		getServer().getPluginManager().registerEvents(new UAListener(), this);
        getCommand("uchallenge").setExecutor(new UAChallenge());
        new UAPlaceholders().register();
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
