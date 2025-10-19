package io.github.tootertutor.ethyrial.menu.menus;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.menu.MainMenu;
import io.github.tootertutor.ethyrial.menu.MenuUtils;
import io.github.tootertutor.ethyrial.menu.PagedMenu;
import io.github.tootertutor.ethyrial.spells.Spell;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SpellTreeMenu extends PagedMenu<Spell> {

    private final Ethyrial plugin = Ethyrial.getInstance();

    private final PlayerData data;

    public SpellTreeMenu(Player player, PlayerData data) {
        super(player, 54, Component.text("Spell Tree").color(NamedTextColor.DARK_PURPLE));
        this.data = data;
        setThemeColor(DyeColor.PURPLE);
        setBackTarget(new MainMenu(player, data));

    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    protected void renderPage(int startIndex) {
        clearItems(); // Important to prevent overlap when changing pages

        MenuUtils.applyBorder(getInventory(), DyeColor.GRAY, plugin, MenuUtils.BorderStyle.TOP,
                MenuUtils.BorderStyle.BOTTOM);
    }

}
