package io.github.tootertutor.ethyrial.menu.menus;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.data.PlayerStats;
import io.github.tootertutor.ethyrial.menu.MainMenu;
import io.github.tootertutor.ethyrial.menu.Menu;
import io.github.tootertutor.ethyrial.menu.MenuUtils;
import io.github.tootertutor.ethyrial.menu.PagedMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StatsMenu extends PagedMenu<Menu> {

	private final Ethyrial plugin = Ethyrial.getInstance();
	private final PlayerData data;

	public StatsMenu(Player player, PlayerData data) {
		super(player, 36, Component.text("Stats Menu").color(NamedTextColor.GOLD));
		this.data = data;
		setThemeColor(DyeColor.CYAN);
		setBackTarget(new MainMenu(player, data));
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void renderPage(int startIndex) {
		clearItems();
		MenuUtils.applyBorder(getInventory(), DyeColor.GRAY, plugin, MenuUtils.BorderStyle.FULL);

		PlayerStats playerStats = data.getStats();

		// Live values from Bukkit for current health, max base, etc.
		double healthNow = player.getHealth();
		double maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null
				? player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
				: 20.0;
		double maxHealthTotal = maxHealthAttr + playerStats.getHealthBonus();

		// Layout suggestions for 36-slot menu
		setItem(10, statItem(
				Material.LAPIS_LAZULI,
				Component.text("Mana").color(NamedTextColor.AQUA),
				new Component[] {
						Component
								.text(playerStats.getMaxMana() > 0
										? (playerStats.getMana() + " / " + playerStats.getMaxMana())
										: String.valueOf(playerStats.getMana()))
								.color(NamedTextColor.WHITE),
						Component.text("Regen: " + playerStats.getManaRegen()).color(NamedTextColor.DARK_AQUA)
				}));

		setItem(12, statItem(
				Material.RED_DYE,
				Component.text("Health").color(NamedTextColor.RED),
				new Component[] {
						Component.text(String.format("%.1f / %.1f", healthNow, maxHealthTotal))
								.color(NamedTextColor.WHITE),
						Component
								.text(playerStats.getHealthBonus() > 0
										? ("Bonus Health: +" + playerStats.getHealthBonus())
										: "No bonus health")
								.color(playerStats.getHealthBonus() > 0 ? NamedTextColor.GREEN
										: NamedTextColor.DARK_GRAY)
				}));

		setItem(14, statItem(
				Material.BLAZE_POWDER,
				Component.text("Spell Power").color(NamedTextColor.LIGHT_PURPLE),
				new Component[] {
						Component.text(String.valueOf(playerStats.getSpellPower())).color(NamedTextColor.WHITE),
						Component.text("Increases potency of spells.").color(NamedTextColor.GRAY)
				}));

		setItem(16, statItem(
				Material.IRON_SWORD,
				Component.text("Strength").color(NamedTextColor.GOLD),
				new Component[] {
						Component.text(String.valueOf(playerStats.getStrength())).color(NamedTextColor.WHITE),
						Component.text("Boosts melee damage.").color(NamedTextColor.GRAY)
				}));

		setItem(19, statItem(
				Material.RABBIT_FOOT,
				Component.text("Agility").color(NamedTextColor.GREEN),
				new Component[] {
						Component.text(String.valueOf(playerStats.getAgility())).color(NamedTextColor.WHITE),
						Component.text("Mobility & finesse.").color(NamedTextColor.GRAY)
				}));

		setItem(21, statItem(
				Material.SHIELD,
				Component.text("Defense").color(NamedTextColor.BLUE),
				new Component[] {
						Component.text(String.valueOf(playerStats.getDefense())).color(NamedTextColor.WHITE),
						Component.text("Damage mitigation.").color(NamedTextColor.GRAY)
				}));

		setItem(23, statItem(
				Material.ENCHANTED_BOOK,
				Component.text("Unlocked Spells").color(NamedTextColor.DARK_AQUA),
				new Component[] {
						Component.text(String.valueOf(data.getUnlockedSpells().size()))
								.color(NamedTextColor.WHITE),
						Component.text("Total spells learned.").color(NamedTextColor.GRAY)
				}));

		setItem(25, statItem(
				Material.NETHER_STAR,
				Component.text("Crits").color(NamedTextColor.YELLOW),
				new Component[] {
						Component.text("Chance: " + playerStats.getCritChance() + "%").color(NamedTextColor.WHITE),
						Component.text("Damage: +" + playerStats.getCritDamage() + "%").color(NamedTextColor.WHITE)
				}));
	}

	/* helpers inside StatsMenu */
	private ItemStack statItem(Material material, Component name, Component[] lore) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(name);
		if (lore != null && lore.length > 0)
			meta.lore(java.util.Arrays.asList(lore));
		item.setItemMeta(meta);
		return item;
	}

}
