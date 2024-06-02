package com.leomelonseeds.ultimaaddons.utils;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;

import com.leomelonseeds.ultimaaddons.UltimaAddons;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class UAPlaceholders extends PlaceholderExpansion {

    @NotNull
    @Override
    public String getIdentifier() {
        return "ua";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "M310N";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // Extra online player placeholders
        do {
            if (!player.isOnline()) {
                break;
            }
            
            if (params.equals("canparry")) {
                return UltimaAddons.getPlugin().getParry().canParry(player.getUniqueId()) + "";
            }
        } while (false);
        
        Kingdom k = KingdomPlayer.getKingdomPlayer(player).getKingdom();
        if (k == null) {
            return null;
        }

        // Is the kingdom challenging or at war with another
        if (params.contains("haschallenged")) {
            return Utils.hasChallenged(k) + "";
        }

        // Has the cooldown from the last purchased shield expired?
        boolean lastShieldExpired = false;
        long ctime = System.currentTimeMillis();
        long nextbuytime = Utils.getNextShield(k);
        if (ctime > nextbuytime) {
            lastShieldExpired = true;
        }

        if (params.contains("lastshieldexpired")) {
            return lastShieldExpired + "";
        }

        if (params.contains("remainingtime")) {
            return Utils.formatDate(nextbuytime - ctime);
        }

        return null;
    }
}
