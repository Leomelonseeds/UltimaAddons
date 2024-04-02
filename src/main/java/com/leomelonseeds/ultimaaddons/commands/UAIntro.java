package com.leomelonseeds.ultimaaddons.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.invs.IntroInv;
import org.bukkit.entity.Player;

@CommandAlias("uintro")
public class UAIntro extends BaseCommand {
    private final UltimaAddons plugin;

    public UAIntro(UltimaAddons plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandPermission("ua.intro")
    @Description("Show the intro")
    public void onIntro(Player p) {
        new IntroInv(p);
    }
}
