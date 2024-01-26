package com.leomelonseeds.ultimaaddons.ability;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class Ability {
    
    private Map<Player, Long> cooldowns; // Player, time when cooldown EXPIRES
    private String displayName;
    private int cooldown;
    
    public Ability() {
        cooldowns = new HashMap<>();
    }
    
    public void setDisplayName(String s) {
        displayName = s;
    }
    
    public void setCooldown(int i) {
        cooldown = i;
    }
    
    public int getCooldown(Player p) {
        Long cd = cooldowns.get(p);
        if (cd == null) {
            return 0;
        }
        
        long time = System.currentTimeMillis();
        if (time >= cd) {
            return 0;
        }
        
        long diff = cd - time;
        return (int) Math.ceil(diff / 1000);
    }
    
    /**
     * Calling this method requires that the
     * player is holding an item with an abilityt
     * 
     * @param player
     * @param target can be null
     * @param e the bukkit event which triggered this ability
     */
    public void runAbility(Player player, LivingEntity target, Event e) {
        if (displayName == null) {
            Bukkit.getLogger().warning("Tried to run an unregistered ability: " + getClass().getSimpleName());
            return;
        }
        
        // Check, according to an event, if the condition was met
        if (!canExecute(e)) {
            return;
        }
        
        // Return if on cooldown
        if (getCooldown(player) > 0) {
            return;
        }
        
        // Add cooldown registration
        long ctime = System.currentTimeMillis();
        cooldowns.put(player, ctime + cooldown * 1000);
        executeAbility(player, target);
    }
    
    /**
     * Helper method to determine if an Event is a 
     * PlayerInteractEvent with right click
     * 
     * @param e
     * @return
     */
    protected boolean isRightClick(Event e) {
        if (!(e instanceof PlayerInteractEvent)) {
            return false;
        }
        
        PlayerInteractEvent pie = (PlayerInteractEvent) e;
        return pie.getAction().toString().contains("RIGHT");
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public abstract void executeAbility(Player player, LivingEntity target);
    
    public abstract boolean canExecute(Event e);

}
