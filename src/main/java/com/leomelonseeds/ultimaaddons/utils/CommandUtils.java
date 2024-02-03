package com.leomelonseeds.ultimaaddons.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtils {

    public static void sendErrorMsg(CommandSender target, String msg) {
        target.sendMessage(Utils.toComponent("&cError: &7" + msg));
    }

    public static void sendMsg(CommandSender target, String msg) {
        target.sendMessage(Utils.toComponent("&7" + msg));
    }

    public static void sendActionbarMsg(CommandSender target, String msg) {
        target.sendActionBar(Utils.toComponent("&7" + msg));
    }

    public static void sendConsoleMsg(String msg) {
        Bukkit.getConsoleSender().sendMessage(Utils.toComponent("&7" + msg));
    }

    public static boolean isAdmin(CommandSender target) {
        if (target.hasPermission("ua.admin")) {
            return true;
        }

        sendErrorMsg(target, "You do not have permission for this!");
        return false;
    }

    public static void sendSuccessMsg(CommandSender target, String msg) {
        target.sendMessage(Utils.toComponent("&aSuccess: &7" + msg));
    }

    public static Player getPlayer(CommandSender target) {
        if (!(target instanceof Player)) {
            return null;
        }

        return (Player) target;
    }
}
