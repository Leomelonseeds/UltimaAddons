package com.leomelonseeds.ultimaaddons.ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.leomelonseeds.ultimaaddons.utils.Utils;

public class Blink extends Ability {

    private int distance;

    public Blink(int distance) {
        this.distance = distance;
    }

    @Override
    public boolean executeAbility(Player player, LivingEntity target, Event e) {
        if (!isRightClick(e)) {
            return false;
        }

        // Iterates to find save location for player up to distance
        Location prevLoc = player.getLocation();
        boolean thruPlayer = false;
        for (int i = 1; i <= distance; i++) {
            Location toLoc = prevLoc.clone().add(prevLoc.getDirection());
            Location plus = toLoc.clone().add(0, 1, 0);

            // Only accept if location is safe, otherwise use prev location
            if (plus.getBlock().getType().isSolid()) {
                break;
            }

            // Check if teleport through player
            if (!thruPlayer && toLoc.getNearbyEntities(0.5, 0.5, 0.5).stream().anyMatch(
                    ent -> ent instanceof Player && !ent.equals(player))) {
                thruPlayer = true;
            }

            // Spawn cool particles
            toLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, plus, 1, 0, 0, 0, 0);
            prevLoc = toLoc;
        }

        // Face opposite direction if tp through player
        if (thruPlayer) {
            prevLoc.setDirection(prevLoc.getDirection().multiply(-1));
        }

        // If a player is facing slightly downward on flat ground, this location will not be air
        // In this case, move the location up until it is on the ground block
        if (prevLoc.getBlock().getType() != Material.AIR) {
            prevLoc.setY(Math.ceil(prevLoc.getY()));
        }

        player.teleport(prevLoc);
        Utils.sendSound(Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1, prevLoc);
        Utils.sendSound(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2F, 2F, prevLoc);

        return true;
    }

}
