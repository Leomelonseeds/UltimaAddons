package com.leomelonseeds.ultimaaddons.ability;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class Lifesteal extends Ability {
    
    private int percent;
    
    public Lifesteal(int percent) {
        this.percent = percent;
    }
    
    @Override
    public boolean canExecute(Player player, LivingEntity target, Event e) {
        if (!(target instanceof Player)) {
            return false;
        }
        return true;
    }
    
    
    @Override
    public void executeAbility(Player player, LivingEntity target) {
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
            if (target.isDead()) {
                return;
            }
            
            target.damage(target.getHealth() * ((double) percent / 100), target);
            target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
            target.getWorld().spawnParticle(Particle.SCULK_SOUL, target.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5);
            Utils.sendSound(Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 2F, 1.5F, target.getLocation());
        }, 1);
    }

}
