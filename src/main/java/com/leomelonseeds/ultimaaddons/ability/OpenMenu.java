package com.leomelonseeds.ultimaaddons.ability;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import com.leomelonseeds.ultimaaddons.invs.IntroInv;

public class OpenMenu extends Ability {

    @Override
    public boolean executeAbility(Player player, LivingEntity target, Event e) {
        if (!isRightClick(e)) {
            return false;
        }
        
        ((PlayerInteractEvent) e).setCancelled(true);
        switch (displayName) {
            case "introbook":
                new IntroInv(player);
        }
        
        return true;
    }

}
