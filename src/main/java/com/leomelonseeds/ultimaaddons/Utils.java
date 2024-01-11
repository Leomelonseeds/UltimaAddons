package com.leomelonseeds.ultimaaddons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.metadata.StandardKingdomMetadata;
import org.kingdoms.utils.time.TimeFormatter;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Utils {
    
    private static Map<UUID, UUID> chalreminders = new HashMap<>(); // Attacker, Defender (since attacker can only challenge 1)
    
    public static String getLastChallenge(Kingdom k) {
        StandardKingdomMetadata skm = (StandardKingdomMetadata) k.getMetadata().get(UltimaAddons.lckh);
        return skm == null ? null : skm.getString();
    }
    
    /**
     * Fetch the next time the kingdom may buy a shield.
     * Only use if the kingdom currently has a shield.
     * 
     * @param k
     * @return
     */
    public static long getNextShield(Kingdom k) {
        return k.getShieldSince() + k.getShieldTime() * 2;
    }
    
    public static boolean isNew(Kingdom k) {
        long since = k.getSince();
        long ctime = System.currentTimeMillis();
        return ctime < since + UltimaAddons.NEWBIE_TIME;
    }
    
    public static String timeUntilNotNew(Kingdom k) {
        long since = k.getSince();
        long ctime = System.currentTimeMillis();
        return formatDate(since + UltimaAddons.NEWBIE_TIME - ctime);
    }
    
    public static void discord(String s) {
        TextChannel warChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("war");
        warChannel.sendMessage(s).queue();
    }
    
    /**
     * Close inventory player which contain titles
     * 
     * @param k
     * @param titles
     */
    public static void closeInventory(Player p, String... titles) {
        String ctitle = Utils.toPlain(p.getOpenInventory().title());
        for (String t : titles) {
            // Stop chat confirm from happening
            if (t.equals("Challenge")) {
                ChatConfirm pc = ChatConfirm.instances.get(p);
                if (pc != null) {
                    pc.stop();
                }
            }
            
            if (!ctitle.contains(t)) {
                continue;
            }
            
            p.closeInventory();
            return;
        }
    }
    
    public static void setupReminders(Kingdom k, Kingdom target, long timeleft) {
        if (chalreminders.containsKey(k.getId())) {
            return;
        }
        chalreminders.put(k.getId(), target.getId());
        
        // Wartime announcement
        int wartime = (int) timeleft / 1000; // seconds until war
        if (wartime > 0) {
            Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
                if (k.getMembers().isEmpty() || target.getMembers().isEmpty()) {
                    return;
                }
                
                List<Player> involved = k.getOnlineMembers();
                involved.addAll(target.getOnlineMembers());
                for (Player q : involved) {
                    q.playSound(q.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_2, SoundCategory.MASTER, 1, 0.8F);
                    q.sendMessage(toComponent("&cWar between &e" + k.getName() + " &cand &e" + target.getName() +
                            " &chas begun! Each kingdom has &6" + UltimaAddons.WAR_TIME / 1000 / 3600 + " hours &cto &4/k invade &ceach other's lands."));
                    discord(":bangbang: War between **" + k.getName() + "** and **" + target.getName() + "** has begun");
                }
            }, wartime * 20);
        }
        
        // Wartime over announcement
        int wartimeover = wartime + (int) (UltimaAddons.WAR_TIME / 1000);
        if (wartimeover > 0) {
            Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
                if (k.getMembers().isEmpty() || target.getMembers().isEmpty()) {
                    return;
                }
                
                List<Player> involved = k.getOnlineMembers();
                involved.addAll(target.getOnlineMembers());
                for (Player q : involved) {
                    q.playSound(q.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_6, SoundCategory.MASTER, 1, 1);
                    q.sendMessage(toComponent("&cWar between &e" + k.getName() + " &cand &e" + target.getName() + " &chas ended!"));
                    discord(":checkered_flag: War between **" + k.getName() + "** and **" + target.getName() + "** has ended");
                }
            }, wartimeover * 20);
        }
        
        // Remind 1 min before
        int oneminremind = (int) (timeleft / 1000 - 60);
        if (oneminremind > 0) {
            Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
                if (k.getMembers().isEmpty() || target.getMembers().isEmpty()) {
                    return;
                }
                
                List<Player> involved = k.getOnlineMembers();
                involved.addAll(target.getOnlineMembers());
                for (Player q : involved) {
                    q.playSound(q.getLocation(), Sound.ENTITY_GHAST_SCREAM, SoundCategory.MASTER, 1, 1);
                    q.sendMessage(toComponent("&cThere is &61 minute left &cuntil war between &e" + k.getName() + " &cand &e" + target.getName() + " &cstarts!"));
                }
            }, oneminremind * 20);
        }
    }
    
    /**
     * Component to plain text, keeping section color codes!
     * 
     * @param component
     * @return
     */
    public static String toPlain(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
    
    /**
     *  Converted all section symbols to ampersands cause for some reason
     *  section symbols just would not work properly
     * 
     * @param s
     * @return
     */
    public static String convertAmps(String s) {
        return s.replaceAll("§", "&");
    }
    
    /**
     * Get a line, translate it to a component.
     * 
     * @param line
     * @return
     */
    public static Component toComponent(String line) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(convertAmps(line)).decoration(TextDecoration.ITALIC, false);
    }
    
    /**
     * Get lines to translate to components
     * 
     * @param line
     * @return
     */
    public static List<Component> toComponent(List<String> lines) {
        List<Component> result = new ArrayList<>();
        for (String s : lines) {
            result.add(toComponent(s));
        }
        return result;
    }
    
    public static String formatDate(long i) {
        return TimeFormatter.ofRaw(i);
    }
    
    /**
     * Create an item from the config section
     * 
     * @param config
     * @return
     */
    public static ItemStack createItem(ConfigurationSection sec) {
        ItemStack item = new ItemStack(Material.valueOf(sec.getString("item")));
        ItemMeta meta = item.getItemMeta();
        if (sec.contains("name")) {
            meta.displayName(toComponent(sec.getString("name")));
        }
        if (sec.contains("lore")) {
            meta.lore(toComponent(sec.getStringList("lore")));
        }
        item.setItemMeta(meta);
        if (sec.contains("glow")) {
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return item;
    }
}
