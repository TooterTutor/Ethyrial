package io.github.tootertutor.ethyrial.menu.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.data.SpellSlots;
import io.github.tootertutor.ethyrial.menu.Menu;
import io.github.tootertutor.ethyrial.menu.MenuManager;
import io.github.tootertutor.ethyrial.menu.MenuUtils;
import io.github.tootertutor.ethyrial.spells.Spell;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WandSelectMenu extends Menu {

    private final Ethyrial plugin = Ethyrial.getInstance();
    private final PlayerData data;

    private final Player player;
    private final Spell spell;
    private String wandId;
    private SpellSlots slots;

    public WandSelectMenu(Player player, PlayerData data, String wandId, Spell spell, boolean isLeftClick) {
        super(player, 9, Component.text("Select Slot For " + wandId).color(NamedTextColor.GOLD));
        this.data = data;
        this.player = player;
        this.spell = spell;
        this.wandId = wandId;

        loadBindings();
    }

    @Override
    public void render() {
        setItem(2, MenuUtils.createItem(Material.BLUE_DYE, Component.text("Bind to LEFT Click", NamedTextColor.BLUE)),
                e -> {
                    saveBinding(true);
                });

        setItem(1, spell.toItemStack(player));

        setItem(6, MenuUtils.createItem(Material.RED_DYE, Component.text("Bind to RIGHT Click", NamedTextColor.RED)),
                e -> {
                    saveBinding(false);
                });

        setItem(8, MenuUtils.createItem(Material.BARRIER, Component.text("Cancel", NamedTextColor.GRAY)), e -> {
            MenuManager.open(player, new SpellTreeMenu(player, data));
        });

    }

    private void saveBinding(boolean isLeftClick) {
        var dao = Ethyrial.getInstance().getPlayerDAO();
        var spellKey = spell.getKey();

        dao.saveWandBindings(player.getUniqueId(), wandId, isLeftClick, spellKey)
                .thenRun(() -> {
                    player.sendMessage(Component.text(
                            "Successfully bound " + spell.getName() + " to " + wandId + " for "
                                    + (isLeftClick ? "LEFT" : "RIGHT") + " click.",
                            NamedTextColor.GREEN));
                });
        MenuManager.open(player, new SpellTreeMenu(player, data));
    }

    private void loadBindings() {
        plugin.getPlayerDAO()
                .loadWandBindings(player.getUniqueId(), wandId)
                .thenAccept(slots -> {
                    this.slots = new SpellSlots(player.getUniqueId(), slots.left());
                });
    }

}
