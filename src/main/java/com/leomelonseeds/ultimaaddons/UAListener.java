package com.leomelonseeds.ultimaaddons;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.kingdoms.config.KingdomsConfig;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.abstraction.data.KingdomItemBuilder;
import org.kingdoms.constants.land.location.SimpleChunkLocation;
import org.kingdoms.constants.land.location.SimpleLocation;
import org.kingdoms.constants.land.structures.Structure;
import org.kingdoms.constants.land.structures.StructureRegistry;
import org.kingdoms.constants.land.structures.StructureStyle;
import org.kingdoms.constants.land.structures.StructureType;
import org.kingdoms.constants.metadata.KingdomMetadata;
import org.kingdoms.constants.metadata.StandardKingdomMetadata;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.constants.player.StandardKingdomPermission;
import org.kingdoms.events.general.GroupDisband;
import org.kingdoms.events.general.GroupShieldPurchaseEvent;
import org.kingdoms.events.general.KingdomCreateEvent;
import org.kingdoms.events.general.KingdomDisbandEvent;
import org.kingdoms.events.general.KingdomPacifismStateChangeEvent;
import org.kingdoms.events.invasion.KingdomInvadeEndEvent;
import org.kingdoms.events.invasion.KingdomInvadeEvent;
import org.kingdoms.events.items.KingdomItemBreakEvent;
import org.kingdoms.events.lands.ClaimLandEvent;
import org.kingdoms.events.members.KingdomLeaveEvent;
import org.kingdoms.main.Kingdoms;
import org.kingdoms.managers.invasions.Plunder;
import org.kingdoms.managers.land.indicator.LandVisualizer;
import org.kingdoms.services.managers.ServiceHandler;
import org.kingdoms.utils.nbt.ItemNBT;
import org.kingdoms.utils.nbt.NBTType;
import org.kingdoms.utils.nbt.NBTWrappers;

import com.leomelonseeds.ultimaaddons.invs.InventoryManager;
import com.leomelonseeds.ultimaaddons.invs.UAInventory;

public class UAListener implements Listener {
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInvasionStart(KingdomInvadeEvent e) {
        new InvasionHandler(e.getInvasion());
    }
    
