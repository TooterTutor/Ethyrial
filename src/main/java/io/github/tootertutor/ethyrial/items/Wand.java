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

public class Wand extends Item implements AutoRegisterItem {

    private final Ethyrial plugin = Ethyrial.getInstance();

    protected Wand(Ethyrial plugin) {
        super(plugin,
                "wand",
                "Wand",
                "#4aeaff",
                List.of("Cast a Spell using", "Left Click or Right Click"),
                List.of("#2fc9d4", "#2fc9d4"),
                Material.STICK,
                new ItemStack(Material.STICK)

        );
        applyMetadata();
    }

    @EventHandler
    public void onUseWand(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack wandItem = event.getItem();
        Action action = event.getAction();

        if (!isItem(wandItem))
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
                        player.sendMessage(Component.text("No spell bound to this Wand slot!", NamedTextColor.RED));
                    }
                });

    }

}
