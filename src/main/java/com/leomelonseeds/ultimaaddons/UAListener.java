package com.leomelonseeds.ultimaaddons;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
import org.kingdoms.constants.metadata.StandardKingdomMetadata;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.constants.player.StandardKingdomPermission;
import org.kingdoms.events.general.GroupShieldPurchaseEvent;
import org.kingdoms.events.general.KingdomCreateEvent;
import org.kingdoms.events.general.KingdomDisbandEvent;
import org.kingdoms.events.general.KingdomPacifismStateChangeEvent;
import org.kingdoms.events.invasion.KingdomInvadeEndEvent;
import org.kingdoms.events.invasion.KingdomInvadeEvent;
import org.kingdoms.events.items.KingdomItemBreakEvent;
import org.kingdoms.events.lands.ClaimLandEvent;
import org.kingdoms.events.lands.UnclaimLandEvent.Reason;
import org.kingdoms.events.members.KingdomLeaveEvent;
import org.kingdoms.main.Kingdoms;
import org.kingdoms.managers.PvPManager;
import org.kingdoms.managers.invasions.Plunder;
import org.kingdoms.utils.nbt.ItemNBT;
import org.kingdoms.utils.nbt.NBTType;
import org.kingdoms.utils.nbt.NBTWrappers;

import com.leomelonseeds.ultimaaddons.invs.InventoryManager;
import com.leomelonseeds.ultimaaddons.invs.UAInventory;

public class UAListener implements Listener {
    
    private static Map<Player, String> lastMessage = new HashMap<>();
    
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
        Utils.discord(":pencil: **" + k.getName() + "** has been disbanded");
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreate(KingdomCreateEvent e) {
        Kingdom k = e.getKingdom();
        Utils.discord(":fleur_de_lis: **" + k.getName() + "** was founded!");
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
        
        Bukkit.getLogger().log(Level.INFO, "Outpost broken");
    }

    @EventHandler
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

        // Only allow outpost to be placed on unclaimed land
        Player p = e.getPlayer();
        Land land = Land.getLand(b.getLocation());
        if (land != null) {
            p.sendMessage(Utils.toComponent("&cYou can only place outposts in unclaimed land!"));
            return;
        }
        
        e.setCancelled(true);
        KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(p);
        Kingdom k = kp.getKingdom();
        
        // Must have kingdom
        if (k == null) {
            p.sendMessage(Utils.toComponent("&cYou must be in a kingdom to use this!"));
            return;
        }
        
        // Must have appropriate perms
        if (!kp.hasPermission(StandardKingdomPermission.CLAIM) || 
            !kp.hasPermission(StandardKingdomPermission.STRUCTURES)) {
            p.sendMessage(Utils.toComponent("&cYou must have both CLAIM and STRUCTURES permissions to create an outpost!"));
            return;
        }
        
        // Must have less than 3 placed outposts
        if (k.getAllStructures().stream().filter(s -> s.getNameOrDefault().equals("Outpost"))
                .count() >= UltimaAddons.MAX_OUTPOSTS) {
            p.sendMessage(Utils.toComponent("&cYour kingdom has already reached its outpost limit!"));
            return;
        }
        
        // Must be less than max lands
        if (k.getLands().size() >= k.getMaxClaims()) {
            p.sendMessage(Utils.toComponent("&cYour kingdom has already reached its claim limit!"));
            return;
        }

        // Handle block and item settings
        item.setAmount(item.getAmount() - 1);
        pb.setType(item.getType());
        
        // Kingdoms spawn structure
        SimpleLocation sl = SimpleLocation.of(pb);
        Land claim = k.claim(SimpleChunkLocation.of(pb), kp, ClaimLandEvent.Reason.CLAIMED).getLands().iterator().next();
        StructureStyle outpostStyle = StructureRegistry.getStyle("outpost");
        Structure outpost = outpostStyle.getType().build(
                new KingdomItemBuilder<Structure, StructureStyle, StructureType>(outpostStyle, SimpleLocation.of(pb), kp));
        claim.getStructures().put(sl, outpost);
        outpost.spawnHolograms(k);
        
