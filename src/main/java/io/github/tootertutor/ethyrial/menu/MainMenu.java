package io.github.tootertutor.ethyrial.menu;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.menu.menus.CodexMenu;
import io.github.tootertutor.ethyrial.menu.menus.InfusionMenu;
import io.github.tootertutor.ethyrial.menu.menus.SkillTreeMenu;
import io.github.tootertutor.ethyrial.menu.menus.SpellTreeMenu;
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
		super(player, 54, Component.text("Stats"));
		this.data = data;
	}

	@Override
	public void render() {
		MenuUtils.applyBorder(getInventory(), DyeColor.BLACK, plugin, MenuUtils.BorderStyle.CORNERS);
		// #region Stats
		setItem(11, MenuUtils.createItem(Material.GLOWSTONE_DUST,
				Component.text()
						.content("Mana: ")
						.color(NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false)
						.append(
								Component.text(data.getStats().getMana())
										.color(NamedTextColor.BLUE))
						.build()));
		setItem(13,
				MenuUtils.createItem(Material.IRON_SWORD,
						Component.text()
								.content("Spell Power: ")
								.color(NamedTextColor.GRAY)
								.decoration(TextDecoration.ITALIC, false)
								.append(
										Component.text(data.getStats().getSpellPower())
												.color(NamedTextColor.GOLD))
								.build()));
		setItem(15, MenuUtils.createItem(Material.TOTEM_OF_UNDYING,
				Component.text()
						.content("Bonus Health: ")
						.color(NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false)
						.append(
								Component.text(data.getStats().getBonusHealth())
										.color(NamedTextColor.GREEN))
						.build()));
		// #endregion

		// #region Magic
		setItem(37, MenuUtils.createItem(Material.KNOWLEDGE_BOOK, Component.text()
				.content("Codex")
				.color(NamedTextColor.LIGHT_PURPLE)
				.decoration(TextDecoration.ITALIC, false)
				.build()), event -> {
					MenuManager.open(player, new CodexMenu(player, data));
				});

		setItem(30, MenuUtils.createItem(Material.WRITTEN_BOOK, Component.text()
				.content("Spell Tree")
				.color(TextColor.color(0x7a57ba))
				.decoration(TextDecoration.ITALIC, false)
				.build()), event -> {
					MenuManager.open(player, new SpellTreeMenu(player, data));
				});

		setItem(32, MenuUtils.createItem(Material.PLAYER_HEAD, Component.text()
				.content("Skill Tree")
				.color(TextColor.color(0x7a57ba))
				.decoration(TextDecoration.ITALIC, false)
				.build(), player.getUniqueId()), event -> {
					MenuManager.open(player, new SkillTreeMenu(player, data));
				});

		setItem(43, MenuUtils.createItem(Material.DRAGON_BREATH, Component.text()
				.content("Infusion")
				.color(TextColor.color(0xa857ba))
				.decoration(TextDecoration.ITALIC, false)
				.build()), event -> {
					MenuManager.open(player, new InfusionMenu(player, data));
				});

		// #endregion
	}
}
