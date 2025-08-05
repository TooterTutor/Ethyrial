package io.github.tootertutor.ethyrial.builders;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.tootertutor.ethyrial.Ethyrial;
import net.kyori.adventure.text.Component;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Ethyrial plugin) {
        this.item = new ItemStack(Material.STONE); // Default
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder setMaterial(Material material) {
        item.setType(material);
        return this;
    }

    public ItemBuilder name(Component name) {
        if (meta != null)
            meta.displayName(name);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        if (meta != null)
            meta.lore(lore);
        return this;
    }

    public ItemBuilder enchant(Enchantment ench, int level) {
        item.addUnsafeEnchantment(ench, level);
        return this;
    }

    public ItemBuilder enchantments(Map<Enchantment, Integer> enchants) {
        item.addUnsafeEnchantments(enchants);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        if (meta != null)
            meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        if (meta != null)
            meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemStack buildItemStack() {
        if (meta != null)
            item.setItemMeta(meta);
        return item;
    }
}