    // Close GUIs to stop bad things from happening
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShield(GroupShieldPurchaseEvent e) {
        if (!(e.getGroup() instanceof Kingdom)) {
            return;
        }
        
        // Register next latest time to buy shield
        Kingdom k = (Kingdom) e.getGroup();
        long shieldtime = System.currentTimeMillis() + e.getShieldDuration() * 2;
        k.getMetadata().put(UltimaAddons.shield_time, new StandardKingdomMetadata(shieldtime));
        Utils.discord(":shield: **" + k.getName() + "** has activated a shield for " + Utils.formatDate(e.getShieldDuration()));
        
        // Close other shield buyers to stop abuse
        k.getOnlineMembers().forEach(p -> Utils.closeInventory(p, "Shields", "Challenge"));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisband(KingdomDisbandEvent e) {
        Kingdom k = e.getKingdom();
        k.getOnlineMembers().forEach(p -> Utils.closeInventory(p, "Challenge"));
        if (e.getReason() == GroupDisband.Reason.INVASION) {
            Utils.discord(":dart: **" + k.getName() + "** was disbanded because their nexus chunk was captured");
        } else {
            Utils.discord(":pencil: **" + k.getName() + "** has been disbanded");
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreate(KingdomCreateEvent e) {
        Kingdom k = e.getKingdom();
        Utils.discord(":fleur_de_lis: **" + k.getName() + "** has been founded");
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(KingdomLeaveEvent e) {
        Utils.closeInventory(e.getPlayer().getPlayer(), "Challenge");
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPacifist(KingdomPacifismStateChangeEvent e) {
        Kingdom k = e.getKingdom();
        if (e.isPacifist()) {
            Utils.discord(":peace: **" + k.getName() + "** is a pacifist kingdom");
        } else {
            Utils.discord(":fire: **" + k.getName() + "** is now an aggressor kingdom");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOutpostBreak(KingdomItemBreakEvent<Structure> e) {
        if (!(e.getKingdomItem() instanceof Structure)) {
            return;
        }
        
        Structure structure = e.getKingdomItem();
        if (!structure.getNameOrDefault().equals("Outpost")) {
            return;
        }
        
        if (e.getPlayer().getPlayer().isFlying()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onOutpostPlace(PlayerInteractEvent e) {
        // Check if item is kingdom item
        Block b = e.getClickedBlock();
        if (b == null) {
            return;
        }
        
        // Must be a right click (place) action 
        if (!e.getAction().toString().contains("RIGHT")) {
            return;
        }
        
        // Must be able to place
        Block pb = b.getRelative(e.getBlockFace());
        if (pb.getType() != Material.AIR) {
            return;
        }
        
        ItemStack item = e.getItem();
        NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(item);
        nbt = nbt.getCompound(Kingdoms.NBT);
        if (nbt == null) {
            return;
        }
        
        // Check if its a structure
        String tag = nbt.get(StructureType.METADATA, NBTType.STRING);
        if (!tag.equals("outpost")) {
            return;
        }

        // Hack to not let the method pass to Kingdoms by setting it to air
        // This works probably because the event stores a copy of the item
        // so when you set the type it doesn't affect the player(?)
        e.setCancelled(true);
        Player p = e.getPlayer();
        Material type = item.getType();
        item.setType(Material.AIR);
        
        // Only allow outpost to be placed on unclaimed land not in the end
        SimpleChunkLocation scl = SimpleChunkLocation.of(pb);
        if (p.getWorld().getName().equals("world_the_end") || ServiceHandler.isInRegion(scl)) {
            message(p, "&cYou cannot create an outpost here!");
            return;
        }
        
        // Check if land is claimed.
        Land land = Land.getLand(scl);
        if (land == null) {
            land = new Land(scl);
        }
        
        if (land.isClaimed()) {
            message(e.getPlayer().getPlayer(), "&cYou can only place outposts in unclaimed land!");
            return;
        }
        
        // Must have kingdom
        KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(p);
        if (!kp.hasKingdom()) {
            message(p, "&cYou must be in a kingdom to use this!");
            return;
        }
        
        // Must have appropriate perms
        if (!kp.hasPermission(StandardKingdomPermission.CLAIM) || 
            !kp.hasPermission(StandardKingdomPermission.STRUCTURES)) {
            message(p, "&cYour kingdom rank must have both CLAIM and STRUCTURES permissions to create an outpost!");
            return;
        }
        
        // Must have a nexus
        Kingdom k = kp.getKingdom();
        if (k.getNexus() == null) {
            message(p, "&cYou must place your nexus using &a/k nexus &cbefore you can claim more lands!");
            return;
        }
        
        // Must have less than 3 placed outposts
        if (k.getAllStructures().stream().filter(s -> s.getNameOrDefault().equals("Outpost"))
                .count() >= UltimaAddons.MAX_OUTPOSTS) {
            message(p, "&cYour kingdom has already reached its outpost limit!");
            return;
        }
        
        // Must be less than max lands
        if (k.getLands().size() >= k.getMaxClaims()) {
            message(p, "&cYour kingdom has already reached its claim limit!");
            return;
        }
        
        pb.setType(type);
        
        // Kingdoms spawn structure
        SimpleLocation sl = SimpleLocation.of(pb);
        k.claim(scl, kp, ClaimLandEvent.Reason.CLAIMED);
        StructureStyle outpostStyle = StructureRegistry.getStyle("outpost");
        Structure outpost = outpostStyle.getType().build(
                new KingdomItemBuilder<Structure, StructureStyle, StructureType>(outpostStyle, SimpleLocation.of(pb), kp));
        land.getStructures().put(sl, outpost);
        outpost.spawnHolograms(k);
        outpost.playSound("place");
        outpost.displayParticle("place");
        message(p, "&2Claimed an outpost land at &6" + scl.getX() + "&7, &6" + scl.getZ());
        
        // Add metadata
        // ID is simply cur time, no way 2 people put an outpost at the same milisecond...
        long id = System.currentTimeMillis();
        land.getMetadata().put(UltimaAddons.outpost_id, new StandardKingdomMetadata(id));
        outpost.getMetadata().put(UltimaAddons.outpost_id, new StandardKingdomMetadata(id));
        
        // Remove item amount
        ItemStack hand = p.getInventory().getItem(e.getHand());
        hand.setAmount(hand.getAmount() - 1);
        
        // Visualize lands
        new LandVisualizer().forPlayer(p, kp).forLand(land, scl.toChunk()).displayIndicators();
    }

    // Only allow claiming lands if nexus was placed
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLandClaim(ClaimLandEvent e) {
        // Allow invasion claims still
        if (e.getReason() == ClaimLandEvent.Reason.INVASION) {
            return;
        }

        // Allow claiming if currently kingdom has 0 lands
        Kingdom k = e.getKingdom();
        if (k.getLands().size() == 0) {
            return;
        }
        
        // Must have a nexus
        Player p = e.getPlayer().getPlayer();
        if (k.getNexus() == null) {
            e.setCancelled(true);
            message(p, "&cYou must place your nexus using &a/k nexus &cbefore you can claim more lands!");
            return;
        }
        
        // Find outpost metadata and add it if available
        Set<SimpleChunkLocation> chunks = e.getLandLocations();
        Bukkit.getLogger().log(Level.INFO, "Size: " + chunks.size()); 
        Bukkit.getScheduler().runTaskLaterAsynchronously(UltimaAddons.getPlugin(), () -> {
            UUID kid = k.getId();
            Set<SimpleChunkLocation> checked = new HashSet<>();
            long outpost_id = 0;
            for (SimpleChunkLocation scl : chunks) {
                Land cl = scl.getLand();
                if (cl == null) {
                    continue;
                }
                
                UUID claimedby = cl.getKingdomId();
                if (claimedby == null || !claimedby.equals(kid)) {
                    continue;
                }

                // Don't check if the claiming chunk already has an outpost id
                KingdomMetadata cldata = cl.getMetadata().get(UltimaAddons.outpost_id);
                if (cldata != null) {
                    continue;
                }
                
                // Iterate through bordering chunks
                for (SimpleChunkLocation sclnear : scl.getChunksAround(1)) {
                    // Continue if this was one of the recently claimed lands
                    if (chunks.contains(sclnear)) {
                        continue;
                    }
                    
                    // Continue if we already checked this land before
                    if (checked.contains(sclnear)) {
                        continue;
                    }
                    
                    // Continue if the land is unclaimed, or not claimed by same kingdom
                    Land scll = sclnear.getLand();
                    if (scll == null) {
                        checked.add(sclnear);
                        continue;
                    }
                    
                    UUID nearclaim = scll.getKingdomId();
                    if (nearclaim == null || !claimedby.equals(kid)) {
                        checked.add(sclnear);
                        continue;
                    }
                    
                    // If any surrounding land doesn't have metadata, it means its a nexus land
                    KingdomMetadata data = scll.getMetadata().get(UltimaAddons.outpost_id);
                    if (data == null) {
                        Bukkit.getLogger().log(Level.INFO, "Found nexus land! Returning..."); 
                        return;
                    }
                    
                    // Otherwise use the first outpost id
                    outpost_id = ((StandardKingdomMetadata) data).getLong();
                    break;
                }
                
                if (outpost_id != 0) {
                    break;
                }
            }

            Bukkit.getLogger().log(Level.INFO, "Found outpost id with timestamp " + outpost_id); 
        }, 1);
    }
    
    // Challenge reminder
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Send all messages with a slight delay so they appear last
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
            Player p = e.getPlayer();
            KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(p);
            if (!kp.hasKingdom()) {
                return;
            }
            
            long wartime = Utils.getWarTime();
            Kingdom k = kp.getKingdom();
            long ctime = System.currentTimeMillis();
            for (Entry<UUID, Long> challenge : k.getChallenges().entrySet()) {
                Kingdom attacker = Kingdom.getKingdom(challenge.getKey());
                if (attacker == null) {
                    continue;
                }
                
                if (ctime > challenge.getValue() + wartime) {
                    continue;
                }
                
                if (ctime < challenge.getValue()) {
                    message(p, "&cYour kingdom has &e" + Utils.formatDate(challenge.getValue() - ctime) + 
                            " &cto prepare for war with &e" + attacker.getName());
                } else {
                    message(p, "&4&l[!] &c&lYour kingdom is currently at war with &e&l" + attacker.getName() + "&c&l!");
                }
                
            }
            
            String lastChallenge = Utils.getLastChallenge(k);
            if (lastChallenge != null) {
                String[] slck = lastChallenge.split("@");
                long lcd = Long.valueOf(slck[1]);
                Kingdom target = Kingdom.getKingdom(UUID.fromString(slck[0]));
                if (target == null) {
                    return;
                }
                
                if (ctime > lcd + wartime) {
                    return;
                }

                long timeleft = lcd - ctime;
                
                if (timeleft > 0) {
                    message(p, "&cYour kingdom has &e" + Utils.formatDate(timeleft) + 
                            " &cto prepare for war with &c" + target.getName());
                } else {
                    message(p, "&4&l[!] &c&lYour kingdom is currently at war with &e&l" + target.getName() + "&c&l!");
                }
                
                Utils.setupReminders(k, target, timeleft);
            }
        }, 5);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInvasionSuccess(KingdomInvadeEndEvent e) {
        if (!(e.getInvasion() instanceof Plunder)) {
            return;
        }

        // Only process successful invasion
        Plunder invasion = (Plunder) e.getInvasion();
        if (!invasion.getResult().isSuccessful()) {
            return;
        }
        
        // Don't end invasion due to champion death
        // If the invasion was successful and capture progress < capture goal, the champion must have been the end cause
        // Plunder kill/death has been set to a high enough amount to not matter
        if (invasion.getCaptureProgress() < KingdomsConfig.Invasions.PLUNDER_CAPTURE_PROGRESS_GOAL.getManager().getDouble()) {
            e.setCancelled(true);
            return;
        }
        
        // Play custom sounds and send custom messages
        // Do everything after a tick to let Kingdoms do its thing first
        Kingdom defender = invasion.getDefender();
        Kingdom attacker = invasion.getAttacker();
        long defrp = defender.getResourcePoints();
        SimpleLocation nexus = defender.getNexus();
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
            if (nexus != null && invasion.getAffectedLands().stream().
                    anyMatch(land -> nexus.toSimpleChunkLocation().equals(land))) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    message(p, "&e" + defender.getName() + " &cwas disbanded due to an invasion from &e" + attacker.getName() + "&c!");
                });
            } else {
                long rp = defrp / defender.getLands().size();
                defender.getOnlineMembers().forEach(p -> {
                    p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.MASTER, 1, 0.8F);
                    message(p, "&cYour kingdom lost &6" + rp + " &cresource points.");
                });
            }
            
            invasion.getAttacker().getOnlineMembers().forEach(p -> {
                p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.MASTER, 1, 0.8F);
                message(p, "&2Your kingdom gained &6" + defrp + " &2resource points.");
            });
        }, 1);
    }
    
    /** Handle clicking of custom GUIs */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        InventoryManager manager = UltimaAddons.getPlugin().getInvs();
        
        if (!(manager.getInventory(player) instanceof UAInventory)) {
            return;
        }
        
        Inventory inv = event.getClickedInventory();
        
        if (inv == null) {
            return;
        }
        
        if (inv.equals(event.getView().getBottomInventory()) && event.getClick().isShiftClick()) {
            event.setCancelled(true);
            return;
        }
        
        if (!inv.equals(event.getView().getTopInventory())){
            return; 
        }
        
        event.setCancelled(true);

        manager.getInventory(player).registerClick(event.getSlot(), event.getClick());
    }
    
    /** Unregister custom mwinventories when they are closed. */
    @EventHandler
    public void unregisterCustomInventories(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        
        // Unregister
        InventoryManager manager = UltimaAddons.getPlugin().getInvs();
        if (manager.getInventory(player) instanceof UAInventory) {
            manager.removePlayer(player);
        }
    }
	
	// Prevent message spam
	private void message(Player p, String m) {
	    p.sendMessage(Utils.toComponent(m));
	}
}
