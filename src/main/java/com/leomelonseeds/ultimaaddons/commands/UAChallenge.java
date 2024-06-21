package com.leomelonseeds.ultimaaddons.commands;

import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.KingdomRelation;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.constants.player.StandardKingdomPermission;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.invs.ChallengeInv;
import com.leomelonseeds.ultimaaddons.utils.ChatConfirm;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;

@CommandAlias("uchallenge")
public class UAChallenge extends BaseCommand {

    @Default
    @CommandPermission("ua.challenge")
    @Description("Challenge another Kingdom to war")
    @Syntax("<kingdom>")
    public void onChallenge(Player player, String targetKingdom) {
        // Challenger must have a kingdom
        KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(player);
        Kingdom attacker = kp.getKingdom();
        if (attacker == null) {
            CommandUtils.sendErrorMsg(player, "You must be in a kingdom to use this!");
            return;
        }

        // Cannot be pacifist
        if (attacker.isPacifist()) {
            CommandUtils.sendErrorMsg(player, "Your kingdom is pacifist, and cannot attack other kingdoms!");
            return;
        }

        if (!kp.hasPermission(StandardKingdomPermission.INVADE)) {
            CommandUtils.sendErrorMsg(player, "Your kingdom rank does not allow you to invade other kingdoms!");
            return;
        }

        // Attacker cannot be new
        long date = System.currentTimeMillis();
        if (Utils.isNew(attacker)) {
            CommandUtils.sendErrorMsg(player, "Your kingdom is new! You will have to wait &e" + Utils.timeUntilNotNew(attacker) +
                    " &7before you can challenge another kingdom.");
            return;
        }

        // Attacker must have a nexus
        if (attacker.getNexus() == null) {
            CommandUtils.sendErrorMsg(player, "You must place your nexus using &a/k nexus &7before you can declare war!");
            return;
        }

        // Kingdom must not already have challenged a kingdom
        String lastChallenge = Utils.getLastChallenge(attacker);
        if (lastChallenge != null) {
            String[] slck = lastChallenge.split("@");
            long lcd = Long.parseLong(slck[1]);
            Kingdom cur = Kingdom.getKingdom(UUID.fromString(slck[0]));
            long cooldown = lcd + UltimaAddons.CHALLENGE_COOLDOWN_TIME;

            // A challenge is pending
            if (cur != null && lcd > date) {
                CommandUtils.sendErrorMsg(player, "Your kingdom has already challenged &e" + cur.getName() +
                        "&7! War starts in &e" + Utils.formatDate(lcd - date));
                return;
            }

            // After invasion cooldown
            if (cooldown > date) {
                CommandUtils.sendErrorMsg(player, "You must wait &e" + Utils.formatDate(cooldown - date) +
                        " &7before you can challenge again.");
                return;
            }
        }

        Kingdom target = Kingdom.getKingdom(targetKingdom);
        if (target == null) {
            CommandUtils.sendErrorMsg(player, "The kingdom you challenged does not exist!");
            return;
        }

        // Must be a different kingdom
        if (target.getId().equals(attacker.getId())) {
            CommandUtils.sendErrorMsg(player, "Bruh you can't start a civil war here...");
            return;
        }

        // Cannot be pacifist
        if (target.isPacifist()) {
            CommandUtils.sendErrorMsg(player, "You cannot attack pacifist kingdoms!");
            return;
        }

        // Attacker cannot be new
        if (Utils.isNew(target)) {
            CommandUtils.sendErrorMsg(player, "That kingdom is new! You will have to wait &e" + Utils.timeUntilNotNew(target) +
                    " &cbefore you can challenge them.");
            return;
        }

        // Must not be allies or truced
        KingdomRelation relation = attacker.getRelationWith(target);
        if (relation == KingdomRelation.ALLY || relation == KingdomRelation.TRUCE) {
            CommandUtils.sendErrorMsg(player, "You cannot attack allied or truced kingdoms!");
            return;
        }

        // Cannot have a shield
        if (target.hasShield()) {
            CommandUtils.sendErrorMsg(player, "The kingdom you are trying to attack is shielded for &e" +
                    Utils.formatDate(target.getShieldTimeLeft()));
            return;
        }

        // Challenger kingdom must not have been challenged by the target kingdom already
        Map<UUID, Long> challenges = attacker.getChallenges();
        if (challenges.containsKey(target.getId())) {
            long time = challenges.get(target.getId());
            if (time > date) {
                CommandUtils.sendErrorMsg(player, "&e" + target.getName() + " &chas already challenged your kingdom! " +
                        "War starts in &e" + Utils.formatDate(time - date));
                return;
            }

            if (time + Utils.getWarTime() > date) {
                CommandUtils.sendErrorMsg(player, "Your war with &e" + target.getName() + " &cis still ongoing!");
                return;
            }
        }

        // Remove kingdom shield if they have one
        if (attacker.hasShield()) {
            CommandUtils.sendMsg(player, "&cYour kingdom is shielded for &e" + Utils.formatDate(attacker.getShieldTimeLeft()) +
                    "&c. Challenging another kingdom will remove this shield, and you will have to wait &e" +
                    Utils.formatDate(Utils.getNextShield(attacker) - date) + " &cbefore you can buy another one. " +
                    "Please type 'confirm' in the chat within 30 seconds to continue.");
            new ChatConfirm(player, "confirm", 30, "Declaration cancelled.", result ->
            {
                if (result == null || !result)
                    return;
                attacker.deactivateShield();
            });
        }

        if (attacker.hasShield())
            return;

        CommandUtils.sendMsg(player, "");
        CommandUtils.sendMsg(player, "&cYou are sending a declaration of war to &e" + target.getName() + "&c. After a chosen amount of time, "
                + "both you and the enemy will have &62 hours &cto invade each other's lands. You can only challenge 1 Kingdom at a time, "
                + "and after the war you will need to wait &61 day &cbefore challenging another Kingdom.");

        Utils.schedule(40, () -> {
            CommandUtils.sendMsg(player, "");
            CommandUtils.sendMsg(player, "&cAdditionally, invading each enemy chunk will cost resource points depending on how many lands you have. "
                    + "Currently, it is estimated that each invasion will cost &e" + attacker.getLands().size() + " &cresource points.");
        });

        Utils.schedule(80, () -> {
            CommandUtils.sendMsg(player, "");
            CommandUtils.sendMsg(player, "&cFinally, both Kingdoms &4cannot &cclaim/unclaim lands or move their nexus during the preparation period.");
        });

        Utils.schedule(120, () -> {
            CommandUtils.sendMsg(player, "");
            CommandUtils.sendMsg(player, "&cPlease type 'confirm' in the chat within 1 minute to continue.");
            new ChatConfirm(player, "confirm", 60, "Declaration cancelled.", result ->
            {
                if (result == null || !result)
                    return;
                new ChallengeInv(target, attacker, player);
            });
        });
    }
}
