package io.github.tootertutor.ethyrial.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.Subcommand;
import io.github.tootertutor.ethyrial.spells.Spell;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CastCommand implements Subcommand {
    private final Ethyrial plugin;

    public CastCommand(Ethyrial plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "cast";
    }

    @Override
    public String getDescription() {
        return "Cast a spell";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /cast <spell> [player]", NamedTextColor.RED));
            return true;
        }

        String spellKey = args[0].toLowerCase();
        Spell spell = plugin.getSpellsRegistered().getSpells().stream()
                .filter(s -> s.getKey().toString().equalsIgnoreCase(spellKey))
                .findFirst()
                .orElse(null);

        if (spell == null) {
            sender.sendMessage(Component.text("Unknown spell: " + spellKey, NamedTextColor.RED));
            return true;
        }

        Player target;
        if (args.length > 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(Component.text("You must specify a player or target", NamedTextColor.RED));
            return true;
        }

        spell.cast(target);
        sender.sendMessage(Component.text("Cast " + spellKey + " on " + target.getName(), NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getSpellsRegistered().getSpells().stream()
                    .map(spell -> spell.getKey().toString())
                    .filter(k -> k.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Arrays.asList();
    }

}
