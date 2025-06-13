package io.github.tootertutor.ethyrial.items;

import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.data.PlayerDataManager;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterItem;
import io.github.tootertutor.ethyrial.menu.MainMenu;
import io.github.tootertutor.ethyrial.menu.MenuManager;

public class SpellbookItem extends Item implements Listener, AutoRegisterItem {

    public SpellbookItem(Ethyrial plugin) {
        super(plugin, new NamespacedKey(plugin, "spellbook"));

        this.displayName = "Spellbook";
        this.nameColor = "#B084FF";
        this.lore = List.of("Harness the powers of the arcane");
        this.loreColor = List.of("#A0A0A0");
        this.material = Material.WRITTEN_BOOK;
        this.itemStack = new ItemStack(this.material);

        applyMetadata();
    }

    @EventHandler
    public void onPlayerUseSpellbook(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
            return;

        if (item != null && isItem(item)) {
            event.setCancelled(true);

            UUID uuid = player.getUniqueId();
            PlayerDataManager.getInstance().get(uuid).thenAccept(data -> {
                MainMenu menu = new MainMenu(player, data);
                MenuManager.open(player, menu);
            });
        }
    }

}
