package io.github.tootertutor.ethyrial.interfaces;

import org.bukkit.entity.Player;

public interface OpensNativeView {
    /** Open a native container (e.g., anvil) instead of Menu#getInventory(). */
    void openNative(Player player);
}
