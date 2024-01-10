package com.leomelonseeds.ultimaaddons;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.leomelonseeds.ultimaaddons.invs.ConfirmCallback;

import io.papermc.paper.event.player.AsyncChatEvent;

public class ChatConfirm implements Listener {
    
    private ConfirmCallback callback;
    private boolean success;
    private String req;
    
    public ChatConfirm(String req, ConfirmCallback callback) {
        this.callback = callback;
        this.req = req;
        this.success = false;
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
        if (!ConfigUtils.toPlain(e.originalMessage()).equals(req)) {
            return;
        }
        
        success = true;
        e.setCancelled(true);
        stop();
        Bukkit.getScheduler().runTask(UltimaAddons.getPlugin(), () -> callback.onConfirm(true));
    }
    
    private void stop() {
        HandlerList.unregisterAll(this);
    }
}
