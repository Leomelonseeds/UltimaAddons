package com.leomelonseeds.ultimaaddons.ability.ae;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.leomelonseeds.ultimaaddons.UltimaAddons;

import net.advancedplugins.ae.impl.effects.effects.actions.execution.ExecutionTask;
import net.advancedplugins.ae.impl.effects.effects.effects.AdvancedEffect;

public class CooldownEffect extends AdvancedEffect {
    
    public CooldownEffect(UltimaAddons plugin) {
        super(plugin, "COOLDOWN");
    }
    

    @Override
    public boolean executeEffect(ExecutionTask task, LivingEntity target, String[] args) {
        if (target.getType() != EntityType.PLAYER) {
            return false;
        }
        
        Player p = (Player) target;
        Material m = Material.valueOf(args[0]);
        int ticks = NumberUtils.toInt(args[1]);
        p.setCooldown(m, ticks);
        return true;
    }
    
}
