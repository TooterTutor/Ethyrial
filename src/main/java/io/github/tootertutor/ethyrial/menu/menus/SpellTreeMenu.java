package io.github.tootertutor.ethyrial.menu.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.menu.Menu;
import io.github.tootertutor.ethyrial.menu.MenuUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SpellTreeMenu extends Menu {

    private final PlayerData data;

    public SpellTreeMenu(Player player, PlayerData data) {
        super(player, 54, Component.text("Spell Tree").color(NamedTextColor.DARK_PURPLE));
        this.data = data;
    }

    @Override
    public void render() {
        // Placeholder: Display coming soon
        setItem(22, MenuUtils.createItem(Material.ENCHANTED_BOOK,
                Component.text()
                        .content("Spell Tree Coming Soon")
                        .color(NamedTextColor.LIGHT_PURPLE)
                        .build()),
                event -> {
                    player.sendMessage("Clicked!");
                });
    }
}
