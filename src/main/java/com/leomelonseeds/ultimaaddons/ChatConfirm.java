package com.leomelonseeds.ultimaaddons;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.leomelonseeds.ultimaaddons.invs.ConfirmCallback;

public class ChatConfirm implements Listener {
    
    public static Map<Player, ChatConfirm> instances = new HashMap<>();
    private ConfirmCallback callback;
    private Player player;
    private boolean success;
    private String req;
    private UltimaAddons plugin;
    
    public ChatConfirm(Player player, String req, ConfirmCallback callback) {
        this.callback = callback;
        this.req = req;
        this.success = false;
        this.player = player;
        
        // Return if player already is in a chat window
        if (instances.containsKey(player)) {
            callback.onConfirm(false);
            return;
        }
        
        instances.put(player, this);
        this.plugin = UltimaAddons.getPlugin();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (success) {
                return;
            }
            
            callback.onConfirm(false);
            stop();
        }, 30 * 20);
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!e.getPlayer().equals(player)) {
            return;
        }
        
        if (!e.getMessage().equals(req)) {
            return;
        }
        
        success = true;
        e.setCancelled(true);
        stop();
        Bukkit.getScheduler().runTask(plugin, () -> callback.onConfirm(true));
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        instances.remove(player);
    }
}
