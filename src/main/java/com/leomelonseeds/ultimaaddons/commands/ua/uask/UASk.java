package com.leomelonseeds.ultimaaddons.commands.ua.uask;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.leomelonseeds.ultimaaddons.commands.BaseCommand;
import com.leomelonseeds.ultimaaddons.commands.Command;
import com.leomelonseeds.ultimaaddons.commands.ua.uask.sub.*;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UASk extends Command {
    private static final Map<String, Argument> argumentTypes = BaseCommand.argumentTypes;
    private final Map<String, ? extends Command> subcommands;

    public UASk(String name, List<String> aliases, String permission, String description) {
        super(name, aliases, permission, description, Collections.emptyList());
        subcommands = Map.of(
                "debug", new DebugCommand("debug", Collections.emptyList(), "ua.sk.debug", "",
                        Collections.emptyList()
                ),
                "discount", new DiscountCommand("discount", Collections.emptyList(), "ua.sk.discount", "",
                        List.of(
                                argumentTypes.get("shopkeeper"),
                                argumentTypes.get("player"),
                                argumentTypes.get("int")
                        )
                ),
                "help", new HelpCommand("help", Collections.emptyList(), "ua.sk.help", "",
                        Collections.emptyList()
                ),
                "info", new InfoCommand("info", Collections.emptyList(), "ua.sk.info", "",
                        List.of(
                                argumentTypes.get("shopkeeper")
                        )
                ),
                "limit", new LimitCommand("limit", Collections.emptyList(), "ua.sk.limit", "",
                        List.of(
                                argumentTypes.get("shopkeeper"),
                                argumentTypes.get("player"),
                                argumentTypes.get("int")
                        )
                ),
                "rotate", new RotateCommand("rotate", Collections.emptyList(), "ua.sk.rotate", "",
                        List.of(
                                argumentTypes.get("shopkeeper")
                        )
                ),
                "sync", new SyncCommand("sync", Collections.emptyList(), "ua.sk.sync", "",
                        List.of(
                                argumentTypes.get("shopkeeper"),
                                argumentTypes.get("shopkeeper")
                        )
                ),
                "unsync", new UnsyncCommand("unsync", Collections.emptyList(), "ua.sk.unsync", "",
                        List.of(
                                argumentTypes.get("shopkeeper")
                        )
                )
        );
    }

    @Override
    public boolean hasInvalidArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0)
            return false;
        if (!subcommands.containsKey(args[0])) {
            CommandUtils.sendErrorMsg(sender, "Invalid subcommand. Use &b/usk help&7 for help");
            return true;
        }
        return false;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String name, @NotNull String[] args) {
        Command subCmd = args.length < 1
                ? subcommands.get("help")
                : subcommands.get(args[0]);
        if (!subCmd.hasPermission(sender))
            CommandUtils.sendErrorMsg(sender, "You do not have permission to run this subcommand");

        // Get rid of base sk command and subcommand and only pass in args if we can
        String[] truncArgs = args.length > 0 ? Arrays.copyOfRange(args, 1, args.length) : args;
        if (!subCmd.hasInvalidArgs(sender, truncArgs))
            subCmd.execute(sender, cmd, name, truncArgs);
    }

    @Override
    public List<String> handleTab(@NotNull String[] args) {
        if (args.length <= 1)
            return subcommands.keySet().stream().toList();
        Command subCmd = subcommands.get(args[0]);
        if (subCmd == null)
            return Collections.emptyList();
        return subCmd.handleTab(Arrays.copyOfRange(args, 1, args.length));
    }
}
