package com.leomelonseeds.ultimaaddons.utils;

import com.cryptomorin.xseries.XItemStack;
import com.leomelonseeds.ultimaaddons.UltimaAddons;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
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

import java.util.*;
import java.util.Map.Entry;

public class Utils {

    private static Map<UUID, UUID> chalreminders = new HashMap<>(); // Attacker, Defender (since attacker can only challenge 1)

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
     *
     * @param <T>
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
        StandardKingdomMetadata skm = (StandardKingdomMetadata) k.getMetadata().get(UltimaAddons.lckH);
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
                    chalreminders.remove(k.getId());
                    return;
                }

                List<Player> involved = k.getOnlineMembers();
                involved.addAll(target.getOnlineMembers());
                for (Player q : involved) {
                    q.playSound(q.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_2, SoundCategory.MASTER, 1, 0.8F);
                    q.sendMessage(toComponent("&cWar between &e" + k.getName() + " &cand &e" + target.getName() +
                            " &chas begun! Each kingdom has &6" + getWarTime() / 1000 / 3600 + " hours &cto &4/k invade &ceach other's lands."));
                }

                discord(":bangbang: War between **" + k.getName() + "** and **" + target.getName() + "** has begun");
            }, wartime * 20);
        }

        // Wartime over announcement (if this method is called it must be positive)
        int wartimeover = wartime + (int) (getWarTime() / 1000);
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
            chalreminders.remove(k.getId());
            if (k.getMembers().isEmpty() || target.getMembers().isEmpty()) {
                return;
            }

            List<Player> involved = k.getOnlineMembers();
            involved.addAll(target.getOnlineMembers());
            for (Player q : involved) {
                q.playSound(q.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_6, SoundCategory.MASTER, 1, 1);
                q.sendMessage(toComponent("&cWar between &e" + k.getName() + " &cand &e" + target.getName() + " &chas ended!"));
            }

            discord(":checkered_flag: War between **" + k.getName() + "** and **" + target.getName() + "** has ended");
        }, wartimeover * 20);

        // Remind 1 min before
        int oneminremind = (int) (timeleft / 1000 - 60);
        if (oneminremind > 0) {
            Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
                if (k.getMembers().isEmpty() || target.getMembers().isEmpty()) {
                    chalreminders.remove(k.getId());
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
     * Convert all ampersands to section symbols
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
     * account for armor sets.
     *
     * @param config
     * @param path   the path String to add to the item
     * @return
     */
    public static ItemStack createItem(ConfigurationSection sec, String path) {
        ItemStack i = XItemStack.deserialize(sec, s -> ChatColor.translateAlternateColorCodes('&', s));
        ItemMeta meta = i.getItemMeta();
        meta.getPersistentDataContainer().set(UltimaAddons.itemKey, PersistentDataType.STRING, path);
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
        if (i == null || i.getItemMeta() == null) {
            return null;
        }

        ItemMeta meta = i.getItemMeta();
        if (!meta.getPersistentDataContainer().has(UltimaAddons.itemKey, PersistentDataType.STRING)) {
            return null;
        }

        return meta.getPersistentDataContainer().get(UltimaAddons.itemKey, PersistentDataType.STRING);
    }
}
