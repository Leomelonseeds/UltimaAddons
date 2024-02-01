package com.leomelonseeds.ultimaaddons.commands.ua.uask.sub;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.leomelonseeds.ultimaaddons.utils.TimeParser;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DebugCommand extends Command {
    public DebugCommand(String name, List<String> aliases, String permission, String description, List<? extends Argument> arguments) {
        super(name, aliases, permission, description, arguments);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        if (hasInvalidArgs(sender, args))
            return;

        CommandUtils.sendMsg(sender, "&aTotal RSKs: &f" + this.plugin.getSKLinker().getSize());
        CommandUtils.sendMsg(sender, "&aConfig Values: &f");
        CommandUtils.sendMsg(sender, "&a->  Limit Warn Start: &f" + this.plugin.getConfigFile().limit_warn_start);
        CommandUtils.sendMsg(sender, "&a->  Restock Time: &f" + TimeParser.format(this.plugin.getConfigFile().restock_time));
        CommandUtils.sendMsg(sender, "&aNext Restock: &f" + TimeParser.timeUntil(this.plugin.getConfigFile().restock_time));
    }
}
