package com.leomelonseeds.ultimaaddons;

import java.util.Map;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.StandardRelationAttribute;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.constants.player.StandardKingdomPermission;

import com.leomelonseeds.ultimaaddons.invs.ChallengeInv;
import com.leomelonseeds.ultimaaddons.utils.ChatConfirm;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class UAChallenge implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (args.length != 1) {
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sendMsg(sender, "&cBruh stop tryna use player command in the console smhsmh");
            return true;
        }
        
        // Challenger must have a kingdom
        Player player = (Player) sender;
        KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(player);
        Kingdom attacker = kp.getKingdom();
        if (attacker == null) {
            sendMsg(sender, "&cYou must be in a kingdom to use this!");
            return true;
        }
        
        // Cannot be pacifist
        if (attacker.isPacifist()) {
            sendMsg(sender, "&cYour kingdom is pacifist, and cannot attack other kingdoms!");
            return true;
        }
        
        if (!kp.hasPermission(StandardKingdomPermission.INVADE)) {
            sendMsg(sender, "&cYour kingdom rank does not allow you to invade other kingdoms!");
            return true;
        }
        
        // Attacker cannot be new
        long date = System.currentTimeMillis();
        if (Utils.isNew(attacker)) {
            sendMsg(sender, "&cYour kingdom is new! You will have to wait &e" + Utils.timeUntilNotNew(attacker) + 
                    " &cbefore you can challenge another kingdom.");
            return true;
        }
        
        // Attacker must have a nexus
        if (attacker.getNexus() == null) {
            sendMsg(sender, "&cYou must place your nexus using &a/k nexus &cbefore you can declare war!");
            return true;
        }
        
        // Challenged kingdom must exist
        Kingdom target = Kingdom.getKingdom(args[0]);
        if (target == null) {
            sendMsg(sender, "&cThat kingdom doesn't exist!");
            return true;
        }
        
        // Must be a different kingdom
        if (target.getId().equals(attacker.getId())) {
            sendMsg(sender, "&cBruh you can't start a civil war here...");
            return true;
        }
        
        // Cannot be pacifist
        if (target.isPacifist()) {
            sendMsg(sender, "&cYou cannot attack pacifist kingdoms!");
            return true;
        }
        
        // Attacker cannot be new
        if (Utils.isNew(target)) {
            sendMsg(sender, "&cThat kingdom is new! You will have to wait &e" + Utils.timeUntilNotNew(target) + 
                    " &cbefore you can challenge them.");
            return true;
        }
        
        // Cannot have a shield
        if (target.hasShield()) {
            sendMsg(sender, "&cThe kingdom you are trying to attack is shielded for &e" + 
                    Utils.formatDate(target.getShieldTimeLeft()));
            return true;
        }
        
        // Must not be allies or truced
        StandardRelationAttribute ceasefire = StandardRelationAttribute.CEASEFIRE;
        if (ceasefire.hasAttribute(attacker, target)) {
            sendMsg(sender, "&cYou cannot attack allied or truced kingdoms!");
            return true;
        }
        
        // Challenger kingdom must not have been challenged by the target kingdom already
        Map<UUID, Long> challenges = attacker.getChallenges();
        if (challenges.containsKey(target.getId())) {
            long time = challenges.get(target.getId());
            if (time > date) {
                sendMsg(sender, "&e" + target.getName() + " &chas already challenged your kingdom! " + 
                        "War starts in &e" + Utils.formatDate(time - date));
                return true;
            }
            
            if (time + Utils.getWarTime() > date) {
                sendMsg(sender, "&cYour war with &e" + target.getName() + " &cis still ongoing!");
                return true;
            }
        }
        
        // Kingdom must not already have challenged a kingdom
        String lastChallenge = Utils.getLastChallenge(attacker);
        if (lastChallenge != null) {
            String[] slck = lastChallenge.split("@");
            long lcd = Long.valueOf(slck[1]);
            Kingdom cur = Kingdom.getKingdom(UUID.fromString(slck[0]));
            long cooldown = lcd + UltimaAddons.CHALLENGE_COOLDOWN_TIME;
            
            // A challenge is pending
            if (cur != null && lcd > date) {
                sendMsg(sender, "&cYour kingdom has already challenged &e" + cur.getName() + 
                        "&c! War starts in &e" + Utils.formatDate(lcd - date));
                return true;
            }
            
            // After invasion cooldown
            if (cooldown > date) {
                sendMsg(sender, "&cYou must wait &e" + Utils.formatDate(cooldown - date) + 
                        " &cbefore you can challenge again.");
                return true;
            }
        }
        
        // Remove kingdom shield if they have one
        if (attacker.hasShield()) {
            sendMsg(sender, "&cYour kingdom is shielded for &e" + Utils.formatDate(attacker.getShieldTimeLeft()) +
                    "&c. Challenging another kingdom will remove this shield, and you will have to wait &e" + 
                    Utils.formatDate(Utils.getNextShield(attacker) - date) + " &cbefore you can buy another one. " +
                    "Please type 'confirm' in the chat within 30 seconds to continue.");
            new ChatConfirm(player, "confirm", 30, result -> {
                if (result == null || !result) {
                    return;
                }
                attacker.deactivateShield();
                confirmChallenge(target, attacker, player);
             });
             return true;
        }

        confirmChallenge(target, attacker, player);
        return true;
    }
    
    private void confirmChallenge(Kingdom target, Kingdom attacker, Player player) {
        sendMsg(player, "");
        sendMsg(player, "&cYou are sending a declaration of war to &e" + target.getName() + "&c. After a chosen amount of time, "
                + "both you and the enemy will have &62 hours &cto invade each other's lands. You can only challenge 1 kingdom at a time, "
                + "and after the war you will need to wait &61 day &cbefore challenging another kingdom.");
        sendMsg(player, "");
        sendMsg(player, "&cAdditionally, you will need to pay resource points to begin invasion when war starts, depending on how "
                + "many lands you have. Currently, it is estimated that each invasion will cost &e" + attacker.getLands().size() 
                + " &cresource points.");
        sendMsg(player, "");
        sendMsg(player, "&cFinally, neither kingdom will be able to claim/unclaim lands or move their nexus during the preparation period.");
        sendMsg(player, "");
        sendMsg(player, "&cPlease type 'confirm' in the chat within 1 minute to continue.");
        new ChatConfirm(player, "confirm", 60, result -> {
           if (result == null || !result) {
               return;
           }
           new ChallengeInv(target, attacker, player);
        });
    }
    
    /**
     * Send the given user a message.
     *
     * @param target the user
     * @param msg the error message
     */
    private void sendMsg(CommandSender target, String msg) {
        target.sendMessage(Utils.toComponent(msg));
    }
}
