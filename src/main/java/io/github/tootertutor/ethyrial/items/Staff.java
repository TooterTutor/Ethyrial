package io.github.tootertutor.ethyrial.items;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterItem;
import io.github.tootertutor.ethyrial.spells.SpellUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Staff extends Item implements AutoRegisterItem {

    private final Ethyrial plugin = Ethyrial.getInstance();

    protected Staff(Ethyrial plugin) {
        super(plugin,
                "staff",
                "Staff",
                "#1e9b39",
                List.of("Cast a Spell using", "Left Click or Right Click"),
                List.of("#1e9b71", "#1e9b71"),
                Material.STICK,
                new ItemStack(Material.STICK)

        );
        applyMetadata();
    }

    @EventHandler
    public void onUseStaff(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack staffItem = event.getItem();
        Action action = event.getAction();

        if (!isItem(staffItem))
            return;

        // Determine click type
        boolean isLeftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;

        event.setCancelled(true);

        UUID uuid = player.getUniqueId();

        plugin.getPlayerDAO()
                .loadWandBindings(uuid, getKey().getKey())
                .thenAccept(pair -> {
                    NamespacedKey selectedKey = (isLeftClick ? pair.left() : pair.right());

                    if (selectedKey != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> SpellUtils.castByKey(selectedKey, player));
                    } else {
                        player.sendMessage(Component.text("No spell bound to this Staff slot!", NamedTextColor.RED));
                    }
                });

    }

}
