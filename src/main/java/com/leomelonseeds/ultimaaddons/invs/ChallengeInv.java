package com.leomelonseeds.ultimaaddons.invs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.metadata.KingdomMetadataHandler;
import org.kingdoms.constants.metadata.StandardKingdomMetadata;
import org.kingdoms.constants.metadata.StandardKingdomMetadataHandler;

import com.leomelonseeds.ultimaaddons.ConfigUtils;
import com.leomelonseeds.ultimaaddons.UltimaAddons;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;

public class ChallengeInv implements UAInventory {
    
    private NamespacedKey nkey;
    private Inventory inv;
    private Kingdom target;
    private Kingdom attacker;
    private Player player;
    
    public ChallengeInv(Kingdom target, Kingdom attacker, Player player) {
        this.target = target;
        this.attacker = attacker;
        this.player = player;
        this.nkey = new NamespacedKey(UltimaAddons.getPlugin(), "time");
        
        inv = Bukkit.createInventory(null, 27, ConfigUtils.toComponent("Challenge " + target.getName()));
        manager.registerInventory(player, this);
    }

    @Override
    public void updateInventory() {
        ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redmeta = red.getItemMeta();
        redmeta.displayName(ConfigUtils.toComponent(""));
        red.setItemMeta(redmeta);
        for (int i : new int[] {0, 8, 9, 17, 18, 26}) {
            inv.setItem(i, red);
        }
        
        ConfigurationSection config = UltimaAddons.getPlugin().getConfig().getConfigurationSection("challengegui");
        for (String key : config.getKeys(false)) {
            int slot = config.getInt(key + ".slot");
            double days = config.getDouble(key + ".days");
            ItemStack citem = ConfigUtils.createItem(config.getConfigurationSection(key));
            ItemMeta cmeta = citem.getItemMeta();
            cmeta.getPersistentDataContainer().set(nkey, PersistentDataType.DOUBLE, days);
            citem.setItemMeta(cmeta);
            inv.setItem(slot, citem);
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        ItemStack clicked = inv.getItem(slot);
        if (clicked == null) {
            return;
        }
        
        ItemMeta meta = clicked.getItemMeta();
        if (!meta.getPersistentDataContainer().has(nkey)) {
            return;
        }
        
        double days = meta.getPersistentDataContainer().get(nkey, PersistentDataType.DOUBLE);
        new ConfirmAction("Battle " + target.getName() + " in " + days + "d", player, this, result -> {
            if (result == null || !result) {
                return;
            }
            
            if (target.getMembers().isEmpty()) {
                player.sendMessage(ConfigUtils.toComponent("&cThe kingdom you are trying to challenge no longer exists..."));
                return;
            }
            
            long wartime = System.currentTimeMillis() + ((long) (days * 1000 * 60 * 60 * 24));
            String data = target.getId().toString() + "@" + wartime;
            KingdomMetadataHandler lckh = new StandardKingdomMetadataHandler(UltimaAddons.LCK);
            attacker.getMetadata().put(lckh, new StandardKingdomMetadata(data));
            target.getChallenges().put(attacker.getId(), wartime);
            
            for (Player p : attacker.getOnlineMembers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 1, 1.2F);
                p.sendMessage(ConfigUtils.toComponent("&e" + player.getName() + " &chas declared war on &e" + target.getName() + 
                        "&c, with &6" + days + " &cday(s) of preparation!"));
            }
            
            for (Player p : target.getOnlineMembers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 1, 1.2F);
                p.sendMessage(ConfigUtils.toComponent("&e" + player.getName() + " &cfrom &e" + attacker.getName() + 
                        " has declared war on your kingdom, with &6" + days + " &cday(s) of preparation!"));
            }

            TextChannel discordChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("war");
            discordChannel.sendMessage(":scroll: " + player.getName() + " from **" + attacker.getName() + "** has declared war on **" + 
                    target.getName() + "**, with " + days + " day(s) of preparation!").queue();
        });
        
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

}
