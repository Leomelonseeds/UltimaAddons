package com.leomelonseeds.ultimaaddons;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

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
import org.kingdoms.events.lands.NexusMoveEvent;
import org.kingdoms.events.lands.UnclaimLandEvent;
import org.kingdoms.events.members.KingdomLeaveEvent;
import org.kingdoms.main.Kingdoms;
import org.kingdoms.managers.invasions.Plunder;
import org.kingdoms.managers.land.indicator.LandVisualizer;
import org.kingdoms.services.managers.ServiceHandler;
import org.kingdoms.utils.nbt.ItemNBT;
import org.kingdoms.utils.nbt.NBTType;
import org.kingdoms.utils.nbt.NBTWrappers;

import com.leomelonseeds.ultimaaddons.invs.ConfirmAction;
import com.leomelonseeds.ultimaaddons.invs.InventoryManager;
import com.leomelonseeds.ultimaaddons.invs.UAInventory;

public class UAListener implements Listener {
    
    // -------------------------------------------------
    // EXTRA DISCORDSRV MESSAGES
    // -------------------------------------------------
    
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
    public void onPacifist(KingdomPacifismStateChangeEvent e) {
        Kingdom k = e.getKingdom();
        if (e.isPacifist()) {
            Utils.discord(":peace: **" + k.getName() + "** is a pacifist kingdom");
        } else {
            Utils.discord(":fire: **" + k.getName() + "** is now an aggressor kingdom");
        }
    }
    
    

