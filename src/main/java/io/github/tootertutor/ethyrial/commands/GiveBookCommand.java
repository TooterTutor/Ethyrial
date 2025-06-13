package io.github.tootertutor.ethyrial.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.Subcommand;
import io.github.tootertutor.ethyrial.items.SpellbookItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GiveBookCommand implements Subcommand {
    private final Ethyrial plugin;

    public GiveBookCommand(Ethyrial plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "givebook";
    }

    @Override
    public String getDescription() {
        return "Gives the player a spellbook.";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        // Get the registered SpellbookItem from your item manager (or instantiate it)
        SpellbookItem spellbook = new SpellbookItem(plugin);
        player.getInventory().addItem(spellbook.getItemStack());

        player.sendMessage(Component.text("You have been given a Spellbook!", NamedTextColor.GOLD));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
