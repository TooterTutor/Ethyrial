package io.github.tootertutor.ethyrial.interfaces;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface Subcommand {
    String getName();

    String getDescription();

    default List<String> getAliases() {
        return List.of();
    }

    /** Return empty string if you prefer the registry to fill from @CommandMeta. */
    default String getUsage() {
        return "";
    }

    /** Return true if executed, false to show usage. */
    boolean execute(CommandSender sender, String[] args);

    /** Tab complete just the sub-args (not including the subcommand token). */
    default List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