        // Add metadata
        // ID is simply cur time, no way 2 people put an outpost at the same milisecond...
        long id = System.currentTimeMillis();
        claim.getMetadata().put(UltimaAddons.outpost_id, new StandardKingdomMetadata(id));
        outpost.getMetadata().put(UltimaAddons.outpost_id, new StandardKingdomMetadata(id));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLandClaim(ClaimLandEvent e) {
        Kingdom k = e.getKingdom();
        if (k.getLands().size() == 0) {
            return;
        }
        
        if (!k.getAllStructures().stream().anyMatch(s -> s.getNameOrDefault().equals("Nexus"))) {
            e.setCancelled(true);
            e.getPlayer().getPlayer().sendMessage(Utils.toComponent("&cYou must place your nexus using &a/k nexus &cbefore you can claim more lands!"));
        }
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
            
            Kingdom k = kp.getKingdom();
            long ctime = System.currentTimeMillis();
            for (Entry<UUID, Long> challenge : k.getChallenges().entrySet()) {
                Kingdom attacker = Kingdom.getKingdom(challenge.getKey());
                if (attacker == null) {
                    continue;
                }
                
                if (ctime > challenge.getValue() + UltimaAddons.WAR_TIME) {
                    continue;
                }
                
                if (ctime < challenge.getValue()) {
                    p.sendMessage(Utils.toComponent("&cYour kingdom has &e" + Utils.formatDate(challenge.getValue() - ctime) + 
                            " &cto prepare for war with &e" + attacker.getName()));
                } else {
                    p.sendMessage(Utils.toComponent("&4&l[!] &c&lYour kingdom is currently at war with &e&l" + attacker.getName() + "&c&l!"));
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
                
                if (ctime > lcd + UltimaAddons.WAR_TIME) {
                    return;
                }

                long timeleft = lcd - ctime;
                
                if (timeleft > 0) {
                    p.sendMessage(Utils.toComponent("&cYour kingdom has &e" + Utils.formatDate(timeleft) + 
                            " &cto prepare for war with &c" + target.getName()));
                } else {
                    p.sendMessage(Utils.toComponent("&4&l[!] &c&lYour kingdom is currently at war with &e&l" + target.getName() + "&c&l!"));
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
        
        // Unclaim all lands if nexus land was invaded.
        // Play custom sounds and send custom messages either way
        // Do everything after a tick to let Kingdoms do its thing first
        Kingdom defender = invasion.getDefender();
        SimpleLocation nexus = defender.getNexus();
        Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
            if (nexus != null && invasion.getAffectedLands().stream().
                    anyMatch(land -> nexus.toSimpleChunkLocation().equals(land))) {
                // Unclaim all defender lands
                defender.unclaim(new HashSet<>(defender.getLandLocations()), null, Reason.INVASION, false);
                
                defender.getPlayerMembers().forEach(op -> {
                    Player p = op.getPlayer();
                    if (p == null) {
                        return;
                    }
                    
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, SoundCategory.MASTER, 1, 1);
                    message(p, "&4&l[!] &c&lYour kingdom's nexus chunk was invaded and all lands were unclaimed. "
                            + "All your resource points were transferred to the enemy.");
                });

                Kingdom attacker = invasion.getAttacker();
                invasion.getAttacker().getPlayerMembers().forEach(op -> {
                    Player p = op.getPlayer();
                    if (p == null) {
                        return;
                    }
                    
                    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1, 1);
                    message(p, "&2&l[!] &a&lYou invaded your enemy's nexus chunk and all their lands were unclaimed. "
                            + "All enemy resource points were transferred to your kingdom.");
                });
                
                Utils.discord(":dart: **" + defender.getName() + "**'s nexus chunk was captured by **" + 
                        attacker.getName() + "**, and all their land was unclaimed!");
            } else {
                long rp = defender.getResourcePoints() / defender.getLands().size();
                defender.getPlayerMembers().forEach(op -> {
                    Player p = op.getPlayer();
                    if (p == null) {
                        return;
                    }
                    
                    p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.MASTER, 1, 0.8F);
                    message(p, "&cYour kingdom lost &6" + rp + " &cresource points.");
                });
                
                invasion.getAttacker().getPlayerMembers().forEach(op -> {
                    Player p = op.getPlayer();
                    if (p == null) {
                        return;
                    }
                    
                    p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.MASTER, 1, 0.8F);
                    message(p, "&2Your kingdom gained &6" + rp + " &2resource points.");
                });
            }
        }, 1);
    }
	
    // Show message for pacifist pvp
    @EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDamage(EntityDamageByEntityEvent e) {
        // Must be cancelled already
        if (!e.isCancelled()) {
            return;
        }
        
	    // Player only
	    if (!(e.getEntity() instanceof Player)) {
	        return;
	    }
	    
	    // NPC check
		Player victim = (Player) e.getEntity();
		if (victim.hasMetadata("NPC")) {
		    return;
		}
		
		// Damager projectile check
		Entity entityDamager = e.getDamager();
		Player damager;
		if (entityDamager instanceof Projectile) {
			Projectile p = (Projectile) entityDamager;
			if (!(p.getShooter() instanceof Player)) {
			    return;
			}
            damager = (Player) p.getShooter();
		} else if (entityDamager instanceof Player) {
			damager = (Player) entityDamager;
		} else {
			return;
		}
		
		// Add extra restrictions if they already can fight
		if (!PvPManager.canFight(damager, victim)) {
		    return;
		}
			
		KingdomPlayer damagerKp = KingdomPlayer.getKingdomPlayer(damager);
		KingdomPlayer victimKp = KingdomPlayer.getKingdomPlayer(victim);
		if (damagerKp.hasKingdom() && victimKp.hasKingdom()) {
			Kingdom damagerKingdom = damagerKp.getKingdom();
			Kingdom victimKingdom = victimKp.getKingdom();
			if (damagerKingdom.isPacifist() && !victimKingdom.isPacifist()) {
				if (!damagerKp.isPvp()) {
					message(damager, "&7You are pacifist, and have PVP disabled! Use &a/k pvp &7to turn it on.");
				}
			} else if (!damagerKingdom.isPacifist() && victimKingdom.isPacifist()) {
				if (!victimKp.isPvp()) {
					message(damager, "&7The player you are attacking is pacifist, and has PVP disabled.");
				}
			} else if (damagerKingdom.isPacifist() && victimKingdom.isPacifist()) {
				if (!(victimKp.isPvp() && damagerKp.isPvp())) {
					message(damager, "&7Pacifist players must both have PVP enabled using &a/k pvp &7to fight.");
				}
			}
		} else if (damagerKp.hasKingdom() && !victimKp.hasKingdom()) {
			if (!damagerKp.isPvp() && damagerKp.getKingdom().isPacifist()) {
				message(damager, "&7You are pacifist, and have PVP disabled! Use &a/k pvp &7to turn it on.");
			}
		} else if (!damagerKp.hasKingdom() && victimKp.hasKingdom()) {
			if (!victimKp.isPvp() && victimKp.getKingdom().isPacifist()) {
				message(damager, "&7The player you are attacking is in a pacifist Kingdom and has PVP disabled.");
			}
		}
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
	    if (lastMessage.get(p) != null && lastMessage.get(p).equals(m)) {
	        return;
	    }
	    
	    p.sendMessage(Utils.toComponent(m));
	    lastMessage.put(p, m);
	    Bukkit.getScheduler().runTaskLater(UltimaAddons.getPlugin(), () -> {
	        lastMessage.remove(p);
	    }, 60);
	}
}
