package io.github.tootertutor.ethyrial.spells;

import java.util.List;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.builders.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class Spell implements Keyed {
    protected final Ethyrial plugin;
    private NamespacedKey key;
    private String name;
    private String description;
    private int manaCost;
    private int cooldownTicks;
    private SpellDomain domain;

    protected Spell(Ethyrial plugin, String key, String name, String description, int manaCost,
            int cooldownTicks, SpellDomain domain) {
        this.plugin = plugin;
        this.key = new NamespacedKey(Ethyrial.getInstance(), key);
        this.name = name;
        this.description = description;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
        this.domain = domain;
    }

    public abstract void cast(Player caster);

    public NamespacedKey getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public SpellDomain getDomain() {
        return domain;
    }

    public int getPowerLevel() {
        return 1; // Or fetch dynamically
    }

    public Rarity getRarity() {
        return Rarity.COMMON; // Replace with actual logic
    }

    public boolean isUnlocked(Player player) {
        return true; // Implement based on player data
    }

    public ItemStack toItemStack(Player viewer) {
        List<Component> lore = List.of(
                Component.text(description, NamedTextColor.GRAY),
                Component.text("Mana: ", NamedTextColor.AQUA)
                        .append(Component.text(String.valueOf(manaCost), NamedTextColor.WHITE)),
                Component.text("Cooldown: ", NamedTextColor.YELLOW)
                        .append(Component.text((cooldownTicks / 20.0) + "s", NamedTextColor.WHITE)),
                Component.empty(),
                Component.text("Power Level: ", NamedTextColor.DARK_GREEN)
                        .append(Component.text(String.valueOf(getPowerLevel()), NamedTextColor.GREEN)),
                Component.text("Rarity: ", NamedTextColor.GOLD)
                        .append(Component.text(getRarity().getDisplayName(), NamedTextColor.WHITE)),
                Component.text(isUnlocked(viewer) ? "Unlocked" : "Locked",
                        isUnlocked(viewer) ? NamedTextColor.GREEN : NamedTextColor.RED));

        return new ItemBuilder(plugin)
                .setMaterial(domain.getIconMaterial())
                .name(Component.text(name, domain.getTextColor())
                        .decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .buildItemStack();
    }

}
