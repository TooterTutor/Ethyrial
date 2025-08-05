package io.github.tootertutor.ethyrial.spells;

import org.bukkit.DyeColor;
import org.bukkit.Material;

import net.kyori.adventure.text.format.NamedTextColor;

public enum SpellDomain {
    TEMERIS(NamedTextColor.DARK_PURPLE, Material.CLOCK, DyeColor.PURPLE),
    VEYRUUN(NamedTextColor.BLUE, Material.ENDER_PEARL, DyeColor.BLUE),
    AURALIS(NamedTextColor.YELLOW, Material.FIRE_CHARGE, DyeColor.YELLOW),
    TRANSARIS(NamedTextColor.GREEN, Material.SLIME_BALL, DyeColor.LIME),
    NOCTYRA(NamedTextColor.DARK_RED, Material.FERMENTED_SPIDER_EYE, DyeColor.RED),
    MORTYXIS(NamedTextColor.GRAY, Material.BONE, DyeColor.GRAY);

    private final NamedTextColor textColor;
    private final Material iconMaterial;
    private final DyeColor dyeColor;

    SpellDomain(NamedTextColor textColor, Material iconMaterial, DyeColor dyeColor) {
        this.textColor = textColor;
        this.iconMaterial = iconMaterial;
        this.dyeColor = dyeColor;
    }

    public NamedTextColor getTextColor() {
        return textColor;
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }
}
