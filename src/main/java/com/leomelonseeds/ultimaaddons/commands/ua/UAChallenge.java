package com.leomelonseeds.ultimaaddons.commands.ua;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.StandardRelationAttribute;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.constants.player.StandardKingdomPermission;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.invs.ChallengeInv;
import com.leomelonseeds.ultimaaddons.utils.ChatConfirm;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class UAChallenge extends Command {
    public UAChallenge(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        super(name, aliases, permission, description, arguments);
    }

    @Override
    public boolean hasInvalidArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 1) {
            CommandUtils.sendErrorMsg(sender, "");
            return true;
        }

        if (!(sender instanceof Player player)) {
            CommandUtils.sendErrorMsg(sender, "Bruh stop tryna use player commands in the console smhsmh");
            return true;
        }

        // Challenger must have a kingdom
        KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(player);
        Kingdom attacker = kp.getKingdom();
        if (attacker == null) {
            CommandUtils.sendErrorMsg(sender, "You must be in a kingdom to use this!");
            return true;
        }

        // Cannot be pacifist
        if (attacker.isPacifist()) {
            CommandUtils.sendErrorMsg(sender, "Your kingdom is pacifist, and cannot attack other kingdoms!");
            return true;
        }

        if (!kp.hasPermission(StandardKingdomPermission.INVADE)) {
            CommandUtils.sendErrorMsg(sender, "Your kingdom rank does not allow you to invade other kingdoms!");
            return true;
        }

        // Attacker cannot be new
        long date = System.currentTimeMillis();
        if (Utils.isNew(attacker)) {
            CommandUtils.sendErrorMsg(sender, "Your kingdom is new! You will have to wait &e" + Utils.timeUntilNotNew(attacker) +
                    " &7before you can challenge another kingdom.");
            return true;
        }

        // Attacker must have a nexus
        if (attacker.getNexus() == null) {
            CommandUtils.sendErrorMsg(sender, "You must place your nexus using &a/k nexus &cbefore you can declare war!");
            return true;
        }

        // Challenged kingdom must exist (handled by KingdomArgument)
        if (super.hasInvalidArgs(sender, args))
            return true;
        Kingdom target = Kingdom.getKingdom(args[0]);

        // Must be a different kingdom
        if (target.getId().equals(attacker.getId())) {
            CommandUtils.sendErrorMsg(sender, "Bruh you can't start a civil war here...");
            return true;
        }

        // Cannot be pacifist
        if (target.isPacifist()) {
            CommandUtils.sendErrorMsg(sender, "You cannot attack pacifist kingdoms!");
            return true;
        }

        // Attacker cannot be new
        if (Utils.isNew(target)) {
            CommandUtils.sendErrorMsg(sender, "That kingdom is new! You will have to wait &e" + Utils.timeUntilNotNew(target) +
                    " &7before you can challenge them.");
            return true;
        }

        // Cannot have a shield
        if (target.hasShield()) {
            CommandUtils.sendErrorMsg(sender, "The kingdom you are trying to attack is shielded for &e" +
                    Utils.formatDate(target.getShieldTimeLeft()));
            return true;
        }

        // Must not be allies or truced
        StandardRelationAttribute ceasefire = StandardRelationAttribute.CEASEFIRE;
        if (ceasefire.hasAttribute(attacker, target)) {
            CommandUtils.sendErrorMsg(sender, "You cannot attack allied or truced kingdoms!");
            return true;
        }

        // Challenger kingdom must not have been challenged by the target kingdom already
        Map<UUID, Long> challenges = attacker.getChallenges();
        if (challenges.containsKey(target.getId())) {
            long time = challenges.get(target.getId());
            if (time > date) {
                CommandUtils.sendErrorMsg(sender, "&e" + target.getName() + " &7has already challenged your kingdom! " +
                        "War starts in &e" + Utils.formatDate(time - date));
                return true;
            }

            if (time + Utils.getWarTime() > date) {
                CommandUtils.sendErrorMsg(sender, "Your war with &e" + target.getName() + " &7is still ongoing!");
                return true;
            }
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
                CommandUtils.sendErrorMsg(sender, "Your kingdom has already challenged &e" + cur.getName() +
                        "&7! War starts in &e" + Utils.formatDate(lcd - date));
                return true;
            }

            // After invasion cooldown
            if (cooldown > date) {
                CommandUtils.sendErrorMsg(sender, "You must wait &e" + Utils.formatDate(cooldown - date) +
                        " &7before you can challenge again.");
                return true;
            }
        }

        // Remove kingdom shield if they have one
        if (attacker.hasShield()) {
            CommandUtils.sendErrorMsg(sender, "Your kingdom is shielded for &e" + Utils.formatDate(attacker.getShieldTimeLeft()) +
                    "&7. Challenging another kingdom will remove this shield, and you will have to wait &e" +
                    Utils.formatDate(Utils.getNextShield(attacker) - date) + " &7before you can buy another one. " +
                    "Please type 'confirm' in the chat within 30 seconds to continue.");
            new ChatConfirm(player, "confirm", 30, "Declaration cancelled.", result ->
            {
                if (result == null || !result)
                    return;
                attacker.deactivateShield();
            });
        }
        return attacker.hasShield();
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        Player player = (Player) sender;
        KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(player);
        Kingdom attacker = kp.getKingdom();
        Kingdom target = Kingdom.getKingdom(args[0]);

        CommandUtils.sendMsg(player, "");
        CommandUtils.sendMsg(player, "&cYou are sending a declaration of war to &e" + target.getName() + "&c. After a chosen amount of time, "
                + "both you and the enemy will have &62 hours &cto invade each other's lands. You can only challenge 1 kingdom at a time, "
                + "and after the war you will need to wait &61 day &cbefore challenging another kingdom.");
        CommandUtils.sendMsg(player, "");
        CommandUtils.sendMsg(player, "&cAdditionally, you will need to pay resource points to begin invasion when war starts, depending on how "
                + "many lands you have. Currently, it is estimated that each invasion will cost &e" + attacker.getLands().size()
                + " &cresource points.");
        CommandUtils.sendMsg(player, "");
        CommandUtils.sendMsg(player, "&cFinally, neither kingdom will be able to claim/unclaim lands or move their nexus during the preparation period.");
        CommandUtils.sendMsg(player, "");
        CommandUtils.sendMsg(player, "&cPlease type 'confirm' in the chat within 1 minute to continue.");
        new ChatConfirm(player, "confirm", 60, "Declaration cancelled.", result ->
        {
            if (result == null || !result)
                return;
            new ChallengeInv(target, attacker, player);
        });
    }
}
