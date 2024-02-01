package com.leomelonseeds.ultimaaddons.handlers;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.ability.Ability;
import com.leomelonseeds.ultimaaddons.utils.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class AbilityManager implements Listener {

    private Map<String, Ability> abilities;
    private Map<Player, BukkitTask> tasks;

    public AbilityManager() {
        abilities = new HashMap<>();
        tasks = new HashMap<>();
    }

    /**
     * @param s must correspond to an item name
     * @param a
     */
    public void addAbility(String s, Ability a) {
        abilities.put(s, a);
    }

    public void clearAbilities() {
        abilities.values().forEach(Ability::onReload);
        abilities.clear();
    }

    public void cancelTasks() {
        tasks.values().forEach(BukkitTask::cancel);
    }

    // Handle interact abilities
    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        runAbility(e.getItem(), e.getPlayer(), null, e);
    }

    // Handle damage abilities
    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) {
            return;
        }

        runAbility(p.getInventory().getItemInMainHand(), p, e.getEntity(), e);
    }

    // Checks necessary conditions to invoke an ability running in the first place
    private void runAbility(ItemStack item, Player player, Entity target, Event e) {
        if (item == null) {
            return;
        }

        if (item.isEmpty()) {
            return;
        }

        String data = Utils.getItemID(item);
        if (data == null) {
            return;
        }

        Ability a = abilities.get(data);
        if (a == null) {
            return;
        }

        if (target == null) {
            a.runAbility(player, null, e);
        } else {
            if (!(target instanceof LivingEntity)) {
                return;
            }

            a.runAbility(player, (LivingEntity) target, e);
        }
    }

    // Handle actionbars
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        tasks.put(p, new BukkitRunnable() {

            boolean lastAvailable = false;
            String lastData = "";

            @Override
            public void run() {
                ItemStack i = p.getInventory().getItemInMainHand();
                String data = Utils.getItemID(i);
                if (data == null || !abilities.containsKey(data)) {
                    if (!lastData.isEmpty()) {
                        p.sendActionBar(Utils.toComponent(""));
                    }
                    lastData = "";
                    return;
                }

                Ability a = abilities.get(data);
                int cd = a.getCooldown(p);
                if (a.getCooldown() <= 0) {
                    if (!lastData.equals(data)) {
                        p.sendActionBar(Utils.toComponent(""));
                    }
                } else if (cd > 0) {
                    p.sendActionBar(Utils.toComponent("&c[✕] " + a + ": Cooldown for " + cd + "s"));
                    lastAvailable = false;
                } else if (data.equals(lastData) && lastAvailable) {
                    return;
                } else {
                    p.sendActionBar(Utils.toComponent("&a[✔] " + a + ": Ready"));
                    lastAvailable = true;
                }

                lastData = data;
            }
        }.runTaskTimerAsynchronously(UltimaAddons.getPlugin(), 2, 2));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!tasks.containsKey(p)) {
            return;
        }

        tasks.remove(p).cancel();
    }
}
