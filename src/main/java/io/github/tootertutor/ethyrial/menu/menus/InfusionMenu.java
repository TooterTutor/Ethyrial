package io.github.tootertutor.ethyrial.menu.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.menu.Menu;
import io.github.tootertutor.ethyrial.menu.MenuUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class InfusionMenu extends Menu {

    private final PlayerData data;

    public InfusionMenu(Player player, PlayerData data) {
        super(player, 54, Component.text("Infusion").color(NamedTextColor.DARK_GREEN));
        this.data = data;
    }

    @Override
    public void render() {
        setItem(22, MenuUtils.createItem(Material.DRAGON_BREATH,
                Component.text("Infusion Coming Soon").color(NamedTextColor.GREEN)));
    }
}
