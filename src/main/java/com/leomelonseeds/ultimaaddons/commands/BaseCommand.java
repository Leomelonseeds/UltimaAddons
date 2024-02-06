package com.leomelonseeds.ultimaaddons.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.commands.arguments.IntArgument;
import com.leomelonseeds.ultimaaddons.commands.arguments.ItemArgument;
import com.leomelonseeds.ultimaaddons.commands.arguments.KingdomArgument;
import com.leomelonseeds.ultimaaddons.commands.arguments.PlayerArgument;
import com.leomelonseeds.ultimaaddons.commands.arguments.ShopkeeperArgument;
import com.leomelonseeds.ultimaaddons.commands.ua.UAChallenge;
import com.leomelonseeds.ultimaaddons.commands.ua.UAGive;
import com.leomelonseeds.ultimaaddons.commands.ua.UARecipes;
import com.leomelonseeds.ultimaaddons.commands.ua.UAReload;
import com.leomelonseeds.ultimaaddons.commands.ua.uask.UASk;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class BaseCommand implements CommandExecutor, TabCompleter {
    public static Map<String, Argument> argumentTypes;
    private final UltimaAddons plugin = UltimaAddons.getPlugin();
    private final Map<String, Command> commands;

    public BaseCommand() {
        argumentTypes = Map.of(
                "int", new IntArgument("int", ""),
                "item", new ItemArgument("int", ""),
                "kingdom", new KingdomArgument("int", ""),
                "player", new PlayerArgument("int", ""),
                "shopkeeper", new ShopkeeperArgument("int", "")
        );
        commands = Map.of(
                "uchallenge", new UAChallenge("uchallenge", Collections.emptyList(), "", "",
                        List.of(
                                argumentTypes.get("kingdom")
                        )),
                "ugive", new UAGive("ugive", Collections.emptyList(), "ua.give", "",
                        List.of(
                                argumentTypes.get("player"),
                                argumentTypes.get("item"),
                                argumentTypes.get("int")
                        )),
                "ureload", new UAReload("ureload", Collections.emptyList(), "ua.reload", ""),
                "usk", new UASk("uask", Collections.emptyList(), "ua.sk", ""),
                "recipes", new UARecipes("recipes", Collections.emptyList(), "", "")
        );
        commands.forEach((key, value) ->
                {
                    Objects.requireNonNull(this.plugin.getCommand(key)).setExecutor(this);
                    if (!key.equals("uachallenge")) // ignore tab handle on uachallenge
                        Objects.requireNonNull(this.plugin.getCommand(key)).setTabCompleter(this);
                }
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(Utils.convertAmps("&cNo support for this instanceof sender."));
            return true;
        }

        Command runCommand = commands.get(name);
        if (!runCommand.hasPermission(sender))
            CommandUtils.sendErrorMsg(sender, "No permission");
        else
            commands.get(name).execute(sender, cmd, name, args);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender))
            return Collections.emptyList();

        Command runCommand = commands.get(name);
        if (runCommand.hasPermission(sender)) {
            int index = args.length - 1;
            String lastArg = args[index];
            return StringUtil.copyPartialMatches(lastArg, commands.get(name).handleTab(args), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
