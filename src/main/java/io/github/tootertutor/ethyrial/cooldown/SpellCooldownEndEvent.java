package io.github.tootertutor.ethyrial.cooldown;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpellCooldownEndEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final NamespacedKey spellKey;

    public SpellCooldownEndEvent(Player player, NamespacedKey spellKey) {
        super(false);
        this.player = player;
        this.spellKey = spellKey;
    }

    public Player getPlayer() {
        return player;
    }

    public NamespacedKey getSpellKey() {
        return spellKey;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
