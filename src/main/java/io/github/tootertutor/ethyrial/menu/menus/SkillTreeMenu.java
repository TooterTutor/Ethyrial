package io.github.tootertutor.ethyrial.menu.menus;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.menu.MainMenu;
import io.github.tootertutor.ethyrial.menu.PagedMenu;
import io.github.tootertutor.ethyrial.spells.Spell;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SkillTreeMenu extends PagedMenu<Spell> {
    private final Ethyrial plugin = Ethyrial.getInstance();

    private final PlayerData data;

    public SkillTreeMenu(Player player, PlayerData data) {
        super(player, 54, Component.text("Skill Tree").color(NamedTextColor.GREEN));
        this.data = data;
        setThemeColor(DyeColor.GREEN);
        setBackTarget(new MainMenu(player, data));
    }

    @Override
    public void render() {
        super.render();

    }

    @Override
    protected void renderPage(int startIndex) {

    }
}
