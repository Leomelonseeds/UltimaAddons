package com.leomelonseeds.ultimaaddons.handlers.item;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.ability.Ability;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class AbilityManager implements Listener {
    
    private static Map<String, PotionEffectType> materials;
    private Map<String, Ability> abilities;
    private Map<Player, BukkitTask> tasks;
    
    public AbilityManager() {
        abilities = new HashMap<>();
        tasks = new HashMap<>();
        materials = Map.of(
            "radiantshard", PotionEffectType.REGENERATION,
            "obsidianingot", PotionEffectType.DAMAGE_RESISTANCE,
            "infusedingot", PotionEffectType.INCREASE_DAMAGE,
            "mithrilingot", PotionEffectType.SPEED,
            "nitinoltube", PotionEffectType.JUMP
        );
    }
    
    /**
     * @param s must correspond to an item name
     * @param a
     */
    public void addAbility(String s, Ability a) {
        abilities.put(s, a);
    }
    
    public void clearAbilities() {
        abilities.values().forEach(a -> a.onReload());
        abilities.clear();
    }
    
    public void cancelTasks() {
        tasks.values().forEach(t -> t.cancel());
    }
    
    // Handle interact abilities
    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        runAbility(e.getItem(), e.getPlayer(), null, e);
    }
    
    // Handle damage abilities
    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) {
            return;
        }
        
        Player p = (Player) e.getDamager();
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
    
    // Handle actionbars and ingot effects
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UltimaAddons plugin = UltimaAddons.getPlugin();
        
        tasks.put(p, new BukkitRunnable() {
            
            long iteration = 0;
            boolean lastAvailable = false;
            String lastData = "";
            
            @Override
            public void run() {
                iteration++;
                PlayerInventory inv = p.getInventory();
                String data = Utils.getItemID(inv.getItemInMainHand());
                
                // Handle radiant shards first
                for (String d : new String[] {data, Utils.getItemID(inv.getItemInOffHand())}) {
                    if (d == null) {
                        continue;
                    }
                    
                    PotionEffectType effect = materials.get(d);
                    if (effect == null) {
                        continue;
                    }
                    
                    int duration = 25;
                    if (effect == PotionEffectType.REGENERATION) {
                        duration = (iteration * 2) % 80 == 0 ? 51 : 25;
                    }
                    int fdur = duration;
                    
                    Bukkit.getScheduler().runTask(plugin, () -> p.addPotionEffect(new PotionEffect(effect, fdur, 0)));
                }
                
                // Clear existing actionbar if no item present
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
        }.runTaskTimerAsynchronously(plugin, 2, 2));
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
