package com.leomelonseeds.ultimaaddons;

import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class UAPlaceholders extends PlaceholderExpansion {
    
    @Override
    public String getIdentifier() {
        return "ua";
    }

    @Override
    public String getAuthor() {
        return "M310N";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        Kingdom k = KingdomPlayer.getKingdomPlayer(player).getKingdom();
        if (k == null) {
            return null;
        }

        long ctime = System.currentTimeMillis();
        
        // Is the kingdom challenging or at war with another
        if (params.contains("haschallenged")) {
            long wartime = Utils.getWarTime();
            String lastChallenge = Utils.getLastChallenge(k);
            if (lastChallenge != null) {
                String[] lcs = lastChallenge.split("@");
                if (Kingdom.getKingdom(UUID.fromString(lcs[0])) != null 
                        && ctime < Long.valueOf(lcs[1]) + wartime) {
                    return "true";
                }
            }
            
            for (Entry<UUID, Long> e : k.getChallenges().entrySet()) {
                if (Kingdom.getKingdom(e.getKey()) != null && 
                        ctime < e.getValue() + wartime) {
                    return "true";
                }
            }
            
            return "false";
        }
        
        // Has the cooldown from the last purchased shield expired?
        boolean lastShieldExpired = false;
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
