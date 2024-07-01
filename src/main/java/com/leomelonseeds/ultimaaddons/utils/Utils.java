package com.leomelonseeds.ultimaaddons.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.kingdoms.config.KingdomsConfig;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.location.SimpleChunkLocation;
import org.kingdoms.constants.land.structures.Structure;
import org.kingdoms.constants.metadata.KingdomMetadata;
import org.kingdoms.constants.metadata.KingdomsObject;
import org.kingdoms.constants.metadata.StandardKingdomMetadata;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.events.lands.UnclaimLandEvent;
import org.kingdoms.utils.time.TimeFormatter;

import com.cryptomorin.xseries.XItemStack;
import com.leomelonseeds.ultimaaddons.UltimaAddons;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class Utils {
    
    public static Map<UUID, UUID> chalreminders = new HashMap<>(); // Attacker, Defender (since attacker can only challenge 1)

    public static void setupReminders(Kingdom k, Kingdom target, long timeleft) {
        if (chalreminders.containsKey(k.getId())) {
            return;
        }
        chalreminders.put(k.getId(), target.getId());
        String aname = k.getName();
        String tname = target.getName();
        
        // Remind 1 min before
        int oneminremind = (int) (timeleft / 1000 - 60);
        reminder(oneminremind, k, target, (a, t) -> {
            warAnnounce(a, t, true, p -> {
                p.playSound(p.getLocation(), Sound.ENTITY_GHAST_SCREAM, SoundCategory.MASTER, 1, 1);
                p.sendMessage(toComponent("&cThere is &61 minute left &cuntil war between &e" + aname + " &cand &e" + tname + " &cstarts!"));
            }, null, null, null);
        });
        
        // Wartime announcement
        int wartime = (int) timeleft / 1000; // seconds until war
        reminder(wartime, k, target, (a, t) -> {
            warAnnounce(a, t, true, p -> {
                p.playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_2, SoundCategory.MASTER, 1, 0.8F);
                p.sendMessage(toComponent("&cWar between &e" + aname + " &cand &e" + tname +
                        " &chas begun! Each kingdom has &6" + getWarTime() / 1000 / 3600 + " hours &cto &4/k invade &ceach other's lands."));
            }, null, p -> {
                p.sendMessage(toComponent("&cWar between &e" + aname + " &cand &e" + tname +" &chas begun!"));
            }, ":bangbang: War between **" + aname + "** and **" + tname + "** has begun");
        });
        
        // Wartime over announcement
        int wartimeover = wartime + (int) (getWarTime() / 1000);
        reminder(wartimeover, k, target, (a, t) -> {
            warAnnounce(a, t, true, p -> {
                p.playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_6, SoundCategory.MASTER, 1, 1);
                p.sendMessage(toComponent("&cWar between &e" + aname + " &cand &e" + tname + " &chas ended!"));
            }, null, p -> {
                p.sendMessage(toComponent("&cWar between &e" + aname + " &cand &e" + tname + " &chas ended!"));
            }, ":checkered_flag: War between **" + aname + "** and **" + tname + "** has ended");
            chalreminders.remove(a.getId());
        });
    }
    
    private static void reminder(int time, Kingdom attacker, Kingdom target, BiConsumer<Kingdom, Kingdom> announce) {
        if (time <= 0) {
            return;
        }
        
        schedule(time * 20, () -> {
            if (!chalreminders.containsKey(attacker.getId())) {
                return;
            }
            
            if (attacker.getMembers().isEmpty() || target.getMembers().isEmpty()) {
                chalreminders.remove(attacker.getId());
                return;
            }
            
            announce.accept(attacker, target);
        });
    }
    
    /**
     * Helper method to schedule a task to run ticks later
     * 
     * @param ticks
     * @param r
     * @return the task
     */
    public static BukkitTask schedule(int ticks, Runnable r) {
        return Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> r.run(), ticks);
    }
    
    /**
     * Executes a function for each player of 2 kingdoms.
     * 
     * @param attacker
     * @param target
     * @param same if true, t can be set to null and a will be executed for both kingdoms' players
     * @param a
     * @param t
     * @param o all other players on the server. Can be null.
     * @param discord The message to send to the discord log channel. Can be null
     */
    public static void warAnnounce(Kingdom attacker, Kingdom target, boolean same, Consumer<Player> a, Consumer<Player> t, Consumer<Player> o, String discord) {
        List<Player> ap = attacker.getOnlineMembers();
        List<Player> tp = target.getOnlineMembers();
        ap.forEach(p -> a.accept(p));
        if (same) {
            tp.forEach(p -> a.accept(p));
        } else {
            tp.forEach(p -> t.accept(p));
        }
        
        if (o != null) {
            List<Player> all = new ArrayList<>(Bukkit.getOnlinePlayers());
            all.removeAll(ap);
            all.removeAll(tp);
            all.forEach(p -> o.accept(p));
        }
        
        if (discord != null) {
            discord += " \n ► ||" + generateMentions(attacker) + "||";
            discord += " \n ► ||" + generateMentions(target) + "||";
            discord(discord);
        }
    }
    
    private static String generateMentions(Kingdom k) {
        AccountLinkManager acm = DiscordSRV.getPlugin().getAccountLinkManager();
        String ret = "";
        for (String id : acm.getManyDiscordIds(k.getMembers()).values()) {
            ret += "<@" + id + "> ";
        }
        
        if (ret.isBlank()) {
            return "No linked accounts!";
        }
        
        return ret;
    }
    
    
    // Kingdoms config constants
    public static long getNewbieTime() {
        return KingdomsConfig.CREATION_KINGDOMS_NEWBIE_PROTECTION.getManager().getTimeMillis();
    }
    
    public static long getWarTime() {
        return KingdomsConfig.Invasions.CHALLENGES_DURATION.getManager().getTimeMillis();
    }
    
    /**
     * Check if a Kingdom is currently challenging or has been
     * challenged by another kingdom
     * 
     * @param k
     * @return
     */
    public static boolean hasChallenged(Kingdom k) {
        long wartime = Utils.getWarTime();
        long ctime = System.currentTimeMillis();
        String lastChallenge = Utils.getLastChallenge(k);
        if (lastChallenge != null) {
            String[] lcs = lastChallenge.split("@");
            if (Kingdom.getKingdom(UUID.fromString(lcs[0])) != null 
                    && ctime < Long.valueOf(lcs[1]) + wartime) {
                return true;
            }
        }
        
        for (Entry<UUID, Long> e : k.getChallenges().entrySet()) {
            if (Kingdom.getKingdom(e.getKey()) != null && 
                    ctime < e.getValue() + wartime) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Unclaim all associated lands given outpost land
     * @param <T>
     * 
     * @param l
     * @return the number of unclaimed lands
     */
    public static <T> int unclaimOutpost(KingdomPlayer kp, Kingdom k, KingdomsObject<T> l) {
        KingdomMetadata outpostdata = l.getMetadata().get(UltimaAddons.outpost_id);
        if (outpostdata == null) {
            return 0;
        }
        
        long outpostid = ((StandardKingdomMetadata) outpostdata).getLong();
        Set<SimpleChunkLocation> toUnclaim = new HashSet<>();
        k.getLands().forEach(kl -> {
            KingdomMetadata kld = kl.getMetadata().get(UltimaAddons.outpost_id);
            if (kld == null) {
                return;
            }

            if (((StandardKingdomMetadata) kld).getLong() != outpostid) {
                return;
            }
            
            toUnclaim.add(kl.getLocation());
        });
        
        if (toUnclaim.size() == 0) {
            return 0;
        }
        
        Bukkit.getScheduler().runTask(UltimaAddons.getPlugin(), () -> {
           k.unclaim(new HashSet<>(toUnclaim), kp, UnclaimLandEvent.Reason.ADMIN, kp != null);
        });
        
        return toUnclaim.size();
    }
    
    /**
     * Attempts to find a land with an outpost
     * 
     * @param set
     * @return null if not found
     */
    public static Land getOutpost(Set<SimpleChunkLocation> set) {
        for (SimpleChunkLocation scl : set) {
            Land l = scl.getLand();
            if (l == null) {
                continue;
            }
            
            for (Structure s : l.getStructures().values()) {
                if (s.getNameOrDefault().equals("Outpost")) {
                    return l;
                }
            }
        }
        
        return null;
    }
    
    public static String getLastChallenge(Kingdom k) {
        StandardKingdomMetadata skm = (StandardKingdomMetadata) k.getMetadata().get(UltimaAddons.lckh);
        return skm == null ? null : skm.getString();
    }
    
    /**
     * Fetch the next time the kingdom may buy a shield.
     * Only use if the kingdom has bought a shield before
     * 
     * @param k
     * @return
     */
    public static long getNextShield(Kingdom k) {
        KingdomMetadata smeta = k.getMetadata().get(UltimaAddons.shield_time);
        if (smeta == null) {
            return 0;
        }
        
        return ((StandardKingdomMetadata) smeta).getLong();
    }
    
    public static boolean isNew(Kingdom k) {
        long since = k.getSince();
        long ctime = System.currentTimeMillis();
        return ctime < since + getNewbieTime();
    }
    
    public static String timeUntilNotNew(Kingdom k) {
        long since = k.getSince();
        long ctime = System.currentTimeMillis();
        return formatDate(since + getNewbieTime() - ctime);
    }
    
    public static void discord(String s) {
        if (!UltimaAddons.getPlugin().getConfig().getBoolean("enable-discord")) {
            return;
        }
        
        TextChannel warChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("war");
        warChannel.sendMessage(s).queue();
    }
    
    public static boolean isInventoryFull(Player player) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Close inventory player which contain titles
     * 
     * @param k
     * @param titles
     */
    public static void closeInventory(Player p, String... titles) {
        if (p == null || !p.isOnline()) {
            return;
        }
        
        String ctitle = Utils.toPlain(p.getOpenInventory().title());
        for (String t : titles) {
            // Stop chat confirm from happening
            if (t.equals("Challenge")) {
                ChatConfirm pc = ChatConfirm.instances.get(p);
                if (pc != null && pc.getReq().equals("confirm")) {
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
    
    /**
     * Gets the square distance from 0,0 of this location,
     * i.e. distance along the greatest axis
     * 
     * @param loc
     * @return
     */
    public static double getDistanceFromSpawn(Location loc) {
        return Math.max(Math.abs(loc.getX()), Math.abs(loc.getZ()));
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
     *  Convert all ampersands to section symbols
     * 
     * @param s
     * @return
     */
    public static String convertAmps(String s) {
        return s.replaceAll("&", "§");
    }
    
    /**
     * Get a line, translate it to a component.
     * 
     * @param line
     * @return
     */
    public static Component toComponent(String line) {
        return LegacyComponentSerializer.legacySection().deserialize(convertAmps(line)).decoration(TextDecoration.ITALIC, false);
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
    
    /**
     * Short function for sending player a message
     * 
     * @param p
     * @param s
     */
    public static void msg(Player p, String s) {
        p.sendMessage(toComponent(s));
    }
    
    public static String formatDate(long i) {
        return TimeFormatter.ofRaw(i);
    }
    
    /**
     * Send a sound
     * 
     * @param path
     * @param location
     */
    public static void sendSound(Sound sound, float volume, float pitch, Location location) {
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, volume, pitch);
    }
    
    /**
     * Create a non-armorset item
     * 
     * @param sec
     * @return
     */
    public static ItemStack createItem(ConfigurationSection sec) {
        return createItem(sec, sec.getName());
    }
    
    /**
     * Create an item from the config section, adding
     * persistent data for the string and a path to 
     * account for armor sets. If a key "durability"
     * is set, adds a persistent data for that too.
     * 
     * @param config
     * @param path the path String to add to the item
     * @return
     */
    public static ItemStack createItem(ConfigurationSection sec, String path) {
        ItemStack i = XItemStack.deserialize(sec, s -> ChatColor.translateAlternateColorCodes('&', s));
        ItemMeta meta = i.getItemMeta();
        meta.getPersistentDataContainer().set(UltimaAddons.itemKey, PersistentDataType.STRING, path);
        if (sec.contains("durability")) {
            int dura = sec.getInt("durability");
            meta.getPersistentDataContainer().set(UltimaAddons.duraKey, PersistentDataType.STRING, dura + "/" + dura);
        }
        i.setItemMeta(meta);
        return i;
    }
    
    /**
     * Returns null if item is null, does not have meta,
     * or does not have ultima persistent data container
     * 
     * @param i
     * @return
     */
    public static String getItemID(ItemStack i) {
        return getItemID(i, UltimaAddons.itemKey);
    }

    public static String getItemID(ItemStack i, NamespacedKey key) {
        if (i == null || i.getItemMeta() == null) {
            return null;
        }
        
        ItemMeta meta = i.getItemMeta();
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return null;
        }
        
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
    
}
