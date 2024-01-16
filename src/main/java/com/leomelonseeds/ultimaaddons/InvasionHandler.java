package com.leomelonseeds.ultimaaddons;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.kingdoms.config.KingdomsConfig;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.KingdomRelation;
import org.kingdoms.managers.invasions.Invasion;
import org.kingdoms.managers.invasions.Plunder;
import org.kingdoms.managers.invasions.Plunder.State;

public class InvasionHandler implements Listener {
    
    private Player target;
    private Creature champion;
    private Set<UUID> inArea;

    public InvasionHandler(Invasion invasion) {
        if (!(invasion instanceof Plunder)) {
            Bukkit.getLogger().log(Level.WARNING, "Detected an invasion that is not a plunder! Not using custom invasion handler...");
            return;
        }
        Plunder plunder = (Plunder) invasion;
        this.inArea = new HashSet<>();
        this.champion = invasion.getChampion();
        this.target = invasion.getInvaderPlayer();
        champion.setTarget(target);
        
        // Register event
        Bukkit.getServer().getPluginManager().registerEvents(this, UltimaAddons.getPlugin());
        
        // Stop capture progress if one defender present
        Kingdom defender = invasion.getDefender();
        plunder.setTickProcessor(data -> {
            if (inArea.contains(champion.getUniqueId()) || hasPlayerInArea(defender) || 
                    defender.getKingdomsWithRelation(KingdomRelation.ALLY).stream().anyMatch(k -> hasPlayerInArea(k))) {
                data.cancelled = true;
                plunder.setState(State.PROTECTED);
            } else {
                data.cancelled = false;
                plunder.setState(State.CAPTURING);
            }
        });
        
        // Champion target task
        double invloc = plunder.getStartLocation().getY();
        int above = KingdomsConfig.Invasions.PLUNDER_VERTICAL_BOUNDARIES_UPWARDS.getManager().getInt();
        int below = KingdomsConfig.Invasions.PLUNDER_VERTICAL_BOUNDARIES_DOWNWARDS.getManager().getInt();
        new BukkitRunnable() {
            @Override
            public void run() {
                // Stop if ended
                if (invasion.hasEnded()) {
                    stop();
                    this.cancel();
                }
                
                // Update entities in area
                inArea.clear();
                plunder.getEntitiesInArea().forEach(e -> {
                    double y = e.getLocation().getY();
                    if (y >= invloc - below && y <= invloc + above && !e.isDead()) {
                        inArea.add(e.getUniqueId());
                    }
                });
                
                // Stop targetting if champion is dead
                if (champion.isDead()) {
                    return;
                }
                
                // If target is still in the area continue targetting him
                if (inArea.contains(target.getUniqueId())) {
                    return;
                }
                
                Player newTarget = findAttacker(invasion.getAttacker());
                if (newTarget != null) {
                    target = newTarget;
                    champion.setTarget(newTarget);
                }
            }
        }.runTaskTimer(UltimaAddons.getPlugin(), 0, 20);
    }
    
    // Crypto said I needed this so here it is
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChampionTarget(EntityTargetEvent event) {
        if (!event.getEntity().getUniqueId().equals(champion.getUniqueId())) {
            return;
        }
        
        Entity ctarget = event.getTarget();
        
        if (ctarget == null || !target.getUniqueId().equals(ctarget.getUniqueId())) {
            event.setTarget(target);
        }
    }

    // Handle various champion damages
    @EventHandler
    public void onChampionDamage(EntityDamageEvent e) {
        if (!e.getEntity().getUniqueId().equals(champion.getUniqueId())) {
            return;
        }
    }
    
    // TODO: teleport champion back if more than 15 seconds are spent outside the chunk
    
    private Player findAttacker(Kingdom attacker) {
        for (Player p : attacker.getOnlineMembers()) {
            if (inArea.contains(p.getUniqueId())) {
                return p;
            }
        }
        
        for (Kingdom ally : attacker.getKingdomsWithRelation(KingdomRelation.ALLY)) {
            for (Player p : ally.getOnlineMembers()) {
                if (inArea.contains(p.getUniqueId())) {
                    return p;
                }
            }
        }
        
        return null;
    }
    
    private boolean hasPlayerInArea(Kingdom kingdom) {
        return kingdom.getOnlineMembers().stream().anyMatch(p -> inArea.contains(p.getUniqueId()));
    }
    
    private void stop() {
        HandlerList.unregisterAll(this);
    }
}
