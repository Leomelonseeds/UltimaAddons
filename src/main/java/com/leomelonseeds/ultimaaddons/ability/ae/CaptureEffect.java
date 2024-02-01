package com.leomelonseeds.ultimaaddons.ability.ae;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import net.advancedplugins.ae.impl.effects.effects.actions.execution.ExecutionTask;
import net.advancedplugins.ae.impl.effects.effects.effects.AdvancedEffect;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CaptureEffect extends AdvancedEffect {

    private List<String> excluded;

    public CaptureEffect(UltimaAddons plugin) {
        super(plugin, "CAPTURE");
        excluded = plugin.getConfig().getStringList("capture-exclude");
    }

    @Override
    public boolean executeEffect(ExecutionTask task, LivingEntity target, String[] args) {
        // Don't drop spawn egg if target was not a mob
        if (!(target instanceof Mob)) {
            return true;
        }

        // Don't drop if mob is excluded
        String type = target.getType().toString();
        if (excluded.contains(type)) {
            return true;
        }

        // Drop spawn egg
        // Bad control flow I know, but idk how else...
        try {
            ItemStack egg = new ItemStack(Material.valueOf(type + "_SPAWN_EGG"));
            target.getWorld().dropItem(target.getLocation(), egg);
            return true;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

}
