package io.github.tootertutor.ethyrial.menu.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.menu.Menu;
import io.github.tootertutor.ethyrial.menu.MenuUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CodexMenu extends Menu {

    private final PlayerData data;

    public CodexMenu(Player player, PlayerData data) {
        super(player, 54, Component.text("Codex").color(NamedTextColor.DARK_AQUA));
        this.data = data;
    }

    @Override
    public void render() {
        setItem(22, MenuUtils.createItem(Material.BOOK,
                Component.text()
                        .content("Codex Coming Soon")
                        .color(NamedTextColor.AQUA)
                        .build()),
                event -> {
                    player.sendMessage("Clicked!");
                });
    }
}
