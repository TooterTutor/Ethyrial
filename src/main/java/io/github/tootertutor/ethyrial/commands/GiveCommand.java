package io.github.tootertutor.ethyrial.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.items.Item;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GiveCommand implements CommandExecutor {
    private final Ethyrial plugin;

    public GiveCommand(Ethyrial plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Usage: /ethyrial give <player> <item> <amount>
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /ethyrial give <player> <item> <amount>", NamedTextColor.RED));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + args[0], NamedTextColor.RED));
            return true;
        }

        NamespacedKey key = new NamespacedKey(plugin, args[1].toLowerCase());
        Item item = plugin.getItemsRegistered().getItem(key); // Make sure this method exists!

        if (item == null) {
            sender.sendMessage(Component.text("Item not found: " + args[1], NamedTextColor.RED));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < 1)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid amount: " + args[2], NamedTextColor.RED));
            return true;
        }

        ItemStack itemStack = item.getItemStack();
        itemStack.setAmount(amount);

        target.getInventory().addItem(itemStack);
        sender.sendMessage(Component.text("Gave " + amount + "x " + item.getKey().getKey() + " to " + target.getName(),
                NamedTextColor.GREEN));
        return true;
    }
}
