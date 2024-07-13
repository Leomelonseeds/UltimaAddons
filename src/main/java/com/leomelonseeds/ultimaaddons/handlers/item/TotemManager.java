package com.leomelonseeds.ultimaaddons.handlers.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.platform.bukkit.location.BukkitImmutableLocation;
import org.kingdoms.server.location.ImmutableLocation;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.ChatConfirm;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class TotemManager implements Listener {

    public static final String TOTEM_INDICATOR = "totemofwarping";
    public static NamespacedKey totemKey;

    private UltimaAddons plugin;
    private ItemManager im;
    private ConfigurationSection totemSec;
    private Map<Player, BukkitTask> pendingTP;
    private Set<Player> pendingAccept;
    private Set<Player> damageImmune;

    public TotemManager(ItemManager im, UltimaAddons plugin) {
        this.im = im;
        this.plugin = plugin;
        this.pendingTP = new HashMap<>();
        this.pendingAccept = new HashSet<>();
        this.damageImmune = new HashSet<>();
        if (totemKey == null) {
            totemKey = new NamespacedKey(plugin, "totem");
        }
    }

    public ConfigurationSection getTotemSec() {
        return totemSec;
    }

    public Map<String, ItemStack> createTotems(ConfigurationSection sec) {
        this.totemSec = sec;
        Map<String, ItemStack> res = new HashMap<>();
        for (String key : sec.getKeys(false)) {
            String mapkey = sec.getName() + "." + key;
            ItemStack cur = Utils.createItem(sec.getConfigurationSection(key), mapkey);
            ItemMeta cmeta = cur.getItemMeta();
            cmeta.getPersistentDataContainer().set(totemKey, PersistentDataType.STRING, key);
            cur.setItemMeta(cmeta);
            res.put(mapkey, cur);
        }

        return res;
    }

    private void removePlayer(Player p, String reason) {
        if (!pendingTP.containsKey(p)) {
            return;
        }

        pendingTP.remove(p).cancel();
        if (reason != null) {
            msg(p, "&cTeleportation cancelled because " + reason + ".");
        }
    }

    private void initiateTeleportation(Player player, EquipmentSlot hand, ItemStack totem, Location loc) {
        initiateTeleportation(player, hand, totem, loc, null);
    }

    /**
     * Start a teleportation. Loc cannot be null.
     *
     * @param player
     * @param loc
     * @param safe   if true, searches upwards from loc until safe airblocks found.
     */
    private void initiateTeleportation(Player player, EquipmentSlot hand, ItemStack totem, Location loc, Player other) {
        if (pendingTP.containsKey(player)) {
            return;
        }

        Location from = player.getLocation().clone().add(0, 1, 0);
        final int TIME = 5;
        pendingTP.put(player, new BukkitRunnable() {

            int iteration = TIME;

            @Override
            public void run() {
                ItemStack curItem = player.getInventory().getItem(hand);
                if (!curItem.isSimilar(totem)) {
                    removePlayer(player, "you aren't holding the totem");
                    return;
                }

                if (iteration > 0) {
                    if (iteration == TIME) {
                        Utils.sendSound(Sound.BLOCK_BEACON_POWER_SELECT, 2F, 2F, from);
                    } else {
                        player.playSound(from, Sound.BLOCK_NOTE_BLOCK_HARP, 1F, 1F);
                    }

                    if (iteration == 2) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, true, false));
                    } else if (iteration == 1) {
                        Utils.sendSound(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2F, 0.9F, from);
                    }

                    int since = TIME - iteration;
                    if (iteration > 1) {
                        from.getWorld().spawnParticle(Particle.PORTAL, from, 50 + since * 50, 0.1, 0.1, 0.1, 0.7 + since * 0.1);
                    }
                    Utils.sendSound(Sound.BLOCK_BEACON_AMBIENT, 2F, 1.2F + since * 0.2F, from);
                    msg(player, "&bTeleporting in &f" + iteration + " &bseconds, do not move...");
                    iteration--;
                    return;
                }
                
                // Handle random TP
                if (isType(Utils.getItemID(totem, totemKey), TotemType.RANDOM)) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rtp player " + player.getName());
                    setSpawn(player, from);
                    curItem.setAmount(curItem.getAmount() - 1);
                    removePlayer(player, null);
                    this.cancel();
                    return;
                }

                // Stop other player tp if they are no longer online
                boolean playertp = other != null; // TRUE if we tp to another player
                if (playertp && !other.isOnline()) {
                    msg(player, "&f" + Utils.toPlain(other.displayName()) + " &cis no longer online!");
                    this.cancel();
                    removePlayer(player, null);
                }

                Location curLoc = playertp ? other.getLocation() : loc;
                do {
                    // Only perform safe TP if we are NOT teleporting to another player.
                    if (playertp) {
                        break;
                    }

                    // If current location is non-passable, move up until safe spot is found
                    boolean moved = false;
                    while (!(isSafe(curLoc) && isSafe(curLoc.clone().add(0, 1, 0)))) {
                        moved = true;
                        curLoc.add(0, 1, 0);
                    }

                    // If we moved player, then a safe location was found
                    if (moved) {
                        break;
                    }

                    // If the player hasn't moved, check DOWNWARDS until solid ground is found
                    while (isSafe(getGround(curLoc))) {
                        curLoc.add(0, -1, 0);
                    }
                } while (false);

                // Cancel if player would be teleported to nether roof
                // For nether roof, there are 5 layers of bedrock from 251 to 255, so any location
                // at 252 or higher has a chance of trapping player in a bedrock box
                Material ground = getGround(curLoc).getBlock().getType();
                if (curLoc.getWorld().getEnvironment() == Environment.NETHER && curLoc.getBlockY() >= 252 ||
                        ground == Material.VOID_AIR) {
                    this.cancel();
                    removePlayer(player, "the destination is unsafe");
                    return;
                }

                // Give player 40s fire resistance if TPing to lava (like undying totem)
                if (ground == Material.LAVA) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));
                }

                // Do not consume item if player in creative mode
                if (player.getGameMode() != GameMode.CREATIVE) {
                    curItem.setAmount(curItem.getAmount() - 1);
                }

                // Set yaw and pitch if tp location doesn't have them set
                if (curLoc.getYaw() == 0) {
                    curLoc.setYaw(player.getLocation().getYaw());
                    curLoc.setPitch(player.getLocation().getPitch());
                }
                
                // Damage immunity for death totems
                if (isType(Utils.getItemID(totem, totemKey), TotemType.DEATH)) {
                    damageImmune.add(player);
                    Utils.schedule(15 * 20, () -> damageImmune.remove(player));
                    msg(player, "&bYou are immune to damage for 15 seconds");
                } else {
                    msg(player, "&bTeleporting...");
                }
                
                player.teleport(curLoc);
                Utils.sendSound(Sound.ITEM_TOTEM_USE, 0.8F, 2F, curLoc);
                Utils.schedule(1, () -> curLoc.getWorld().spawnParticle(
                        Particle.DRAGON_BREATH, curLoc.clone().add(0, 1, 0), 150, 0, 0, 0, 0.3));
                this.cancel();
                removePlayer(player, null);
            }
        }.runTaskTimer(plugin, 1, 20)); // Start 1 tick later to make sure its not immediately cancelled due to movement or something
    }
    
    /**
     * Sets the player spawn, only if player is more than 10 blocks away
     * from the initial location provided. If not, then it tries again
     * after 10 ticks.
     * 
     * @param player
     * @param init
     */
    private void setSpawn(Player player, Location init) {
        Utils.schedule(10, () -> {
            Location cur = player.getLocation();
            if (cur.distance(init) < 10) {
                setSpawn(player, init);
                return;
            }
            
            player.setRespawnLocation(cur, true);
            player.sendMessage(Utils.toComponent("&fRespawn point set"));
        });
    }
    
    private boolean isSafe(Location loc) {
        if (loc.getBlockY() > 384) {
            return false;
        }
        
        // Player head for grave support
        Block block = loc.getBlock();
        if (block.getType() == Material.PLAYER_HEAD) {
            return true;
        }
        
        if (block.getType() == Material.VOID_AIR) {
            return false;
        }
        
        return block.isPassable() && !block.isLiquid();
    }

    private Location getGround(Location location) {
        return location.clone().add(0, -1, 0);
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) {
            return;
        }
        
        if (damageImmune.contains(p)) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Enemy || e.getEntity() instanceof Player)) {
            return;
        }
        
        if (!(e.getDamager() instanceof Player p)) {
            return;
        }
        
        if (damageImmune.remove(p)) {
            msg(p, "&cDamage immunity was removed because you attacked");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        // Check pending tp set first for better performance
        Player p = e.getPlayer();
        if (!pendingTP.containsKey(p)) {
            return;
        }

        if (e.getFrom().distance(e.getTo()) > 0.1) {
            removePlayer(e.getPlayer(), "you moved");
        }
    }

    // Keep death/RTP totems on death
    // HIGH priority to run after Abiding skill check
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        removePlayer(e.getPlayer(), "you died");
        List<ItemStack> drops = e.getDrops();
        for (ItemStack i : new ArrayList<>(drops)) {
            String id = Utils.getItemID(i, totemKey);
            if (id == null) {
                continue;
            }

            if (isType(id, TotemType.DEATH) || isType(id, TotemType.RANDOM)) {
                drops.remove(i);
                e.getItemsToKeep().add(i);
            }
        }
    }

    // Do not allow totems to resurrect players
    @EventHandler(ignoreCancelled = true)
    public void onTotem(EntityResurrectEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) {
            return;
        }

        EquipmentSlot hand = e.getHand();
        if (hand == null) {
            return;
        }

        Player p = (Player) e.getEntity();
        ItemStack tot = p.getInventory().getItem(hand);
        if (Utils.getItemID(tot, totemKey) != null) {
            e.setCancelled(true);
        }
    }

    // Handle totem right-clicks and teleportations
    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!e.getAction().isRightClick()) {
            return;
        }

        ItemStack totem = e.getItem();
        String totid = Utils.getItemID(totem, totemKey);
        if (totid == null || isType(totid, TotemType.UNSET)) {
            return;
        }

        e.setCancelled(true);
        
        // KINGDOM HOME TELEPORT
        Player player = e.getPlayer();
        EquipmentSlot hand = e.getHand();
        if (isType(totid, TotemType.KHOME)) {
            KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(player);
            if (!kp.hasKingdom()) {
                msg(player, "&cYou do not have a Kingdom!");
                return;
            }

            ImmutableLocation loc = kp.getKingdom().getHome();
            if (loc == null) {
                msg(player, "&cYour kingdom does not have a valid home!");
                return;
            }

            initiateTeleportation(player, hand, totem, BukkitImmutableLocation.from(loc));
            return;
        }

        // REGULAR HOME TELEPORT
        if (isType(totid, TotemType.HOME)) {
            Location loc = player.getRespawnLocation();
            if (loc == null) {
                msg(player, "&cYou do not have a valid respawn location!");
                return;
            }

            initiateTeleportation(player, hand, totem, loc);
            return;
        }
        
        // RANDOM TP
        if (isType(totid, TotemType.RANDOM)) {
            initiateTeleportation(player, hand, totem, null);
            return;
        }

        // LAST DEATH TELEPORT
        if (isType(totid, TotemType.DEATH)) {
            int deathTimeout = 20 * 300; // 5 minutes in ticks
            int lastDeath = player.getStatistic(Statistic.TIME_SINCE_DEATH);
            Location loc = player.getLastDeathLocation();
            if (loc == null || lastDeath > deathTimeout) {
                msg(player, "&cYou have not died within the last 5 minutes!");
                return;
            }

            initiateTeleportation(player, hand, totem, loc.toCenterLocation().add(0, -0.5, 0));
            return;
        }

        String[] args = totid.split(":");
        if (args.length != 2) {
            msg(player, "&cYour totem is invalid! Please contact an administrator.");
            Bukkit.getLogger().warning("Invalid totem detected. Item: " + totem);
            return;
        }

        // LODESTONE TELEPORT
        if (isType(args[0], TotemType.LODESTONE)) {
            // Reset totem to unset if lodestone no longer exists.
            Location loc = stringToLoc(args[1]);
            if (loc.getBlock().getType() != Material.LODESTONE) {
                msg(player, "&cThat lodestone no longer exists :(");
                return;
            }

            initiateTeleportation(player, hand, totem, loc.toCenterLocation().add(0, 0.5, 0));
            return;
        }

        // PLAYER TELEPORT
        if (isType(args[0], TotemType.PLAYER)) {
            Player other = Bukkit.getPlayer(args[1]);
            if (other == null) {
                msg(player, "&cThat player is not online!");
                return;
            }

            if (other.equals(player)) {
                return;
            }

            if (pendingTP.containsKey(player)) {
                return;
            }

            String otherName = Utils.toPlain(other.displayName());
            if (pendingAccept.contains(player)) {
                msg(player, "&bRequest to &f" + otherName + " &bstill pending...");
                return;
            }

            pendingAccept.add(player);
            msg(player, "&bSent a teleportation request to &f" + otherName + "&b...");
            String requesterName = Utils.toPlain(player.displayName());
            other.sendMessage(Utils.toComponent("&c&l[!] &f" + requesterName + " &7 has requested to teleport to you."));
            other.sendMessage(Utils.toComponent("&c&l[!] &7To accept, type \"&aaccept&7\" in the chat within &f30 seconds&7."));
            other.sendMessage(Utils.toComponent("&c&l[!] &7To deny, type \"&cdeny&7\"."));
            new ChatConfirm(other, "accept", "deny", 30, "Teleportation request denied.", result -> {
                pendingAccept.remove(player);
                if (!player.isOnline()) {
                    return;
                }

                if (result == null || !result) {
                    msg(player, "&cYour request was not accepted.");
                    return;
                }

                ItemStack curItem = player.getInventory().getItem(hand);
                if (!curItem.isSimilar(totem)) {
                    msg(player, "&cTeleportation cancelled because you changed items.");
                    other.sendMessage(Utils.toComponent("&cTeleportation request accepted, but " + requesterName + " &cis no longer holding a totem."));
                    return;
                }

                other.sendMessage(Utils.toComponent("&aTeleportation request from " + requesterName + " &aaccepted."));
                initiateTeleportation(player, hand, curItem, null, other);
                return;
            });
            return;
        }

        msg(player, "&cYour totem is invalid! Please contact an administrator.");
        Bukkit.getLogger().warning("Invalid totem detected. Item: " + totem);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removePlayer(e.getPlayer(), null);
    }

    // Handle shapeless totem recipes. The recipes added to RecipeManager are only for display in /recipes,
    // the actual recipes are handled here for different bed types, compass/book meta, and creating totems
    // from any other totem.
    @EventHandler(priority = EventPriority.HIGH)
    public void onCraft(PrepareItemCraftEvent e) {
        CraftingInventory ci = e.getInventory();
        List<ItemStack> curItems = new ArrayList<>();

        // Add all non-null items in grid to a list to use for later.
        for (ItemStack item : ci.getMatrix()) {
            if (item != null) {
                curItems.add(item);
            }
        }

        // Must only have 2 ingredients
        if (curItems.size() != 2) {
            return;
        }

        // Must have a totem and another item
        ItemStack other = null;
        ItemStack curTotem = null;
        for (ItemStack item : curItems) {
            if (Utils.getItemID(item, totemKey) == null) {
                other = item;
            } else {
                curTotem = item;
            }
        }

        // curTotem being null means no totem was found
        if (curTotem == null) {
            return;
        }

        // If other is still null, we have combined two valid totems.
        // Check for totem duplication recipe
        if (other == null) {
            // Check if 1 totem is unset and another is something else
            // Attempt to assign "other" to the UNSET totem
            // and toDupe to the SET totem
            ItemStack toDupe = null;
            for (ItemStack tot : curItems) {
                String id = Utils.getItemID(tot, totemKey);
                if (isType(id, TotemType.UNSET)) {
                    other = tot;
                } else {
                    toDupe = tot;
                }
            }

            if (toDupe == null || other == null) {
                return;
            }

            ItemStack result = toDupe.clone();
            result.setAmount(2);
            ci.setResult(result);
            return;
        }

        String mat = other.getType().toString();
        if (mat.contains("_BED")) {
            setResult(ci, curTotem, getTotem(TotemType.KHOME));
            return;
        }

        if (mat.equals("COMPASS")) {
            CompassMeta cmeta = (CompassMeta) other.getItemMeta();
            if (!cmeta.hasLodestone()) {
                setResult(ci, curTotem, getTotem(TotemType.HOME));
                return;
            }

            Location lodestone = cmeta.getLodestone();
            if (lodestone == null) {
                ci.setResult(null);
                return;
            }

            ItemStack ltot = getTotem(TotemType.LODESTONE);
            String lodeloc = locToString(lodestone);
            String totname = totemSec.getString("lodestone.set-name").replace("%location%", lodeloc);
            ItemMeta tmeta = ltot.getItemMeta();
            tmeta.displayName(Utils.toComponent(totname));
            tmeta.getPersistentDataContainer().set(totemKey, PersistentDataType.STRING, "lodestone:" + lodeloc);
            ltot.setItemMeta(tmeta);
            setResult(ci, curTotem, ltot);
            return;
        }

        if (mat.equals("CALIBRATED_SCULK_SENSOR")) {
            setResult(ci, curTotem, getTotem(TotemType.DEATH));
            return;
        }

        if (mat.equals("WRITTEN_BOOK")) {
            // Apparently if a book meta does NOT have generation, then it must be original
            BookMeta bmeta = (BookMeta) other.getItemMeta();
            if (!bmeta.hasAuthor() || bmeta.hasGeneration()) {
                ci.setResult(null);
                return;
            }

            String player = bmeta.getAuthor();
            ItemStack btot = getTotem(TotemType.PLAYER);
            ItemMeta tmeta = btot.getItemMeta();
            String totname = totemSec.getString("player.set-name").replace("%player%", player);
            tmeta.displayName(Utils.toComponent(totname));
            tmeta.getPersistentDataContainer().set(totemKey, PersistentDataType.STRING, "player:" + player);
            btot.setItemMeta(tmeta);
            setResult(ci, curTotem, btot);
            return;
        }
    }

    // Unstack stacked totems from duplication
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        // Check if clicked slot has item and is a RESULT slot type
        Inventory inv = e.getClickedInventory();
        if (inv == null) {
            return;
        }

        if (!(inv.getType() == InventoryType.WORKBENCH || inv.getType() == InventoryType.CRAFTING)) {
            return;
        }

        if (e.getSlotType() != SlotType.RESULT) {
            return;
        }

        if (e.getCurrentItem() == null) {
            return;
        }

        ItemStack i = e.getCurrentItem().clone();
        if (i.getType() != Material.TOTEM_OF_UNDYING || i.getAmount() <= 1) {
            return;
        }

        InventoryAction action = e.getAction();
        if (!(action == InventoryAction.HOTBAR_MOVE_AND_READD || action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                action == InventoryAction.HOTBAR_SWAP)) {
            return;
        }

        Player p = (Player) e.getWhoClicked();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (ItemStack item : p.getInventory().getContents()) {
                if (item == null || !item.equals(i)) {
                    continue;
                }

                addOrDropItem(item, p);
                return;
            }
        });
    }

    // Unstack stacked totems on pickup
    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        ItemStack i = e.getItem().getItemStack();
        if (i.getType() != Material.TOTEM_OF_UNDYING || i.getAmount() <= 1) {
            return;
        }

        if (e.getEntityType() != EntityType.PLAYER) {
            return;
        }

        addOrDropItem(i, (Player) e.getEntity());
        e.getItem().setItemStack(i);
    }
    
    // Do not allow dropping of random totem
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack item = e.getItemDrop().getItemStack();
        String totid = Utils.getItemID(item, totemKey);
        if (totid == null) {
            return;
        }
        
        if (isType(totid, TotemType.RANDOM)) {
            e.setCancelled(true);
        }
    }

    // Sets the crafting inventory result to the specified item, unless the given
    // curTotem is already the same as the result. Requires that curTotem and nTotem
    // both have a totem tag
    private void setResult(CraftingInventory ci, ItemStack curTotem, ItemStack nTotem) {
        if (Utils.getItemID(curTotem, totemKey).equals(Utils.getItemID(nTotem, totemKey))) {
            ci.setResult(null);
        } else {
            ci.setResult(nTotem);
        }
    }

    // Add a totem to inventory, dropping if full
    private void addOrDropItem(ItemStack totem, Player p) {
        // Set initial stack size to 1
        ItemStack extra = totem.clone();
        extra.setAmount(extra.getAmount() - 1);
        totem.setAmount(1);

        // Add extras as singular stacks back to inv
        List<ItemStack> extras = new ArrayList<>();
        for (int i = 0; i < extra.getAmount(); i++) {
            ItemStack cur = extra.clone();
            cur.setAmount(1);
            Map<Integer, ItemStack> notFit = p.getInventory().addItem(cur);
            extras.addAll(notFit.values());
        }

        if (extras.isEmpty()) {
            return;
        }

        // Leftovers are dropped
        extras.forEach(i -> p.getWorld().dropItem(p.getLocation(), i));
    }

    private boolean isType(String s, TotemType type) {
        return s.equals(type.toString());
    }

    private ItemStack getTotem(TotemType type) {
        return im.getItem(TOTEM_INDICATOR + "." + type);
    }

    // Requires a non-null location
    private String locToString(Location loc) {
        return loc.getWorld().getName() + ", " + loc.getBlockX() + ", " +
                loc.getBlockY() + ", " + loc.getBlockZ();
    }

    // Requires that s be a valid string produced from locToString
    private Location stringToLoc(String s) {
        String[] args = s.split(", ");
        return new Location(Bukkit.getWorld(args[0]), NumberUtils.toInt(args[1]),
                NumberUtils.toInt(args[2]), NumberUtils.toInt(args[3]));
    }

    private void msg(Player p, String s) {
        if (s.indexOf("&c") == 0) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
        }
        p.sendActionBar(Utils.toComponent(s));
    }

}
