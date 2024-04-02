package com.leomelonseeds.ultimaaddons.commands.ua;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.Utils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UAGive extends Command {

    public UAGive(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        super(name, aliases, permission, description, arguments);
    }

    @Override
    public boolean hasInvalidArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            CommandUtils.sendErrorMsg(sender, "Usage: /ugive [player] [item] <amount>");
            return true;
        }
        return super.hasInvalidArgs(sender, args);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name,
                        @NotNull String[] args) {
        Player p = Bukkit.getPlayerExact(args[0]);
        assert p != null;
        ItemStack i = UltimaAddons.getPlugin().getItems().getItem(args[1]);
        assert i != null;
        if (args.length == 3)
            i.setAmount(NumberUtils.toInt(args[2], 1));
        // Add item to inventory, drop if anything is returned
        p.getInventory().addItem(i).forEach((index, item) -> p.getWorld().dropItem(p.getLocation(), item));

        String iname = i.getType().toString();
        ItemMeta meta = i.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            iname = Utils.toPlain(meta.displayName());
        }
        CommandUtils.sendSuccessMsg(sender, "Gave &c" + i.getAmount() + "x " + iname + "&7 to &c" + p.getName());
    }
}
