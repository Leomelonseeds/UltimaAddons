package com.leomelonseeds.ultimaaddons.objects;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.ChatConfirm;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class TutorialQuiz {

    private static Map<Integer, Pair<String, String>> questions;
    private Player player;
    private int rewardAmt;
    
    /**
     * Starts a tutorial quiz for a player
     * 
     * @param player
     */
    public TutorialQuiz(Player player) {
        this.player = player;
        this.rewardAmt = UltimaAddons.getPlugin().getConfig().getInt("tutorial-reward");
        loadQuestions();
        init();
    }
    
    private void init() {
        if (player.hasPermission("ua.quiz")) {
            Utils.msg(player, "&cYou have either already completed the quiz or failed within the last minute.");
            return;
        }
        
        Utils.msg(player, "");
        Utils.msg(player, "&bYou are taking the intro quiz, which consists of " + questions.size() + " easy questions on basic server features. "
                + "Your reward will be " + rewardAmt + " diamonds. Please read the short intro handbooks before taking this quiz. "
                + "Type 'confirm' in chat within 30 seconds to continue.");
        new ChatConfirm(player, "confirm", 30, "Quiz cancelled", result -> {
            if (result == null || !result) {
                return;
            }
            quiz(0);
        });
    }
    
    private void quiz(int q) {
        if (!player.isOnline()) {
            return;
        }
        
        if (!questions.containsKey(q)) {
            success();
            return;
        }

        Pair<String, String> question = questions.get(q);
        Utils.msg(player, "");
        Utils.msg(player, "&b" + question.getLeft());
        Utils.msg(player, "&7&oPlease enter a &f&o1 " + (q == 5 ? "digit" : "word") + " &7&oresponse in chat within 30 seconds.");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
        new ChatConfirm(player, question.getRight(), 30, null, result -> {
            if (result == null || !result) {
                fail();
                return;
            }
            quiz(q + 1);
        });
    }
    
    private void fail() {
        String cmd = "lp user " + player.getName() + " permission settemp ua.quiz true 1m";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        Utils.msg(player, "&cAnswer incorrect! Please retry the quiz in 1 minute.");
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_DEATH, 1F, 1F);
    }
    
    private void success() {
        String cmd = "lp user " + player.getName() + " permission set ua.quiz true";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        Utils.msg(player, "&aCongratulations on completing the quiz! You get " + rewardAmt + " free diamonds as a reward.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
        Utils.giveItems(player, new ItemStack(Material.DIAMOND, rewardAmt));
    }

    // Load hard-coded questions
    private void loadQuestions() {
        if (questions != null) {
            return;
        }
        
        questions = new HashMap<>();
        
        questions.put(0, ImmutablePair.of(
                "Which block is essential to a Kingdom’s existence?", 
                "nexus"));
        
        questions.put(1, ImmutablePair.of(
                "All Kingdom upgrades are purchased using ________ points.", 
                "resource"));
        
        questions.put(2, ImmutablePair.of(
                "What is the name of the mob that spawns to defend your Kingdom from invasions?", 
                "champion"));
        
        questions.put(3, ImmutablePair.of(
                "What is the name of the Kingdom at the center of the world, where players can sell and trade items?", 
                "market"));
        
        questions.put(4, ImmutablePair.of(
                "You can teleport to your home, other players, or last death location using a Totem of _______.", 
                "warping"));
        
        questions.put(5, ImmutablePair.of(
                "How many thousand blocks away is the Overworld border from the center?", 
                "8"));
    }
}
