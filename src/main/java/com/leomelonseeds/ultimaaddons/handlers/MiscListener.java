package com.leomelonseeds.ultimaaddons.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.platform.bukkit.location.BukkitImmutableLocation;
import org.kingdoms.server.location.ImmutableLocation;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.objects.UASkills;
import com.leomelonseeds.ultimaaddons.utils.Utils;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.user.SkillsUser;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.advancedplugins.ae.api.AEAPI;
import net.kyori.adventure.text.Component;

/**
 * Used for various game mechanics and staff logger
 */
public class MiscListener implements Listener {

    private static final double DEFAULT_MINECART_SPEED = 0.4;
    
    private static Map<Player, String> msgs = new HashMap<>();
    private static Set<Player> elytraCancelling = new HashSet<>();
    
    // Respawn at bed location if it exists, otherwise go k home, otherwise let essentials do the job
    @EventHandler(priority = EventPriority.HIGH)
    public final void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (player.getRespawnLocation() != null) {
            return;
        }
        
        KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(event.getPlayer());
        if (!kp.hasKingdom()) {
            return;
        }
        
        ImmutableLocation home = kp.getKingdom().getHome();
        if (home == null) {
            return;
        }
        
        event.setRespawnLocation(BukkitImmutableLocation.from(home));
    }
    
    // Minecart speed on copper
    // Thanks to https://github.com/ergor/hsrails/blob/master/src/main/java/no/netb/mc/hsrails/MinecartListener.java
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart cart)) {
            return;
        }
        
        Location cartLocation = cart.getLocation();
        World cartsWorld = cart.getWorld();
        Block rail = cartsWorld.getBlockAt(cartLocation);
        if (rail.getType() != Material.POWERED_RAIL) {
            return;
        }
        
        String type = cartsWorld.getBlockAt(cartLocation.add(0, -1, 0)).getType().toString();
        if (!type.contains("COPPER") || type.contains("ORE") || type.contains("RAW")) {
            cart.setMaxSpeed(DEFAULT_MINECART_SPEED);
            return;
        }
        
        if (type.contains("OXIDIZED")) {
            cart.setMaxSpeed(DEFAULT_MINECART_SPEED * 6);
        } else if (type.contains("WEATHERED")) {
            cart.setMaxSpeed(DEFAULT_MINECART_SPEED * 5.33);
        } else if (type.contains("EXPOSED")) {
            cart.setMaxSpeed(DEFAULT_MINECART_SPEED * 4.67);
        } else {
            cart.setMaxSpeed(DEFAULT_MINECART_SPEED * 4);
        }
    }
    
    // Cave noise when moving between safe/mob scaled zones
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Location fromLoc = e.getFrom();
        int border = fromLoc.getWorld().getEnvironment() == Environment.NETHER ? 2048 : 4096;
        double from = Utils.getDistanceFromSpawn(fromLoc);
        double to = Utils.getDistanceFromSpawn(e.getTo());
        if (from < border && to > border) {
            Player player = e.getPlayer();
            player.playSound(player, Sound.AMBIENT_CAVE, 1F, 1F);
            msg(player, "&cYou crossed the halfway point of this world. Mobs and loot will get stronger from here. Good luck.");
            return;
        } 
        
        if (from > border && to < border) {
            msg(e.getPlayer(), "&aEntering world center. Mobs and loot are now vanilla strength.");
            return;
        }
    }
    
    // Abiding skill application, let player know death location
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
        int abiding = user.getAbilityLevel(UASkills.ABIDING);
        if (abiding <= 0) {
            return;
        }
        
        List<ItemStack> drops = e.getDrops();
        double chance = UASkills.ABIDING.getValue(abiding) / 100.0;
        for (ItemStack drop : new ArrayList<>(drops)) {
            String id = Utils.getItemID(drop);
            if (id != null && id.equals("introbook")) {
                e.getDrops().remove(drop);
                e.getItemsToKeep().add(drop);
                continue;
            }
            
            // Binomial distribution calculator by pjs on stackoverflow
            double log_q = Math.log(1.0 - chance);
            int amount = drop.getAmount();
            int keepAmount = 0;
            double sum = 0;
            while (true) {
                sum += Math.log(Math.random()) / (amount - keepAmount);
                if (sum < log_q) {
                   break;
                }
                keepAmount++;
            }
            
            if (keepAmount >= 1) {
                ItemStack keptDrop = new ItemStack(drop);
                drop.setAmount(drop.getAmount() - keepAmount);
                keptDrop.setAmount(keepAmount);
                e.getItemsToKeep().add(keptDrop);
                continue;
            }
            
            // Only check for soulbound if item is not kept by Abiding
            int soulbound = AEAPI.getEnchantmentsOnItem(drop).getOrDefault("soulbound", 0);
            if (soulbound <= 0) {
                continue;
            }

            drops.remove(drop);
            ItemStack boundDrop = new ItemStack(drop);
            AEAPI.removeEnchantment(boundDrop, "soulbound");
            if (soulbound > 1) {
                AEAPI.applyEnchant("soulbound", soulbound - 1, boundDrop);
            }
            
            e.getItemsToKeep().add(boundDrop);
        }
    }
    
    // CREEPERSHOT (from UMW)
    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        
        if (event.getBow().getType() != Material.CROSSBOW) {
            return;
        }
        
        ItemStack crossbow = event.getBow();
        CrossbowMeta cmeta = (CrossbowMeta) crossbow.getItemMeta();
        EntityType type = null;
        for (ItemStack proj : cmeta.getChargedProjectiles()) {
            if (proj.getType() != Material.FIREWORK_ROCKET || !proj.hasItemMeta()) {
                return;
            }
            
            ItemMeta meta = proj.getItemMeta();
            if (!meta.hasLore()) {
                return;
            }
            
            List<Component> lore = meta.lore();
            type = EntityType.fromName(Utils.toPlain(lore.get(0)));
            if (type != null) {
                break;
            }
        }
        
        if (type == null) {
            return;
        }
        
        Entity proj = event.getProjectile();
        Location spawnLoc = proj.getLocation();
        Entity mob = spawnLoc.getWorld().spawnEntity(spawnLoc, type);
        mob.setVelocity(proj.getVelocity().multiply(2.5));
        proj.remove();
        
        try {
            Sound sound = Sound.valueOf("ENTITY_" + type + "_DEATH");
            Utils.sendSound(sound, 2F, 2F, spawnLoc);
        } catch (IllegalArgumentException e) {
            // Do nothing
        }
        return;
    }
    
    // Handle mobslinger
    @EventHandler
    public void onCrossbowLoad(EntityLoadCrossbowEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        
        ItemStack crossbow = event.getCrossbow();
        if (!AEAPI.hasCustomEnchant("mobslinger", crossbow)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        ItemStack offhand = player.getInventory().getItemInOffHand();
        String mat = offhand.getType().toString();
        if (!mat.contains("SPAWN_EGG")) {
            return;
        }
        
        // Prepare fake creeper item
        String mobName = mat.replace("_SPAWN_EGG", "");
        String displayName = WordUtils.capitalize(mobName.replace('_', ' ').toLowerCase());
        offhand.setAmount(offhand.getAmount() - 1);
        ItemStack creeper = new ItemStack(Material.FIREWORK_ROCKET);
        ItemMeta meta = creeper.getItemMeta();
        meta.displayName(Utils.toComponent("&f" + displayName));
        meta.lore(List.of(new Component[] {Utils.toComponent(mobName)}));
        creeper.setItemMeta(meta);
        
        // Load "creeper" into crossbow
        Utils.schedule(1, () -> {
            CrossbowMeta cmeta = (CrossbowMeta) crossbow.getItemMeta();
            List<ItemStack> newProjs = new ArrayList<>();
            for (int i = 0; i < cmeta.getChargedProjectiles().size(); i++) {
                newProjs.add(new ItemStack(creeper));
            }
            cmeta.setChargedProjectiles(newProjs);
            crossbow.setItemMeta(cmeta);
        });
    }
    
    // Disable dragon firework right click block interaction
    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        String id = Utils.getItemID(e.getItem());
        if (id == null || !id.equals("dragonfirework")) {
            return;
        }
        
        e.setCancelled(true);
    }

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
    public void onGlide(PlayerMoveEvent e) {
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
        if (!Utils.isInstalled("DiscordSRV")) {
            return;
        }
        
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
        if (!logged.contains(base) && !cmd.contains("/k admin")) {
            return;
        }

        String group = PlaceholderAPI.setPlaceholders(p, "%vault_group_capital%");
        Location loc = p.getLocation();
        String locStr = "[" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "]";

        // If it is a WorldEdit command, try to get player's current selection
        if (WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager().getCommandManager().containsCommand(arg1)) {
            try {
                LocalSession playerSession = Objects.requireNonNull(WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p)));
                String pos1 = playerSession.getSelection(playerSession.getSelectionWorld()).getBoundingBox().getPos1().toParserString();
                String pos2 = playerSession.getSelection(playerSession.getSelectionWorld()).getBoundingBox().getPos2().toParserString();
                locStr += " [(" + pos1.replace(",", ", ") + ") to (" + pos2.replace(",", ", ") + ")]";
            } catch (Exception ex) {
                // Do nothing as there is no selection
            }
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
