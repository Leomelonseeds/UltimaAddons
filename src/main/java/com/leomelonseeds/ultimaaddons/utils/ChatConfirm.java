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
import org.bukkit.scheduler.BukkitTask;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.invs.ConfirmCallback;

@SuppressWarnings("deprecation")
public class ChatConfirm implements Listener {
    
    public static Map<Player, ChatConfirm> instances = new HashMap<>();
    private ConfirmCallback callback;
    private BukkitTask timeout;
    private Player player;
    private String req;
    private UltimaAddons plugin;
    private String cancelmsg;
    
    public ChatConfirm(Player player, String req, int time, String cancelmsg, ConfirmCallback callback) {
        this.callback = callback;
        this.req = req;
        this.player = player;
        this.cancelmsg = cancelmsg;
        
        // Return if player already is in a chat window
        if (instances.containsKey(player)) {
            player.sendMessage(Utils.toComponent("&cYou already have an existing chat dialogue!"));
            callback.onConfirm(false);
            return;
        }
        
        instances.put(player, this);
        this.plugin = UltimaAddons.getPlugin();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.timeout = Utils.schedule(time * 20, () -> {
            stop();
            callback(false);
        });
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        if (!sender.equals(player)) {
            return;
        }
        
        boolean success = e.getMessage().equalsIgnoreCase(req);
        this.timeout.cancel();
        e.setCancelled(true);
        stop();
        Bukkit.getScheduler().runTask(plugin, () -> callback(success));
    }
    
    public String getReq() {
        return req;
    }
    
    private void callback(boolean success) {
        if (!success && cancelmsg != null) {
            player.sendMessage(Utils.toComponent("&c" + cancelmsg));
        }
        
        callback.onConfirm(success);
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        instances.remove(player);
    }
}
