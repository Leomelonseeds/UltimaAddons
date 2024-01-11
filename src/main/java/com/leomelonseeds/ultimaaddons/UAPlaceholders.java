package com.leomelonseeds.ultimaaddons;

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
        boolean hasChallenged = false;
        boolean lastShieldExpired = false;
        
        // Is the kingdom challenging or at war with another
        String lastChallenge = Utils.getLastChallenge(k);
        if (lastChallenge != null) {
            long lastChallengeTime = Long.valueOf(lastChallenge.split("@")[1]);
            if (ctime < lastChallengeTime + UltimaAddons.WAR_TIME) {
                hasChallenged = true;
            }
        }
        
        if (!hasChallenged) {
            for (Long challenge : k.getChallenges().values()) {
                if (ctime < challenge + UltimaAddons.WAR_TIME) {
                    hasChallenged = true;
                    break;
                }
            }
        }
        
        // Has the cooldown from the last purchased shield expired?
        long nextbuytime = Utils.getNextShield(k);
        if (ctime > nextbuytime) {
            lastShieldExpired = true;
        }
        
        if (params.contains("lastshieldexpired")) {
            return lastShieldExpired + "";
        }
        
        if (params.contains("haschallenged")) {
            return hasChallenged + "";
        }
        
        if (params.contains("remainingtime")) {
            return Utils.formatDate(nextbuytime - ctime);
        }
        
        return null;
    }
}
