package com.leomelonseeds.ultimaaddons.ability.ae;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimaaddons.UltimaAddons;

import net.advancedplugins.ae.impl.effects.effects.actions.execution.ExecutionTask;
import net.advancedplugins.ae.impl.effects.effects.effects.AdvancedEffect;

public class UAddDurabilityArmor extends AdvancedEffect {

    public UAddDurabilityArmor(UltimaAddons plugin) {
        super(plugin, "UADD_DURABILITY_ARMOR");
    }
    

    @Override
    public boolean executeEffect(ExecutionTask task, LivingEntity target, String[] args) {
        if (!(target instanceof Player)) {
            return true;
        }
        
        Player p = (Player) target;
        for (ItemStack a : p.getInventory().getArmorContents()) {
            UAddDurabilityCurrentItem.damageItem(p, a, (int) NumberUtils.toDouble(args[0]));
        }
        
        return true;
    }

}
