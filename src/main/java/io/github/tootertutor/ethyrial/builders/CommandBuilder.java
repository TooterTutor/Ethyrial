package io.github.tootertutor.ethyrial.builders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Maps subcommands to executors and prints usage/arguments automatically.
 *
 * Example:
 * new CommandBuilder("ethyrial")
 * .register("give", new GiveCommand(), "/ethyrial give <item_key> [amount]
 * [player]");
 */
public class CommandBuilder implements CommandExecutor {

    private final Map<String, CommandExecutor> commands = new HashMap<>();
    private final Map<String, String> usages = new HashMap<>();
    private final String rootLabel;

    public CommandBuilder(String rootLabel) {
        this.rootLabel = rootLabel;
    }

    public CommandBuilder register(String name, CommandExecutor executor) {
        commands.put(name.toLowerCase(), executor);
        return this;
    }

    public CommandBuilder register(String name, CommandExecutor executor, String usage) {
        commands.put(name.toLowerCase(), executor);
        if (usage != null && !usage.isBlank()) {
            usages.put(name.toLowerCase(), usage);
        }
        return this;
    }

    public CommandBuilder usage(String name, String usage) {
        if (usage != null && !usage.isBlank()) {
            usages.put(name.toLowerCase(), usage);
        }
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        CommandExecutor exec = commands.get(sub);
        if (exec == null) {
            sender.sendMessage(Component.text("Unknown subcommand: ", NamedTextColor.RED)
                    .append(Component.text(sub, NamedTextColor.YELLOW)));
            sendHelp(sender);
            return true;
        }

        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        boolean ok = exec.onCommand(sender, command, label, newArgs);
        if (!ok) {
            String usage = usages.getOrDefault(sub, "/" + (rootLabel != null ? rootLabel : label) + " " + sub);
            sender.sendMessage(Component.text("Usage: ", NamedTextColor.GRAY)
                    .append(Component.text(usage, NamedTextColor.AQUA)));
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        if (commands.isEmpty()) {
            sender.sendMessage(Component.text("No subcommands registered.", NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("Available subcommands:", NamedTextColor.GOLD));
        for (var e : commands.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())) {
            String name = e.getKey();
            String usage = usages.getOrDefault(name, "/" + rootLabel + " " + name);
            sender.sendMessage(Component.text(" - ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(usage, NamedTextColor.AQUA)));
        }
    }
}
