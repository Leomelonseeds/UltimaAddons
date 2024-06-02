package com.leomelonseeds.ultimaaddons.ability.ae;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.impl.effects.effects.actions.execution.ExecutionTask;
import net.advancedplugins.ae.impl.effects.effects.effects.AdvancedEffect;

public class FallDistanceDamage extends AdvancedEffect {
    
    private Set<UUID> cancelFall;

    public FallDistanceDamage(UltimaAddons plugin) {
        super(plugin, "FALL_DISTANCE_DAMAGE");
        this.cancelFall = new HashSet<>();
    }
    
    @Override
    public boolean executeEffect(ExecutionTask task, LivingEntity target, String[] args) {
        LivingEntity attacker = this.getOtherEntity(target, task);
        float fd = attacker.getFallDistance();
        if (fd < 1.5) {
            return true;
        }
        
        double multiplier = NumberUtils.toDouble(args[0]);
        double dmg = (fd - 1) * multiplier;
        task.getDamageHandler().damage(target, attacker, dmg);
        Utils.sendSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1F, 0.5F, target.getLocation());
        cancelFall.add(attacker.getUniqueId());
        Utils.schedule(5, () -> cancelFall.remove(attacker.getUniqueId()));
        return true;
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        if (!cancelFall.contains(uuid)) {
            return;
        }
        
        if (e.getCause() != DamageCause.FALL) {
            return;
        }
        
        e.setCancelled(true);
        cancelFall.remove(uuid);
    }
}
