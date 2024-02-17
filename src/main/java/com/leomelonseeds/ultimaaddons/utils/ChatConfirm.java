package com.leomelonseeds.ultimaaddons.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.invs.ConfirmCallback;

@SuppressWarnings("deprecation")
public class ChatConfirm implements Listener {
    
    public static Map<Player, ChatConfirm> instances = new HashMap<>();
    private ConfirmCallback callback;
    private Player player;
    private boolean success;
    private String req;
    private UltimaAddons plugin;
    
    public ChatConfirm(Player player, String req, int time, ConfirmCallback callback) {
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
        }, time * 20);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        if (!sender.equals(player)) {
            return;
        }
        
        success = e.getMessage().equals(req);
        if (!success) {
            sender.sendMessage(Utils.toComponent("&cOperation cancelled."));
        }
        
        e.setCancelled(true);
        stop();
        Bukkit.getScheduler().runTask(plugin, () -> callback.onConfirm(success));
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        instances.remove(player);
    }
}
