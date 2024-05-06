package com.leomelonseeds.ultimaaddons.commands;

import org.bukkit.entity.Player;

import com.leomelonseeds.ultimaaddons.invs.IntroInv;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;

@CommandAlias("uintro")
public class UAIntro extends BaseCommand {
    
    @Default
    @CommandPermission("ua.intro")
    @Description("Show the intro")
    public void onIntro(Player p) {
        new IntroInv(p);
    }
}
