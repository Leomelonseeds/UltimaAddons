package com.leomelonseeds.ultimaaddons.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;

public abstract class Command {
    protected final UltimaAddons plugin = UltimaAddons.getPlugin();
    private final String name;
    private final List<String> aliases;
    private final String permission;
    private final String description;
    private final List<? extends Argument> arguments;

    public Command(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        this.name = name;
        this.aliases = aliases;
        this.permission = permission;
        this.description = description;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission(@NotNull CommandSender sender) {
        if (permission == null || permission.isBlank()) {
            return true;
        }
        
        return sender.hasPermission(permission);
    }

    public List<? extends Argument> getArguments() {
        return arguments;
    }

    public boolean hasInvalidArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        for (int i = 0; i < arguments.size() && i < args.length; i++) {
            Argument arg = arguments.get(i);
            String context = args[i];

            if (!arg.canParse(context)) {
                CommandUtils.sendErrorMsg(sender, arg.getError());
                return true;
            }
        }
        return false;
    }

    /**
     * @param sender sender
     * @param cmd    cmd
     * @param name   name of cmd
     * @param args   arguments
     */
    abstract public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args);

    public List<String> handleTab(@NotNull String[] args) {
        int index = args.length - 1;
        return arguments.size() > index
                ? arguments.get(index).tabComplete()
                : Collections.emptyList();
    }
}