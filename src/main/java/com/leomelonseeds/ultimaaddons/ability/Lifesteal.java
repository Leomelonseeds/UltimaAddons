package com.leomelonseeds.ultimaaddons.ability;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.leomelonseeds.ultimaaddons.utils.Utils;

public class Lifesteal extends Ability {
    
    private int percent;
    
    public Lifesteal(int percent) {
        this.percent = percent;
    }
    
    
    @Override
    public boolean executeAbility(Player player, LivingEntity target, Event e) {
        if (!(target instanceof Player)) {
            return false;
        }
        
        if (!(e instanceof EntityDamageByEntityEvent)) {
            return false;
        }
        
        if (!((EntityDamageByEntityEvent) e).isCritical()) {
            return false;
        }
        
        // Deal custom damage with setHealth
        // This happens within the event so target health is still value before damage
        double dmg = target.getHealth() * (percent / 100.0);
        target.setHealth(Math.max(target.getHealth() - dmg, 0)); // It shouldn't be possible for the final value to < 0, but just in case...
        target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
        target.getWorld().spawnParticle(Particle.SCULK_SOUL, target.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5);
        Utils.sendSound(Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 2F, 1.5F, target.getLocation());
        return true;
    }

}
