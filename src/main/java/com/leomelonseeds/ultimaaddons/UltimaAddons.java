package com.leomelonseeds.ultimaaddons;

import org.bukkit.plugin.java.JavaPlugin;
import org.kingdoms.constants.namespace.Namespace;

import com.leomelonseeds.ultimaaddons.invs.InventoryManager;


public class UltimaAddons extends JavaPlugin {

    public static final int WAR_HOURS = 2;
    public static final int CHALLENGE_COOLDOWN_HOURS = 24;
    public static final int NEWBIE_DAYS = 5;
    private static UltimaAddons plugin;
    private InventoryManager invManager;
    
    // Last challenged kingdom, Last challenged date
    public static final Namespace LCK = new Namespace("UltimaAddons", "LCK");
	
	@Override
    public void onEnable() {
	    plugin = this;
        saveDefaultConfig();
	    invManager = new InventoryManager();
		getServer().getPluginManager().registerEvents(new UAListener(), this);
        getCommand("uchallenge").setExecutor(new UAChallenge());
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
