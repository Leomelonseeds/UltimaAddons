package com.leomelonseeds.ultimaaddons.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.handlers.item.TotemManager;
import com.leomelonseeds.ultimaaddons.handlers.item.TotemType;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.Utils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@CommandAlias("ugive")
public class UAGive extends BaseCommand {
    private final UltimaAddons plugin;

    public UAGive(UltimaAddons plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandPermission("ua.give")
    @CommandCompletion("@players @ua_item @nothing @players|@ua_coordinates")
    @Description("Give an UltimaAddons item to a player")
    @Syntax("Usage: /ugive <player> <item> [<amount>] [<player>|[<world>],<x>,<y>,<z>]")
    public void onGive(CommandSender sender, @Flags("other") Player target, String uaItem, @Default("1") int amount, @Optional String context) {
        ItemStack i = UltimaAddons.getPlugin().getItems().getItem(uaItem);
        if (i == null) {
            CommandUtils.sendErrorMsg(sender, "That item does not exist!");
            return;
        }
        i.setAmount(amount);
        ItemMeta meta = i.getItemMeta();
        // Check additional args for player & lodestone totems
        String id = Utils.getItemID(i, TotemManager.totemKey);
        if (context != null) sender.sendMessage(id);
        if (context != null) sender.sendMessage(context);
        if (id.equals(TotemType.PLAYER.toString()) && context != null) {
                ConfigurationSection totemSec = UltimaAddons.getPlugin().getItems().getTotems().getTotemSec();
                String totemName = Objects.requireNonNull(totemSec.getString("player.set-name")).replace("%player%", context);
                meta.displayName(Utils.toComponent(totemName));
                meta.getPersistentDataContainer().set(TotemManager.totemKey, PersistentDataType.STRING, "player:" + context);
                i.setItemMeta(meta);
        } else if (id.equals(TotemType.LODESTONE.toString()) && context != null) {
            String[] coordinates = checkCoordinates(sender, context);
            if (coordinates != null) {
                World world = plugin.getServer().getWorld(coordinates[0]);
                int x = Integer.parseInt(coordinates[1]);
                int y = Integer.parseInt(coordinates[2]);
                int z = Integer.parseInt(coordinates[3]);
                Block targetBlock = new Location(world, x, y, z).getBlock();
                if (targetBlock.getType().equals(Material.LODESTONE)) {
                    assert world != null;
                    String lodeLoc = world.getName() + ", " + x + ", " + y + ", " + z;
                    ConfigurationSection totemSec = UltimaAddons.getPlugin().getItems().getTotems().getTotemSec();
                    String totName = Objects.requireNonNull(totemSec.getString("lodestone.set-name")).replace("%location%", lodeLoc);
                    meta.displayName(Utils.toComponent(totName));
                    meta.getPersistentDataContainer().set(TotemManager.totemKey, PersistentDataType.STRING, "lodestone:" + lodeLoc);
                    i.setItemMeta(meta);
                } else {
                    CommandUtils.sendErrorMsg(sender, "Target location block is not a lodestone!");
                    return;
                }
            } else {
                CommandUtils.sendErrorMsg(sender, "Unable to properly parse coordinates!");
                return;
            }
        }
        // Add item to inventory, drop if anything is returned
        target.getInventory().addItem(i).forEach((index, item) -> {
            Item ownedItem = target.getWorld().dropItem(target.getLocation(), item);
            ownedItem.setOwner(target.getUniqueId());
        });

        String itemName = meta != null && meta.hasDisplayName() ? Utils.toPlain(meta.displayName()) : i.getType().toString();
        CommandUtils.sendSuccessMsg(sender, "Gave &c" + amount + "x " + itemName + "&7 to &c" + target.getName());
    }

    private String[] checkCoordinates(CommandSender sender, String coordinates) {
        List<String> split = Arrays.asList(coordinates.split(",", 4));
        World world;
        if (sender instanceof Player && split.size() == 3) {
            world = ((Player) sender).getWorld();
            split.add(0, world.getName());
        } else {
            world = plugin.getServer().getWorld(split.get(0));
        }
        if (split.size() != 4) return null;
        if (world == null) return null;
        for (int i = 1; i < split.size(); i++) {
            String coordinate = split.get(i);
            if (!NumberUtils.isCreatable(coordinate))
                return null;
        }
        return split.toArray(new String[4]);
    }
}
