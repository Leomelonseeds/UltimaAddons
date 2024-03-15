package com.leomelonseeds.ultimaaddons.ability.ae;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import net.advancedplugins.ae.impl.effects.effects.actions.execution.ExecutionTask;
import net.advancedplugins.ae.impl.effects.effects.effects.AdvancedEffect;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * RECUPERATE:[ticks to cut off]
 */
public class RecuperateEffect extends AdvancedEffect {

    private UltimaAddons plugin;

    public RecuperateEffect(UltimaAddons plugin) {
        super(plugin, "RECUPERATE");
        this.plugin = plugin;
    }

    @Override
    public boolean executeEffect(ExecutionTask task, LivingEntity target, String[] args) {
        if (!(target instanceof Player)) {
            return false;
        }

        Player p = (Player) target;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int shieldcd = p.getCooldown(Material.SHIELD);
            if (shieldcd == 0) {
                return;
            }

            int toCut = args.length == 0 ? 0 : NumberUtils.toInt(args[0]);
            p.setCooldown(Material.SHIELD, shieldcd - toCut);
        }, 1);
        return true;
    }

}
