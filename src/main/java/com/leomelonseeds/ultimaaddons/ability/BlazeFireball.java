package com.leomelonseeds.ultimaaddons.ability;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class BlazeFireball extends Ability {
    
    private int power;
    private double randomness;
    
    public BlazeFireball(int power, double randomness) {
        this.power = power;
        this.randomness = randomness;
    }

    @Override
    public boolean executeAbility(Player player, LivingEntity target, Event e) {
        if (!isRightClick(e)) {
            return false;
        }
        
        shootFireball(player, randomness);
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> shootFireball(player, randomness * -1), 6);
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> shootFireball(player, 0), 12);
        return true;
    }
    
    private void shootFireball(Player player, double r) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();
        Fireball fireball = (Fireball) player.getWorld().spawnEntity(eye.clone().add(direction), EntityType.FIREBALL);
        
        if (r != 0) {
            Random random = new Random();
            double abs = Math.abs(r);
            double x = random.nextDouble(abs) * (r > 0 ? 1 : -1);
            double y = random.nextDouble(abs) * (r > 0 ? 1 : -1);
            double z = random.nextDouble(abs) * (r > 0 ? 1 : -1);
            Vector vector = new Vector(x, y, z);
            direction = direction.add(vector);
        }
        
        fireball.setIsIncendiary(true);
        fireball.setYield(power);
        fireball.setDirection(direction);
        fireball.setShooter(player);
        Utils.sendSound(Sound.ENTITY_GHAST_SHOOT, 1, 1, player.getLocation());
    }

}
