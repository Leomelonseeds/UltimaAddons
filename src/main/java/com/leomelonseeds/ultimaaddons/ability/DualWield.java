package com.leomelonseeds.ultimaaddons.ability;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import io.papermc.paper.event.player.PlayerArmSwingEvent;

public class DualWield extends Ability implements Listener {
    
    private static final int DEFAULT_COOLDOWN = 12;
    
    // Players who swung mainhand but not offhand
    // are added to this list, removed otherwise
    private Map<Player, BukkitTask> cd;
    private String weaponName;
    
    public DualWield(String weaponName) {
        this.weaponName = weaponName;
        Bukkit.getServer().getPluginManager().registerEvents(this, UltimaAddons.getPlugin());
        cd = new HashMap<>();
    }

    @Override
    public boolean executeAbility(Player player, LivingEntity target, Event e) {
        if (!(e instanceof EntityDamageByEntityEvent)) {
            return false;
        }

        if (!isDualWield(player)) {
            return false;
        }
        
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> target.setNoDamageTicks(DEFAULT_COOLDOWN / 2 - 1), 1);
        return true;
    }
    
    @Override
    public void onReload() {
        cd.values().forEach(t -> t.cancel());
        cd.clear();
        HandlerList.unregisterAll(this);
    }
    
    // Alternate swings between main and offhand
    @EventHandler
    public void onSwing(PlayerArmSwingEvent e) {
        Player p = e.getPlayer();
        if (!isDualWield(p)) {
            return;
        }
        
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        if (!cd.containsKey(p)) {
            cd.put(p, Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> cd.remove(p), DEFAULT_COOLDOWN));
            return;
        }
        
        e.setCancelled(true);
        cd.remove(p).cancel();
        p.swingOffHand();
    }
    
    // Check if player is dual wielding the specified weapon
    private boolean isDualWield(Player p) {
        PlayerInventory pinv = p.getInventory();
        String main = Utils.getItemID(pinv.getItemInMainHand());
        if (main == null || !main.equals(weaponName)) {
            return false;
        }
        
        String off = Utils.getItemID(pinv.getItemInOffHand());
        if (off == null || !off.equals(main)) {
            return false;
        }
        
        return true;
    }

}
