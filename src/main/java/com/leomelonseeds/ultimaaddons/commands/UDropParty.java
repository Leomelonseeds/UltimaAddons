package com.leomelonseeds.ultimaaddons.commands;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;

@CommandAlias("udropparty")
public class UDropParty extends BaseCommand {
    
    private UltimaAddons plugin;
    private Random random;
    
    public UDropParty() {
        this.plugin = UltimaAddons.getPlugin();
        this.random = new Random();
    }
    
    @Default
    @CommandPermission("ua.dropparty")
    public void onTest(int amt) {
        ConfigurationSection partyConfig = plugin.getConfig().getConfigurationSection("party");
        int amount = partyConfig.getInt("amount") * amt;
        int time = partyConfig.getInt("time");
        int min = partyConfig.getInt("min");
        int max = partyConfig.getInt("max");
        give(0, time, min, max, amount);
    }
    
    private void give(int i, int time, int min, int max, int amt) {
        if (i >= amt) {
            return;
        }

        ItemStack dchip = plugin.getItems().getItem("dchip");
        dchip.setAmount(random.nextInt(min, max + 1));
        for (Player p : Bukkit.getOnlinePlayers()) {
            Item item = p.getWorld().dropItem(p.getLocation().add(0, 2, 0), dchip);
            item.setOwner(p.getUniqueId());
            double vely = 0.3;
            double velx = random.nextDouble(-0.1, 0.1);
            double velz = random.nextDouble(-0.1, 0.1);
            item.setVelocity(new Vector(velx, vely, velz));
        }
        
        Utils.schedule(time, () -> give(i + 1, time, min, max, amt));
    }
}
