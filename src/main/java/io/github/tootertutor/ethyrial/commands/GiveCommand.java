package io.github.tootertutor.ethyrial.commands;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterCommand;
import io.github.tootertutor.ethyrial.interfaces.Subcommand;
import io.github.tootertutor.ethyrial.items.Item;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GiveCommand implements Subcommand, AutoRegisterCommand {

    private final Ethyrial plugin = Ethyrial.getInstance();

    public String getUsage() {
        return "/ethyrial give <item_key> [amount] [player]";
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getDescription() {
        return "Give an Ethyrial item to yourself or another player.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1)
            return false; // show usage

        // Parse key
        final NamespacedKey key;
        try {
            key = parseKey(args[0]);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(Component.text("Invalid item key: ", NamedTextColor.RED)
                    .append(Component.text(args[0], NamedTextColor.YELLOW)));
            return false;
        }

        // Parse amount
        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException nfe) {
                sender.sendMessage(Component.text("Invalid amount: ", NamedTextColor.RED)
                        .append(Component.text(args[1], NamedTextColor.YELLOW)));
                return false;
            }
        }

        // Resolve player
        Player target = null;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: ", NamedTextColor.RED)
                        .append(Component.text(args[2], NamedTextColor.YELLOW)));
                return false;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(
                    Component.text("You must specify a player when running from console.", NamedTextColor.RED));
            return false;
        }

        // Fetch item prototype
        Item item = plugin.getItemsRegistered().getItem(key);
        if (item == null) {
            sender.sendMessage(Component.text("Unknown item: ", NamedTextColor.RED)
                    .append(Component.text(key.asString(), NamedTextColor.YELLOW)));
            return false;
        }

        // Build a fresh stack (apply metadata, then clone to avoid mutating prototype)
        ItemStack stack = item.getItemStack().clone();
        stack.setAmount(amount);
        target.getInventory().addItem(stack);

        sender.sendMessage(Component.text("Gave ", NamedTextColor.GREEN)
                .append(Component.text(amount + "x ", NamedTextColor.GREEN))
                .append(Component.text(key.getKey(), NamedTextColor.AQUA))
                .append(Component.text(" to ", NamedTextColor.GREEN))
                .append(Component.text(target.getName(), NamedTextColor.AQUA)));
        return true;
    }

    private NamespacedKey parseKey(String raw) {
        String s = raw.toLowerCase(Locale.ROOT);
        if (s.contains(":"))
            return NamespacedKey.fromString(s);
        return new NamespacedKey(plugin, s); // default to your plugin namespace
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // /ethyrial give <item_key> [amount] [player]
        if (args.length == 1) {
            final String prefix = args[0].toLowerCase(Locale.ROOT);
            // Suggest all registered item keys
            return plugin.getItemsRegistered().getItems().stream()
                    .map(i -> i.getKey().asString())
                    .filter(k -> k.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            final String prefix = args[1];
            return List.of("1", "8", "16", "32", "64").stream()
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            final String prefix = args[2].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return List.of();
    }

}
