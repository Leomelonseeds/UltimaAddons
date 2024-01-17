package com.leomelonseeds.ultimaaddons;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.kingdoms.config.KingdomsConfig;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.KingdomRelation;
import org.kingdoms.constants.group.upgradable.champion.ChampionUpgrade;
import org.kingdoms.events.general.ChampionAbilityEvent;
import org.kingdoms.managers.invasions.Invasion;
import org.kingdoms.managers.invasions.Plunder;
import org.kingdoms.managers.invasions.Plunder.State;

public class InvasionHandler implements Listener {
    
    private final static int CHAMPION_MAX_OUT = 15;
    
    private Player target;
    private Creature champion;
    private Set<UUID> inArea;
    private Location init;
    private int championOut;
    private Kingdom defender;
    
    public InvasionHandler(Invasion invasion) {
        if (!(invasion instanceof Plunder)) {
            Bukkit.getLogger().log(Level.WARNING, "Detected an invasion that is not a plunder! Not using custom invasion handler...");
            return;
        }
        Plunder plunder = (Plunder) invasion;
        this.inArea = new HashSet<>();
        this.champion = invasion.getChampion();
        this.target = invasion.getInvaderPlayer();
        this.init = invasion.getStartLocation();
        this.defender = invasion.getDefender();
        this.championOut = 0;
        champion.setTarget(target);
        
        // Register event
        Bukkit.getServer().getPluginManager().registerEvents(this, UltimaAddons.getPlugin());
        
        // Stop capture progress if one defender present
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
                
                // Teleport champion to initial location if he has been out for 15 sec
                if (!inArea.contains(champion.getUniqueId())) {
                    championOut++;
                } else {
                    championOut = 0;
                }
                
                if (championOut >= CHAMPION_MAX_OUT) {
                    champion.teleport(init);
                    championOut = 0;
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
        
        // Stop fall damage
        if (e.getCause() == DamageCause.FALL) {
            e.setCancelled(true);
            return;
        }
        
        // TP back if void damage somehow
        if (e.getCause() == DamageCause.VOID) {
            e.setCancelled(true);
            champion.teleport(init);
            championOut = 0;
            return;
        }
    }
    
    // Disable shields on THOR
    @EventHandler
    public void onChampionAbility(ChampionAbilityEvent e) {
        if (!e.getInvasion().getChampion().equals(champion)) {
            return;
        }
        
        if (e.getAbility().getName() != ChampionUpgrade.THOR) {
            return;
        }
        
        Set<UUID> defenders = new HashSet<>();
        defender.getOnlineMembers().forEach(p -> defenders.add(p.getUniqueId()));
        for (UUID uuid : inArea) {
            Player ap = Bukkit.getPlayer(uuid);
            if (ap == null) {
                continue;
            }
            
            if (!defenders.contains(uuid)) {
                ap.setCooldown(Material.SHIELD, 100);
            }
        }
    }
    
    // Add PIERCING to skeleton arrows
    @EventHandler
    public void onBowFire(EntityShootBowEvent e) {
        if (!e.getEntity().getUniqueId().equals(champion.getUniqueId())) {
            return;
        }
        
        if (!(e.getProjectile() instanceof Arrow)) {
            return;
        }
        
        ((Arrow) e.getProjectile()).setPierceLevel(4);
    }
    
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
