package com.leomelonseeds.ultimaaddons.ability;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import io.papermc.paper.event.player.PlayerArmSwingEvent;

public class DualWield extends Ability implements Listener {

    // Players who swung mainhand but not offhand
    // are added to this list, removed otherwise
    private Map<Player, BukkitTask> cd;
    private String weaponName;
    private int speed; // The ATTACK SPEED of the weapon measured in TICKS (default 12)

    public DualWield(String weaponName, int speed) {
        this.weaponName = weaponName;
        this.speed = speed;
        Bukkit.getServer().getPluginManager().registerEvents(this, UltimaAddons.getPlugin());
        cd = new HashMap<>();
    }

    @Override
    public boolean executeAbility(Player player, LivingEntity target, Event e) {
        if (!(e instanceof EntityDamageByEntityEvent)) {
            return false;
        }

        if (!isDualWield(player)) {
            return false;
        }

        // Switch blades before being used to give the illusion that both blades get used
        PlayerInventory playerInv = player.getInventory();
        ItemStack main = playerInv.getItemInMainHand();
        ItemStack off = playerInv.getItemInOffHand();
        ItemMeta mainMeta = main.getItemMeta();
        main.setItemMeta(off.getItemMeta());
        off.setItemMeta(mainMeta);
        
        Utils.schedule(1, () -> target.setNoDamageTicks(speed - 1));
        return true;
    }

    @Override
    public void onReload() {
        cd.values().forEach(BukkitTask::cancel);
        cd.clear();
        HandlerList.unregisterAll(this);
    }

    // Alternate swings between main and offhand
    @EventHandler
    public void onSwing(PlayerArmSwingEvent e) {
        Player p = e.getPlayer();
        if (!isDualWield(p)) {
            return;
        }

        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!cd.containsKey(p)) {
            Utils.schedule(speed * 2, () -> cd.remove(p));
            return;
        }

        e.setCancelled(true);
        cd.remove(p).cancel();
        p.swingOffHand();
    }

    // Check if player is dual wielding the specified weapon
    private boolean isDualWield(Player p) {
        PlayerInventory playerInv = p.getInventory();
        String main = Utils.getItemID(playerInv.getItemInMainHand());
        if (main == null || !main.equals(weaponName))
            return false;

        String off = Utils.getItemID(playerInv.getItemInOffHand());
        return off != null && off.equals(main);
    }

}
