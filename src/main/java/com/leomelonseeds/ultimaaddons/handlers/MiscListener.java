package com.leomelonseeds.ultimaaddons.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import me.clip.placeholderapi.PlaceholderAPI;

/**
 * Used for various game mechanics and staff logger
 */
public class MiscListener implements Listener {

    private static Map<Player, String> msgs = new HashMap<>();
    private static Set<Player> elytraCancelling = new HashSet<>();

    // Stop people from using normal fireworks
    @EventHandler
    public void onElytraBoost(PlayerElytraBoostEvent e) {
        ItemStack item = e.getItemStack();
        Player p = e.getPlayer();
        String id = Utils.getItemID(item);
        if (id == null || !id.equals("dragonfirework")) {
            e.setCancelled(true);
            msg(p, "&cYou need &dDragon Fireworks &cto elytra boost");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
        }

        if (p.getWorld().getEnvironment() == Environment.THE_END) {
            Firework firework = e.getFirework();
            FireworkMeta fmeta = firework.getFireworkMeta();
            fmeta.setPower(3);
            firework.setFireworkMeta(fmeta);
        }
    }

    // Stop elytras while in the rain
    @EventHandler
    public void onElytra(EntityToggleGlideEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) {
            return;
        }

        if (!e.isGliding()) {
            return;
        }

        Player p = (Player) e.getEntity();
        if (!p.isInRain()) {
            return;
        }

        e.setCancelled(true);
        msg(p, "&cElytras cannot be used in the rain");
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
    }

    // Warn players of elytra disabling while in the rain
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        UltimaAddons pl = UltimaAddons.getPlugin();
        Player p = e.getPlayer();
        if (elytraCancelling.contains(p)) {
            return;
        }

        if (!p.isInRain() || !p.isGliding()) {
            return;
        }

        elytraCancelling.add(p);
        Bukkit.getScheduler().runTaskLater(pl, () -> {
            p.sendMessage(Utils.toComponent("&cYou feel your elytra weaken as the rain starts..."));
            p.playSound(p.getLocation(), Sound.ENCHANT_THORNS_HIT, 2F, 0.8F);
            if (!p.isGliding()) {
                elytraCancelling.remove(p);
                return;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!p.isOnline() || p.getLocation().add(0, -1, 0).getBlock().getType() != Material.AIR) {
                        elytraCancelling.remove(p);
                        this.cancel();
                        return;
                    }
                    p.setGliding(false);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 25, 0, true, false));
                }
            }.runTaskTimer(pl, 20, 20);
        }, 20);
    }

    // Stop items with hide_enchant flag being used in grindstone
    @EventHandler
    public void onGrindstone(PrepareGrindstoneEvent e) {
        GrindstoneInventory gi = e.getInventory();
        if (e.getResult() == null) {
            return;
        }

        for (ItemStack i : new ItemStack[]{gi.getUpperItem(), gi.getLowerItem()}) {
            if (i == null || !i.hasItemMeta()) {
                continue;
            }

            ItemMeta meta = i.getItemMeta();
            if (meta.getItemFlags().contains(ItemFlag.HIDE_ENCHANTS)) {
                e.setResult(null);
                return;
            }
        }
    }

    // Log Staff
    @EventHandler
    public void listen(PlayerCommandPreprocessEvent e) {
        // No need to log non-staff members
        Player p = e.getPlayer();
        if (!p.hasPermission("group.helper") && !p.hasPermission("group.builder")) {
            return;
        }

        // Get first arg
        String cmd = e.getMessage();
        String arg1 = cmd.split(" ")[0].substring(1);
        String base = arg1.replace("/", "");
        if (base.isBlank()) {
            return;
        }

        // Check if is logged command
        List<String> logged = UltimaAddons.getPlugin().getConfig().getStringList("staff-log");
        if (!logged.contains(base)) {
            return;
        }

        String group = PlaceholderAPI.setPlaceholders(p, "%vault_group_capital%");
        Location loc = p.getLocation();
        String locStr = "[" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "]";
        
        try {
            if (Objects.requireNonNull(Bukkit.getPluginCommand(arg1)).getPlugin().getName().equals("WorldEdit")) {
                LocalSession playerSession = Objects.requireNonNull(WorldEdit.getInstance().getSessionManager().findByName(p.getName()));
                BlockVector3 min = playerSession.getSelection(playerSession.getSelectionWorld()).getMinimumPoint();
                BlockVector3 max = playerSession.getSelection(playerSession.getSelectionWorld()).getMaximumPoint();
                locStr += " [selection: " + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() + " to";
                locStr += " " + max.getBlockX() + ", " + max.getBlockY() + ", " + max.getBlockZ() + "]";
                locStr += " [selection: none]";
            }
        } catch (Exception ex) {
            // Do nothing
        }
        
        String msg = "**" + p.getName() + "** (" + group + ") at " + locStr + " used command `" + cmd + "`";
        TextChannel logChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-log");
        logChannel.sendMessage(msg).queue();
    }

    private void msg(Player p, String msg) {
        String cur = msgs.get(p);
        if (cur != null && cur.equals(msg)) {
            return;
        }

        msgs.put(p, msg);
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> msgs.remove(p), 100);
        p.sendMessage(Utils.toComponent(msg));
    }
}
