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
    private String cancelmsg;
    
    public ChatConfirm(Player player, String req, int time, String cancelmsg, ConfirmCallback callback) {
        this.callback = callback;
        this.req = req;
        this.success = false;
        this.player = player;
        this.cancelmsg = cancelmsg;
        
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
            
            callback();
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
        e.setCancelled(true);
        stop();
        Bukkit.getScheduler().runTask(plugin, () -> callback());
    }
    
    public String getReq() {
        return req;
    }
    
    private void callback() {
        if (!success) {
            player.sendMessage(Utils.toComponent("&c" + cancelmsg));
        }
        
        callback.onConfirm(success);
        stop();
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        instances.remove(player);
    }
}
