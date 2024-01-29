package com.leomelonseeds.ultimaaddons.ability;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class Shiruken extends Ability implements Listener {
    
    private double speed;
    private double damage;
    private int ticks;
    
    public Shiruken(double speed, double damage, int ticks) {
        Bukkit.getServer().getPluginManager().registerEvents(this, UltimaAddons.getPlugin());
        this.speed = speed;
        this.damage = damage;
        this.ticks = ticks;
    }

    @Override
    public boolean executeAbility(Player player, LivingEntity target, Event e) {
        if (!isRightClick(e)) {
            return false;
        }
        
        ItemStack shiruken = player.getInventory().getItemInMainHand();
        Snowball ball = player.launchProjectile(Snowball.class, player.getLocation().getDirection().multiply(speed));
        ItemStack ballItem = new ItemStack(shiruken);
        ballItem.setAmount(1);
        ball.setItem(ballItem);
        shiruken.setAmount(shiruken.getAmount() - 1);
        return true;
    }
    
    @EventHandler
    public void onProjHit(ProjectileHitEvent e) {
        if (e.getEntityType() != EntityType.SNOWBALL) {
            return;
        }
        
        Snowball ball = (Snowball) e.getEntity();
        if (ball.getShooter() == null || !(ball.getShooter() instanceof Player)) {
            return;
        }
        
        ItemStack item = ball.getItem();
        if (Utils.getItemID(item) == null) {
            return;
        }
        
        if (e.getHitBlock() != null) {
            dropItem(ball);
            return;
        }
        
        Entity ent = e.getHitEntity();
        if (ent == null || !(ent instanceof LivingEntity)) {
            dropItem(ball);
            return;
        }
        
        LivingEntity target = (LivingEntity) ent;
        if (target.hasMetadata("NPC")) {
            dropItem(ball);
            return;
        }
        
        new BukkitRunnable() {
            
            int cur = 0;
            
            @Override
            public void run() {
                if (cur >= ticks) {
                    target.getWorld().dropItem(target.getLocation(), item, ie -> ie.setPickupDelay(20));
                    this.cancel();
                    return;
                }

                target.setNoDamageTicks(0);
                target.damage(damage, (Player) ball.getShooter());
                target.setVelocity(new Vector(0, target.getVelocity().getY(), 0));
                cur++;
            }
        }.runTaskTimer(UltimaAddons.getPlugin(), 0, 1);
    }
    
    private void dropItem(Snowball ball) {
        ball.getWorld().dropItem(ball.getLocation(), ball.getItem(), ie -> ie.setPickupDelay(20));
    }
    
    @Override
    public void onReload() {
        HandlerList.unregisterAll(this);
    }
}
