package com.leomelonseeds.ultimaaddons.invs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.logs.misc.challenge.LogKingdomChallenged;
import org.kingdoms.constants.group.model.logs.misc.challenge.LogKingdomChallenger;
import org.kingdoms.constants.metadata.StandardKingdomMetadata;
import org.kingdoms.constants.player.KingdomPlayer;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class ChallengeInv implements UAInventory {
    
    private Inventory inv;
    private Kingdom target;
    private Kingdom attacker;
    private Player player;
    
    public ChallengeInv(Kingdom target, Kingdom attacker, Player player) {
        this.target = target;
        this.attacker = attacker;
        this.player = player;
        
        inv = Bukkit.createInventory(null, 27, Utils.toComponent("&8-=( &cChallenge &e" + target.getName() + " &8)=-"));
        manager.registerInventory(player, this);
    }

    @Override
    public void updateInventory() {
        ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redmeta = red.getItemMeta();
        redmeta.displayName(Utils.toComponent(""));
        red.setItemMeta(redmeta);
        for (int i : new int[] {0, 8, 9, 17, 18, 26}) {
            inv.setItem(i, red);
        }
        
        ConfigurationSection config = UltimaAddons.getPlugin().getConfig().getConfigurationSection("challengegui");
        for (String key : config.getKeys(false)) {
            int slot = config.getInt(key + ".slot");
            inv.setItem(slot, Utils.createItem(config.getConfigurationSection(key)));
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        ItemStack clicked = inv.getItem(slot);
        if (clicked == null) {
            return;
        }
        
        String meta = Utils.getItemID(clicked);
        if (meta == null) {
            return;
        }
        
        double days = Double.parseDouble(meta.split("-")[0]);
        new ConfirmAction("Challenge " + target.getName() + " in " + days + "d", player, this, result -> {
            if (result == null || !result) {
                return;
            }

            inv.close();
            if (target.getMembers().isEmpty() || target.hasShield()) {
                player.sendMessage(Utils.toComponent("&cThe target kingdom either bought a shield or no longer exists..."));
                return;
            }
            
            // Setup Kingdom challenge
            long timeleft = (long) (days * 1000 * 60 * 60 * 24);
            long wartime = System.currentTimeMillis() + timeleft;
            String data = target.getId().toString() + "@" + wartime;
            attacker.getMetadata().put(UltimaAddons.lckh, new StandardKingdomMetadata(data));
            target.getChallenges().put(attacker.getId(), wartime);
            
            // Log in audit logs
            KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(player);
            attacker.log(new LogKingdomChallenger(target, kp, wartime));
            target.log(new LogKingdomChallenged(attacker, kp, wartime));
            
            // Send warning messages and close GUIs
            for (Player p : attacker.getOnlineMembers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 1, 1.2F);
                p.sendMessage(Utils.toComponent("&e" + player.getName() + " &chas declared war on &e" + target.getName() + 
                        "&c, with &6" + days + " &cday(s) of preparation!"));
                Utils.closeInventory(p, "Challenge", "Shields");
            }
            
            for (Player p : target.getOnlineMembers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 1, 1.2F);
                p.sendMessage(Utils.toComponent("&e" + player.getName() + " &cfrom &e" + attacker.getName() + 
                        " &chas declared war on your kingdom, with &6" + days + " &cday(s) of preparation!"));
                Utils.closeInventory(p, "Challenge", "Shields");
            }

            Utils.discord(":scroll: " + player.getName() + " from **" + attacker.getName() + "** has declared war on **" + 
                    target.getName() + "**, with " + days + " day(s) of preparation!");
            
            // Setup reminders
            Utils.setupReminders(attacker, target, timeleft);
        });
        
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

}
