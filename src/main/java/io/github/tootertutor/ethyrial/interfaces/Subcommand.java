package io.github.tootertutor.ethyrial.interfaces;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface Subcommand {
    String getName();
    String getDescription();
    List<String> getAliases();

    boolean execute(CommandSender sender, String[] args);
    List<String> tabComplete(CommandSender sender, String[] args);
}
