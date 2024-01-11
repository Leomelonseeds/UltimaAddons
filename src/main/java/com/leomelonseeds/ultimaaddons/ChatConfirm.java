package com.leomelonseeds.ultimaaddons;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.leomelonseeds.ultimaaddons.invs.ConfirmCallback;

import io.papermc.paper.event.player.AsyncChatEvent;

public class ChatConfirm implements Listener {
    
    public static Map<Player, ChatConfirm> instances = new HashMap<>();
    private ConfirmCallback callback;
    private Player player;
    private boolean success;
    private String req;
    
    public ChatConfirm(Player player, String req, ConfirmCallback callback) {
        this.callback = callback;
        this.req = req;
        this.success = false;
        this.player = player;
        instances.put(player, this);
        Bukkit.getServer().getPluginManager().registerEvents(this, UltimaAddons.getPlugin());
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
            if (!success) {
                callback.onConfirm(false);
                stop();
            }
        }, 15 * 20);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncChatEvent e) {
        if (!e.getPlayer().equals(player)) {
            return;
        }
        
        if (!Utils.toPlain(e.originalMessage()).equals(req)) {
            return;
        }
        
        success = true;
        e.setCancelled(true);
        stop();
        Bukkit.getScheduler().runTask(UltimaAddons.getPlugin(), () -> callback.onConfirm(true));
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        instances.remove(player);
    }
}
