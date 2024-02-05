package com.leomelonseeds.ultimaaddons.ability.ae;

import java.util.Random;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.handlers.ItemManager;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.impl.effects.effects.actions.execution.ExecutionTask;
import net.advancedplugins.ae.impl.effects.effects.effects.AdvancedEffect;

public class UAddDurabilityCurrentItem extends AdvancedEffect {

    public UAddDurabilityCurrentItem(UltimaAddons plugin) {
        super(plugin, "UADD_DURABILITY_CURRENT_ITEM");
    }
    

    @Override
    public boolean executeEffect(ExecutionTask task, LivingEntity target, String[] args) {
        if (!(target instanceof Player)) {
            return true;
        }
        
        ItemStack item = task.getBuilder().getItem();
        damageItem((Player) target, item, (int) NumberUtils.toDouble(args[0]));
        return true;
    }
    
    /**
     * Damage an item by a certain amount, taking into
     * account unbreaking.
     * 
     * @param i
     * @param amt
     */
    public static void damageItem(Player player, ItemStack item, int amt) {
        if (amt == 0 || item == null || item.getItemMeta() == null) {
            return;
        }
        
        if (!(item.getItemMeta() instanceof Damageable)) {
            return;
        }
        
        // The supplied amount is the amount to add, so multiply by -1 to get amt to damage
        amt = amt * -1;
        Damageable dmeta = (Damageable) item.getItemMeta();
        ItemManager im = UltimaAddons.getPlugin().getItems();
        
        // Heals item if damage is negative, e.g. positive durability addition
        if (amt < 0) {
            if (im.damageItem(item, amt)) {
                return;
            }
            
            // If above returned false, this is not a custom item
            // Add amt since its negative
            dmeta.setDamage(Math.max(dmeta.getDamage() + amt, 0));
            item.setItemMeta(dmeta);
            return;
        }
        
        // Damages item
        int famt = 0;
        Random random = new Random();
        int unbreaking = dmeta.getEnchantLevel(Enchantment.DURABILITY);
        if (unbreaking > 0) {
            // Use unbreaking formula from MC WIKI
            double toCompare;
            if (item.getItemMeta() instanceof ArmorMeta) {
                toCompare = 0.6 + 0.4 / (unbreaking + 1);
            } else {
                toCompare = 1.0 / (unbreaking + 1);
            }

            for (int i = 0; i < amt; i++) {
                if (random.nextDouble() < toCompare) {
                    famt++;
                }
            }
        } else {
            famt = amt;
        }
        
        if (im.damageItem(item, famt)) {
            return;
        }
        
        int max = item.getType().getMaxDurability();
        int fdmg = dmeta.getDamage() + famt;
        if (fdmg > max) {
            item.setAmount(item.getAmount() - 1);
            Utils.sendSound(Sound.ENTITY_ITEM_BREAK, 1F, 1F, player.getLocation());
        } else {
            dmeta.setDamage(fdmg);
            item.setItemMeta(dmeta);
        }
    }

}