    // -------------------------------------------------
    // INVASION HANDLERS
    // -------------------------------------------------

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
        SimpleLocation nexus = defender.getNexus();
        Set<SimpleChunkLocation> affected = invasion.getAffectedLands(); // This should be a size 1 set
        Land outpost = Utils.getOutpost(affected);
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
            // Send messages
            long loss = defender.getResourcePoints();
            if (nexus != null && affected.stream().anyMatch(land -> nexus.toSimpleChunkLocation().equals(land))) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    message(p, "&e" + defender.getName() + " &cwas disbanded due to an invasion from &e" + attacker.getName() + "&c!");
                });
            } else {
                int extra = 1;
                if (outpost != null) {
                    extra += Utils.unclaimOutpost(null, defender, outpost);
                }
                
                loss = loss * extra / (defender.getLandLocations().size() + 1);
                long floss = loss;
                defender.getOnlineMembers().forEach(p -> {
                    p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.MASTER, 1, 0.8F);
                    if (outpost != null) {
                        message(p, "&cYour outpost land was invaded, and all claims made from the outpost have been lost.");
                    }
                    message(p, "&cYour kingdom lost &6" + floss + " &cresource points.");
                });
                
                defender.addResourcePoints(-1 * floss);
            }
            
            long floss = loss;
            attacker.getOnlineMembers().forEach(p -> {
                p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.MASTER, 1, 0.8F);
                if (outpost != null) {
                    message(p, "&2You invaded an enemy outpost, and all enemy claims made from that outpost were unclaimed.");
                }
                message(p, "&2Your kingdom gained &6" + floss + " &2resource points.");
            });
            
            attacker.addResourcePoints(floss);
        }, 1);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInvasionStart(KingdomInvadeEvent e) {
        new InvasionHandler(e.getInvasion());
    }

    

    // -------------------------------------------------
    // CUSTOM OUTPOST HANDLERS
    // -------------------------------------------------
    
    private static Set<Structure> justRemoved = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOutpostBreak(KingdomItemBreakEvent<Structure> e) {
        if (!(e.getKingdomItem() instanceof Structure)) {
            return;
        }
        
        Structure structure = e.getKingdomItem();
        if (!structure.getNameOrDefault().equals("Outpost")) {
            return;
        }
        
        // Stop recursion
        if (justRemoved.contains(structure)) {
            justRemoved.remove(structure);
            return;
        }
        
        // Allow if structure was removed by Kingdoms or a player w/o kingdom
        KingdomPlayer kp = e.getPlayer();
        if (kp == null || !kp.hasKingdom()) {
            return;
        }
        
        // Allow if structure has no land (if kingdom did unclaimall or disbanded)
        if (!structure.getLand().isClaimed()) {
            return;
        }

        e.setCancelled(true);
        
        // Must not be in war
        Player p = kp.getPlayer();
        if (Utils.hasChallenged(kp.getKingdom())) {
            message(p, "&cYou cannot do this as you either challenged or have been challenged by another kingdom.");
            return;
        }
        
        if (!kp.hasPermission(StandardKingdomPermission.UNCLAIM)) {
            message(p, "&cYour kingdom rank must have UNCLAIM permissions to remove outposts!");
            return;
        }
        
        // For some reason without the 1 tick delay it skips the confirmation screen
        p.closeInventory();
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
            new ConfirmAction("Unclaim Outpost Lands", p, null, result -> {
                if (result == null || !result) {
                    return;
                }
                
                justRemoved.add(structure);
                structure.remove();
                int amt = Utils.unclaimOutpost(kp, kp.getKingdom(), structure);
                message(p, "&2You unclaimed &6" + amt + " &2land(s).");
            });
        }, 1);
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
        if (k.getAllStructures().stream().filter(s -> s.getNameOrDefault().equals("Outpost")).count() >= 
                StructureRegistry.getStyle("outpost").getOption("limits", "total").getInt()) {
            message(p, "&cYour kingdom has already reached its outpost limit!");
            return;
        }
        
        // Must be less than max lands
        if (k.getLandLocations().size() >= k.getMaxClaims()) {
            message(p, "&cYour kingdom has already reached its claim limit!");
            return;
        }
        
        // Must not be in war
        if (Utils.hasChallenged(k)) {
            message(p, "&cYou cannot use this as you either challenged or have been challenged by another kingdom.");
            return;
        }
        
        pb.setType(type);
        
        // Kingdoms spawn structure
        SimpleLocation sl = SimpleLocation.of(pb);
        k.claim(scl, kp, ClaimLandEvent.Reason.ADMIN);
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
        StandardKingdomMetadata skm = new StandardKingdomMetadata(System.currentTimeMillis());
        land.getMetadata().put(UltimaAddons.outpost_id, skm);
        outpost.getMetadata().put(UltimaAddons.outpost_id, skm);
        
        // Remove item amount
        ItemStack hand = p.getInventory().getItem(e.getHand());
        hand.setAmount(hand.getAmount() - 1);
        
        // Visualize lands
        new LandVisualizer().forPlayer(p, kp).forLand(land, scl.toChunk()).displayIndicators();
    }

    // Only allow claiming lands if nexus was placed/add outpost IDs
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLandClaim(ClaimLandEvent e) {
        // Allow claiming if currently kingdom has 0 lands
        Kingdom k = e.getKingdom();
        if (k.getLandLocations().size() == 0) {
            return;
        }
        
        // Also returns if this land was claimed through means of an outpost
        if (e.getReason() == ClaimLandEvent.Reason.ADMIN) {
            return;
        }
        
        // Must have a nexus
        Player p = e.getPlayer().getPlayer();
        if (k.getNexus() == null) {
            e.setCancelled(true);
            message(p, "&cYou must place your nexus using &a/k nexus &cbefore you can claim more lands!");
            return;
        }
        
        // Disable claiming if there are no other claims in the same world
        Set<SimpleChunkLocation> chunks = e.getLandLocations();
        if (!k.getLandLocations().stream().anyMatch(scl -> scl.getWorld().equals(p.getWorld().getName()))) {
            e.setCancelled(true);
            message(p, "&cYour land must be connected to your other kingdom lands.");
            return;
        }
        
        // Find outpost metadata and add it if available
        // Assume getLandLocations only returns successful claims
        Bukkit.getScheduler().runTaskLaterAsynchronously(UltimaAddons.getPlugin(), () -> {
            UUID kid = k.getId();
            Set<SimpleChunkLocation> checked = new HashSet<>();
            long outpost_id = 0;
            for (SimpleChunkLocation scl : chunks) {
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
                    if (nearclaim == null || !nearclaim.equals(kid)) {
                        checked.add(sclnear);
                        continue;
                    }
                    
                    // If any surrounding land doesn't have metadata, it means its a nexus land
                    KingdomMetadata data = scll.getMetadata().get(UltimaAddons.outpost_id);
                    if (data == null) {
                        return;
                    }
                    
                    // Otherwise log an outpost id
                    outpost_id = ((StandardKingdomMetadata) data).getLong();
                }
                
                // This checks if this is the first chunk that a kingdom claimed during an invasion.
                // We return after because invasions can only have 1 land claim at a time.
                // Assign a negative outpost ID so that this land still works with custom disconnectLands
                // function, and also to allow nexus to be moved to an invasion spot.
                if (e.getReason() == ClaimLandEvent.Reason.INVASION && checked.size() == 8) {
                    long ctime = System.currentTimeMillis();
                    scl.getLand().getMetadata().put(UltimaAddons.outpost_id, new StandardKingdomMetadata(-1 * ctime));
                    return;
                }
            }
            
            if (outpost_id == 0) {
                return;
            }
            
            // If we got to this point, an outpost land must've been found. Then add the metadata to all claimed chunks
            long finalid = outpost_id;
            chunks.forEach(c -> c.getLand().getMetadata().put(UltimaAddons.outpost_id, new StandardKingdomMetadata(finalid)));
        }, 1);
    }
    
    // Stop unclaiming of outpost chunk
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUnclaim(UnclaimLandEvent e) {
        // This method checks if this event would unclaim an outpost
        if (cancelUnclaim(e)) {
            return;
        }
        
        // Remove metadata
        e.getLandLocations().forEach(scl -> {
            scl.getLand().getMetadata().remove(UltimaAddons.outpost_id);
        });
    }
    
    // Checks if a land can be unclaimed
    private boolean cancelUnclaim(UnclaimLandEvent e) {
        // Don't check if unclaimall was done
        if (e.getLandLocations().size() > 1) {
            return false;
        }
        
        // Don't check if cause was invasion - OnInvadeSuccess checks for that instead
        if (e.getReason() == UnclaimLandEvent.Reason.INVASION || e.getReason() == UnclaimLandEvent.Reason.ADMIN) {
            return false;
        }
        
        for (SimpleChunkLocation scl : e.getLandLocations()) {
            if (scl.getLand().getStructures().values().stream().anyMatch(s -> s.getNameOrDefault().equals("Outpost"))) {
                e.setCancelled(true);
                message(e.getPlayer().getPlayer(), "&cTo unclaim a land with an outpost on it, you must break the outpost.");
                return true;
            }
        }
        
        return false;
    }
    
    // Disallow nexus to be moved to an outpost chunk
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNexusMove(NexusMoveEvent e) {
        Land l = e.getTo().toSimpleChunkLocation().getLand();
        KingdomMetadata meta = l.getMetadata().get(UltimaAddons.outpost_id);
        if (meta == null) {
            return;
        }
        
        // Only check if meta is positive, meaning time > 0
        if (((StandardKingdomMetadata) meta).getLong() > 0) {
            e.setCancelled(true);
            message(e.getPlayer().getPlayer(), "&cYou cannot move your nexus to an outpost land.");
        }
    }
    
    

    // -------------------------------------------------
    // CUSTOM CHALLENGE HANDLERS
    // -------------------------------------------------
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(KingdomLeaveEvent e) {
        Utils.closeInventory(e.getPlayer().getPlayer(), "Challenge");
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
	
	// Shorten message call functions
	private void message(Player p, String m) {
	    p.sendMessage(Utils.toComponent(m));
	}
}
