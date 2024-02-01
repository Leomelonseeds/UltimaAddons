package com.leomelonseeds.ultimaaddons.commands.arguments;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerArgument extends Argument {
    public PlayerArgument(String name, String description) {
        super(name, description);
    }

    /**
     * @param context player name
     * @return if player exists
     */
    @Override
    protected boolean canParse(@NotNull String context) {
        return Bukkit.getPlayerExact(context) != null;
    }

    /**
     * @return list of players
     */
    @Override
    protected List<String> tabComplete() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    /**
     * @return error msg
     */
    @Override
    protected String getError() {
        return "Player does not exist";
    }
}
