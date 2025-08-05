package io.github.tootertutor.ethyrial.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.menu.menus.CodexMenu;
import io.github.tootertutor.ethyrial.menu.menus.InfusionMenu;
import io.github.tootertutor.ethyrial.menu.menus.SkillTreeMenu;
import io.github.tootertutor.ethyrial.menu.menus.SpellTreeMenu;
import io.github.tootertutor.ethyrial.menu.menus.StatsMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Base menu showing player statistics.
 */
public class MainMenu extends Menu {

	private final Ethyrial plugin = Ethyrial.getInstance();
	private final PlayerData data;

	public MainMenu(Player player, PlayerData data) {
		super(player, 9 * 5, Component.text("Spell Book"));
		this.data = data;
	}

	@Override
	public void render() {
		MenuUtils.applyBorder(getInventory(), DyeColor.BLACK, plugin, MenuUtils.BorderStyle.CORNERS);

		// Stats
		setItem(22, MenuUtils.createItem(Material.PLAYER_HEAD, Component.text()
				.content("Stats")
				.color(NamedTextColor.GOLD)
				.decoration(TextDecoration.ITALIC, false)
				.build(), player.getUniqueId()), event -> {
					MenuManager.open(player, new StatsMenu(player, data));
				});

		// Skill Tree
		setItem(20, MenuUtils.createItem(Material.WRITABLE_BOOK, Component.text()
				.content("Skill Tree")
				.color(TextColor.color(0x478cd1))
				.decoration(TextDecoration.ITALIC, false)
				.build()), event -> {
					MenuManager.open(player, new SkillTreeMenu(player, data));
				});

		// Spell Tree
		setItem(24, MenuUtils.createItem(Material.WRITTEN_BOOK, Component.text()
				.content("Spell Tree")
				.color(TextColor.color(0x7a57ba))
				.decoration(TextDecoration.ITALIC, false)
				.build()), event -> {
					MenuManager.open(player, new SpellTreeMenu(player, data));
				});

		// Codex
		setItem(39, MenuUtils.createItem(Material.KNOWLEDGE_BOOK, Component.text()
				.content("Codex")
				.color(NamedTextColor.LIGHT_PURPLE)
				.decoration(TextDecoration.ITALIC, false)
				.build()), event -> {
					MenuManager.open(player, new CodexMenu(player, data));
				});

		// Infusions
		setItem(41, MenuUtils.createItem(Material.DRAGON_BREATH, Component.text()
				.content("Infusions")
				.color(TextColor.color(0xa857ba))
				.decoration(TextDecoration.ITALIC, false)
				.build()), event -> {
					MenuManager.open(player, new InfusionMenu(player, data));
				});

		// Wand & Staff
		plugin.getPlayerDAO().loadWandBindings(player.getUniqueId(), "wand").thenAccept(bindings -> {
			String leftSpell = bindings.left() != null ? bindings.left().getKey() : "None";
			String rightSpell = bindings.right() != null ? bindings.right().getKey() : "None";
			List<Component> lore = new ArrayList<>();

			// Left spell line
			lore.add(Component.text()
					.content("Left Core: ")
					.color(NamedTextColor.AQUA)
					.append(Component.text(leftSpell, NamedTextColor.WHITE))
					.decoration(TextDecoration.ITALIC, false)
					.build());

			// Right spell line
			lore.add(Component.text()
					.content("Right Core: ")
					.color(NamedTextColor.AQUA)
					.append(Component.text(rightSpell, NamedTextColor.WHITE))
					.decoration(TextDecoration.ITALIC, false)
					.build());

			Bukkit.getScheduler().runTask(plugin, () -> {
				setItem(3, MenuUtils.createItem(Material.STICK,
						Component.text()
								.content("Wand")
								.color(NamedTextColor.GOLD)
								.decoration(TextDecoration.ITALIC, false)
								.build(),
						player.getUniqueId(), lore),
						event -> {
							// Open wand management menu
						});
			});
		});

		plugin.getPlayerDAO().loadWandBindings(player.getUniqueId(), "staff").thenAccept(bindings -> {
			String leftSpell = bindings.left() != null ? bindings.left().getKey() : "None";
			String rightSpell = bindings.right() != null ? bindings.right().getKey() : "None";
			List<Component> lore = new ArrayList<>();

			// Left spell line
			lore.add(Component.text()
					.content("Left Core: ")
					.color(NamedTextColor.AQUA)
					.append(Component.text(leftSpell, NamedTextColor.WHITE))
					.decoration(TextDecoration.ITALIC, false)
					.build());

			// Right spell line
			lore.add(Component.text()
					.content("Right Core: ")
					.color(NamedTextColor.AQUA)
					.append(Component.text(rightSpell, NamedTextColor.WHITE))
					.decoration(TextDecoration.ITALIC, false)
					.build());

			setItem(5, MenuUtils.createItem(Material.STICK,
					Component.text()
							.content("Staff")
							.color(NamedTextColor.GOLD)
							.decoration(TextDecoration.ITALIC, false)
							.build(),
					player.getUniqueId(), lore),
					event -> {
						// Open wand management menu
					});
		});
	}

}
