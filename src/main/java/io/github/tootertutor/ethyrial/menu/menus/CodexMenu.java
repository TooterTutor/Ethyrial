package io.github.tootertutor.ethyrial.menu.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.menu.MainMenu;
import io.github.tootertutor.ethyrial.menu.MenuManager;
import io.github.tootertutor.ethyrial.menu.MenuUtils;
import io.github.tootertutor.ethyrial.menu.PagedMenu;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CodexMenu extends PagedMenu<Spell> {
    private final Ethyrial plugin = Ethyrial.getInstance();

    private final PlayerData data;

    public CodexMenu(Player player, PlayerData data) {
        super(player, 54, Component.text("Codex").color(NamedTextColor.DARK_AQUA));
        this.data = data;
        setThemeColor(DyeColor.PURPLE);
        setBackTarget(new MainMenu(player, data));

        ArrayList<Spell> spellList = new ArrayList<>();
        plugin.getSpellsRegistered().getSpells().forEach(spellList::add);
        setItems(spellList);

        setItems(plugin.getSpellsRegistered().getSpells().stream().collect(Collectors.toList()));
        setItemsPerPage(3); // Number of domains per page

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

        SpellDomain[] allDomains = SpellDomain.values();
        int domainsPerPage = 3;
        int maxPerRow = 7;

        int startDomainIndex = page * domainsPerPage;
        int endDomainIndex = Math.min(startDomainIndex + domainsPerPage, allDomains.length);

        Map<SpellDomain, List<Spell>> grouped = items.stream()
                .collect(Collectors.groupingBy(Spell::getDomain));

        for (int domainOffset = 0; domainOffset < (endDomainIndex - startDomainIndex); domainOffset++) {
            int domainIndex = startDomainIndex + domainOffset;
            SpellDomain domain = allDomains[domainIndex];
            List<Spell> domainSpells = grouped.getOrDefault(domain, List.of());

            int row = domainOffset + 1;
            int labelSlot = row * 9;
            int baseSlot = labelSlot + 1;

            // Use the domain's text color to pick a corresponding pane color
            DyeColor dye = domain.getDyeColor();

            // Set domain label
            setItem(labelSlot, MenuUtils.createGlassPane(dye,
                    Component.text(domain.name(), domain.getTextColor()), plugin));

            // Set spells in row
            for (int i = 0; i < Math.min(domainSpells.size(), maxPerRow); i++) {
                Spell spell = domainSpells.get(i);
                int slot = baseSlot + i;
                setItem(slot, spell.toItemStack(player), event -> {
                    // player.sendMessage(Component.text("You clicked " + spell.getName()));
                    ClickType click = event.getClick();
                    boolean isLeft = click.isLeftClick();

                    if (isLeft) {
                        // Open wand selection menu for Wand with this spell and click type
                        MenuManager.open(player, new WandSelectMenu(player, data, "wand", spell, isLeft));
                    } else if (click.isRightClick()) {
                        // Open wand selection menu for BetaWand with this spell and click type
                        MenuManager.open(player, new WandSelectMenu(player, data, "staff", spell, !isLeft));
                    }

                });
            }
        }
    }
}
