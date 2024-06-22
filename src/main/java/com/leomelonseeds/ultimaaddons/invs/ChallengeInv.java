package com.leomelonseeds.ultimaaddons.invs;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.logs.misc.challenge.LogKingdomChallenged;
import org.kingdoms.constants.group.model.logs.misc.challenge.LogKingdomChallenger;
import org.kingdoms.constants.group.model.relationships.KingdomRelation;
import org.kingdoms.constants.metadata.StandardKingdomMetadata;
import org.kingdoms.constants.player.KingdomPlayer;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class ChallengeInv extends UAInventory {
    
    private Kingdom target;
    private Kingdom attacker;
    private Player player;
    private boolean deactivateShield;
    
    public ChallengeInv(Kingdom target, Kingdom attacker, Player player, boolean deactivateShield) {
        super(player, 27, "&8-=( &cChallenge &e" + target.getName() + " &8)=-");
        this.target = target;
        this.attacker = attacker;
        this.player = player;
        this.deactivateShield = deactivateShield;
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
        new ConfirmAction("Challenge " + target.getName() + " in " + days + "d", player, null, result -> {
            if (result == null || !result) {
                return;
            }

            if (target.getMembers().isEmpty() || target.hasShield()) {
                player.sendMessage(Utils.toComponent("&cThe target kingdom either bought a shield or no longer exists..."));
                return;
            }

            // Check cost
            int cost = attacker.getLands().size();
            long current = attacker.getResourcePoints();
            if (current < cost) {
                CommandUtils.sendErrorMsg(player, "Declaring war costs &e" + cost + " &cresource points for your kingdom! You currently have &e" + current + "&c.");
                return;
            }
            
            // Deactivate shields
            if (deactivateShield) {
                attacker.deactivateShield();
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
            
            // Make the Kingdoms enemies
            if (attacker.getRelationWith(target) != KingdomRelation.ENEMY) {
                attacker.setRelationShipWith(target, KingdomRelation.ENEMY);
            }
            
            // Send warning messages
            String prep = Utils.formatDate(timeleft);
            long wartimesec = wartime / 1000;
            Utils.warAnnounce(attacker, target, false, p -> {
                p.sendMessage(Utils.toComponent("&e" + player.getName() + " &chas declared war on &e" + target.getName() + 
                        "&c, with &6" + prep + " &cof preparation!"));
            }, p -> {
                p.sendMessage(Utils.toComponent("&e" + player.getName() + " &cfrom &e" + attacker.getName() + 
                        " &chas declared war on your kingdom, with &6" + prep + " &cof preparation!"));
            }, p -> {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 1, 1.2F);
                p.sendMessage(Utils.toComponent("&e" + player.getName() + " &cfrom &e" + attacker.getName() + 
                        " &chas declared war on &e" + target.getName() + "&c, with &6" + prep + " &cof preparation!"));
            }, ":scroll: " + player.getName() + " from **" + attacker.getName() + "** has declared war on **" + 
                    target.getName() + "**, with " + prep + " of preparation! War starts <t:" + wartimesec + ":f>");

            // For both kingdoms, send dragon growl and close inventories
            Utils.warAnnounce(attacker, target, true, p -> {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 1, 1.2F);
                p.sendMessage(Utils.toComponent("&7War can be cancelled by requesting a neutral, truce, or ally relation."));
                Utils.closeInventory(p, "Challenge", "Shields");
            }, null, null, null);
            
            Utils.setupReminders(attacker, target, timeleft);
        });
        
    }
}
