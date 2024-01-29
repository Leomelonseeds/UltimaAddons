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
    
    /**
     * Get maximum cooldown
     * 
     * @return
     */
    public int getCooldown() {
        return cooldown;
    }
    
    /**
     * Get cooldown for a specified player
     * 
     * @param p
     * @return
     */
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
        return (int) Math.ceil(diff / 1000.0);
    }
    
    /**
     * Calling this method requires that the
     * player is holding an item with an ability
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
        
        // Return if on cooldown
        if (getCooldown(player) > 0) {
            return;
        }
        
        // Check if the ability was ran
        if (!executeAbility(player, target, e)) {
            return;
        }
        
        // Add cooldown registration
        if (cooldown == 0) {
            return;
        }
        
        long ctime = System.currentTimeMillis();
        cooldowns.put(player, ctime + cooldown * 1000);
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
    
    /**
     * Run the ability. Target can be null. Assume
     * player is off cooldown already.
     * 
     * @param player
     * @param target
     * @return if the ability successfully executed
     */
    public abstract boolean executeAbility(Player player, LivingEntity target, Event e);
    
    /**
     * Override in order to provide extra events when the plugin is reloaded
     */
    public void onReload() {}

}
