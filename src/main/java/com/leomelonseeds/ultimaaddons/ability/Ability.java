package com.leomelonseeds.ultimaaddons.ability;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Get maximum cooldown
     *
     * @return general cooldown
     */
    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int i) {
        cooldown = i;
    }

    /**
     * Get cooldown for a specified player
     *
     * @param p player
     * @return cooldown for player
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
     * @param player player
     * @param target can be null
     * @param e      the bukkit event which triggered this ability
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

        long c_time = System.currentTimeMillis();
        cooldowns.put(player, c_time + cooldown * 1000L);
    }

    /**
     * Helper method to determine if an Event is a
     * PlayerInteractEvent with right click
     *
     * @param e
     * @return
     */
    protected boolean isRightClick(Event e) {
        if (!(e instanceof PlayerInteractEvent pie)) {
            return false;
        }

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
     * @param player player
     * @param target target
     * @return if the ability successfully executed
     */
    public abstract boolean executeAbility(Player player, LivingEntity target, Event e);

    /**
     * Override in order to provide extra events when the plugin is reloaded
     */
    public void onReload() {
    }

}
