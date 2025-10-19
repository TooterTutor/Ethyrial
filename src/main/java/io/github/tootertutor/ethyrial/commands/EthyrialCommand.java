package io.github.tootertutor.ethyrial.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.github.tootertutor.ethyrial.interfaces.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EthyrialCommand implements CommandExecutor, TabCompleter {

    private final Map<String, Subcommand> subcommands = new HashMap<>();
    private final Map<String, String> usageOverrides = new HashMap<>();
    private final String rootLabel;

    public EthyrialCommand(String rootLabel) {
        this.rootLabel = rootLabel;
    }

    public EthyrialCommand register(Subcommand cmd) {
        subcommands.put(cmd.getName().toLowerCase(), cmd);
        if (cmd.getAliases() != null) {
            for (String a : cmd.getAliases()) {
                if (a != null && !a.isBlank())
                    subcommands.put(a.toLowerCase(), cmd);
            }
        }
        return this;
    }

    /** Optionally override usage text per subcommand/alias. */
    public EthyrialCommand usage(String name, String usage) {
        if (usage != null && !usage.isBlank()) {
            usageOverrides.put(name.toLowerCase(), usage);
        }
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        Subcommand sub = subcommands.get(args[0].toLowerCase());
        if (sub == null) {
            sender.sendMessage(Component.text("Unknown subcommand: ", NamedTextColor.RED)
                    .append(Component.text(args[0], NamedTextColor.YELLOW)));
            sendHelp(sender);
            return true;
        }

        boolean ok = sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        if (!ok) {
            sendUsage(sender, label, sub, args[0].toLowerCase());
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("Ethyrial Commands:", NamedTextColor.GOLD));
        Set<Subcommand> unique = new HashSet<>(subcommands.values());
        for (Subcommand sc : unique.stream().sorted(Comparator.comparing(Subcommand::getName))
                .collect(Collectors.toList())) {
            sender.sendMessage(Component.text(" - ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(getUsageText(sc), NamedTextColor.AQUA))
                    .append(Component.text(" — ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(sc.getDescription(), NamedTextColor.GRAY)));
        }
    }

    private void sendUsage(CommandSender sender, String label, Subcommand sub, String keyUsed) {
        String usage = usageOverrides.getOrDefault(keyUsed, getUsageText(sub));
        sender.sendMessage(Component.text("Usage: ", NamedTextColor.GRAY)
                .append(Component.text(usage, NamedTextColor.AQUA)));
    }

    private String getUsageText(Subcommand sub) {
        String byName = usageOverrides.get(sub.getName().toLowerCase());
        if (byName != null)
            return byName;

        // Reflective support for optional getUsage() without changing Subcommand
        try {
            var m = sub.getClass().getMethod("getUsage");
            Object o = m.invoke(sub);
            if (o instanceof String s && !s.isBlank())
                return s;
        } catch (ReflectiveOperationException ignored) {
        }

        return "/" + rootLabel + " " + sub.getName();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return subcommands.keySet().stream()
                    .filter(k -> k.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        Subcommand sub = subcommands.get(args[0].toLowerCase());
        if (sub != null) {
            return sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        }
        return Collections.emptyList();
    }
}
