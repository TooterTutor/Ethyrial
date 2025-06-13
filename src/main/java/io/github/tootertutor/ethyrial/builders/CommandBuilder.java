package io.github.tootertutor.ethyrial.builders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * A simple command dispatcher that maps subcommand names to CommandExecutor instances.
 * It allows registering subcommands and dispatching execution to the appropriate executor.
 */
public class CommandBuilder {
    private final Map<String, CommandExecutor> commands = new HashMap<>();

    /**
     * Constructs a CommandBuilder.
     * @param plugin The plugin instance (currently unused).
     */
    public CommandBuilder(Plugin plugin) {
    }

    /**
     * Registers a subcommand executor with the given name.
     * @param name The subcommand name.
     * @param executor The CommandExecutor to handle the subcommand.
     */
    public void registerCommand(String name, CommandExecutor executor) {
        commands.put(name.toLowerCase(), executor);
    }

    /**
     * Executes the subcommand based on the first argument.
     * @param sender The command sender.
     * @param command The command.
     * @param label The command label.
     * @param args The command arguments.
     * @return true if the subcommand was found and executed, false otherwise.
     */
    public boolean executeCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false; // No subcommand provided
        }

        String subCommand = args[0].toLowerCase();
        CommandExecutor executor = commands.get(subCommand);
        if (executor != null) {
            // Remove the subcommand from the args array
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            return executor.onCommand(sender, command, label, newArgs);
        }
        return false; // Command not found
    }
}
