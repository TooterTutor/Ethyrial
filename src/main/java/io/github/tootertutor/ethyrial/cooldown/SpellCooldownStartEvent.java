package io.github.tootertutor.ethyrial.cooldown;

import java.time.Duration;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpellCooldownStartEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final NamespacedKey spellKey;
    private final Duration duration;

    public SpellCooldownStartEvent(Player player, NamespacedKey spellKey, Duration duration) {
        super(false);
        this.player = player;
        this.spellKey = spellKey;
        this.duration = duration;
    }

    public Player getPlayer() {
        return player;
    }

    public NamespacedKey getSpellKey() {
        return spellKey;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
