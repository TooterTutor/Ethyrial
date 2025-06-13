package io.github.tootertutor.ethyrial.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.Subcommand;

public class EthyrialCommand implements CommandExecutor, TabCompleter {
    private final Map<String, Subcommand> subcommands = new HashMap<>();

    public EthyrialCommand(Ethyrial plugin) {
        // Discover or manually register
        registerSubcommand(new GiveBookCommand(plugin));
        registerSubcommand(new CastCommand(plugin));
    }

    private void registerSubcommand(Subcommand cmd) {
        subcommands.put(cmd.getName().toLowerCase(), cmd);
        for (String alias : cmd.getAliases()) {
            subcommands.put(alias.toLowerCase(), cmd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        Subcommand cmd = subcommands.get(args[0].toLowerCase());
        if (cmd != null) return cmd.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return subcommands.keySet().stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }

        Subcommand cmd = subcommands.get(args[0].toLowerCase());
        if (cmd != null)
            return cmd.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        return Collections.emptyList();
    }
}
