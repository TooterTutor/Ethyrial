package io.github.tootertutor.ethyrial.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class ManaManager {
    private final Map<UUID, Integer> mana = new HashMap<>();
    private final Map<UUID, Integer> maxMana = new HashMap<>();

    public int getMana(Player player) {
        return mana.getOrDefault(player.getUniqueId(), 100);
    }

    public void setMana(Player player, int amount) {
        mana.put(player.getUniqueId(), Math.max(0, Math.min(amount, getMaxMana(player))));
    }

    public int getMaxMana(Player player) {
        return maxMana.getOrDefault(player.getUniqueId(), 100);
    }

    public void setMaxMana(Player player, int amount) {
        maxMana.put(player.getUniqueId(), amount);
    }

    public boolean useMana(Player player, int amount) {
        int currentMana = getMana(player);
        if (currentMana >= amount) {
            setMana(player, currentMana - amount);
            return true;
        }
        return false;
    }
}
