package io.github.tootertutor.ethyrial.party;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.database.TeamDAO;

public final class TeamManager {
    private final Ethyrial plugin;
    private final TeamDAO dao;

    // teamId -> Team
    private final ConcurrentMap<UUID, Team> teams = new ConcurrentHashMap<>();
    // player -> teamId
    private final ConcurrentMap<UUID, UUID> membership = new ConcurrentHashMap<>();
    // token -> invite
    private final ConcurrentMap<UUID, TeamInvite> invites = new ConcurrentHashMap<>();

    private final TeamEffects effects = new TeamEffects();

    public TeamManager(Ethyrial plugin, TeamDAO dao) {
        this.plugin = plugin;
        this.dao = dao;
    }

    public CompletableFuture<Void> init() {
        return dao.init().thenRun(() -> {
            // optional: warm cache lazily on demand instead of bulk load
            plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,
                    () -> dao.deleteExpiredInvites(), 20L * 60, 20L * 60);
        });
    }

    // --- Membership lookups ---
    public Optional<Team> getTeam(UUID playerId) {
        UUID teamId = membership.get(playerId);
        if (teamId == null)
            return Optional.empty();
        Team t = teams.get(teamId);
        if (t != null)
            return Optional.of(t);
        // lazy load
        return dao.loadTeam(teamId).join().map(loaded -> {
            teams.putIfAbsent(teamId, loaded);
            return loaded;
        });
    }

    public boolean areTeammates(UUID a, UUID b) {
        UUID ta = membership.get(a);
        return ta != null && ta.equals(membership.get(b));
    }

    public List<Player> getNearbyTeammates(Player source, double radius) {
        Optional<Team> teamOpt = getTeam(source.getUniqueId());
        if (teamOpt.isEmpty())
            return List.of();
        Set<UUID> ids = teamOpt.get().members();
        World w = source.getWorld();
        Location l = source.getLocation();
        return ids.stream()
                .map(plugin.getServer()::getPlayer)
                .filter(Objects::nonNull)
                .filter(p -> p.getWorld().equals(w) && p.getLocation().distanceSquared(l) <= radius * radius)
                .toList();
    }

    // --- Creation / disband ---
    public CompletableFuture<Team> createTeam(UUID leader, String name) {
        UUID id = UUID.randomUUID();
        Team t = new Team(id, name, leader, false, List.of(leader));
        return dao.insertTeam(t).thenApply(v -> {
            teams.put(id, t);
            membership.put(leader, id);
            return t;
        });
    }

    public CompletableFuture<Void> disbandTeam(UUID leader) {
        Optional<Team> t = getTeam(leader);
        if (t.isEmpty() || !t.get().leader().equals(leader))
            return CompletableFuture.completedFuture(null);
        UUID id = t.get().id();
        t.get().members().forEach(membership::remove);
        teams.remove(id);
        effects.clearTeam(id);
        return dao.deleteTeam(id);
    }

    // --- Invites ---
    public CompletableFuture<TeamInvite> invite(UUID inviter, UUID invitee) {
        Optional<Team> tOpt = getTeam(inviter);
        if (tOpt.isEmpty())
            throw new IllegalStateException("Inviter has no team");
        Team t = tOpt.get();
        UUID token = UUID.randomUUID();
        long now = System.currentTimeMillis();
        long expires = now + 5 * 60_000L; // 5 minutes
        TeamInvite inv = new TeamInvite(token, t.id(), inviter, invitee, now, expires);
        invites.put(token, inv);
        return dao.insertInvite(inv).thenApply(v -> inv);

    }

    public CompletableFuture<Boolean> accept(UUID invitee, UUID token) {
        TeamInvite inv = invites.get(token);
        if (inv == null)
            inv = dao.getInvite(token).join().orElse(null);
        // accept(...)
        if (inv == null || inv.isExpired() || !inv.getInvitee().equals(invitee)) {
            return CompletableFuture.completedFuture(false);
        }

        // leave previous team if any
        getTeam(invitee).ifPresent(prev -> leave(invitee));

        // join
        Team team = teams.computeIfAbsent(inv.getTeamId(), id -> dao.loadTeam(id).join().orElse(null));
        if (team == null)
            return CompletableFuture.completedFuture(false);
        team.addMember(invitee);
        membership.put(invitee, team.id());

        dao.deleteInvite(token);
        invites.remove(token);
        return dao.upsertMember(team.id(), invitee, TeamRole.MEMBER).thenApply(v -> true);
    }

    public CompletableFuture<Void> decline(UUID invitee, UUID token) {
        TeamInvite inv = invites.get(token);
        if (inv == null)
            inv = dao.getInvite(token).join().orElse(null);
        if (inv == null || !inv.getInvitee().equals(invitee))
            return CompletableFuture.completedFuture(null);
        invites.remove(token);
        return dao.deleteInvite(token);
    }

    // --- Leave / kick / promote ---
    public CompletableFuture<Void> leave(UUID player) {
        Optional<Team> tOpt = getTeam(player);
        if (tOpt.isEmpty())
            return CompletableFuture.completedFuture(null);
        Team t = tOpt.get();

        if (t.leader().equals(player)) {
            // auto-promote another member or disband
            UUID newLeader = t.members().stream().filter(u -> !u.equals(player)).findFirst().orElse(null);
            if (newLeader == null)
                return disbandTeam(player);
            t.setLeader(newLeader);
            dao.setLeader(t.id(), newLeader);
        }

        t.removeMember(player);
        membership.remove(player);
        effects.clearPlayerFromTeam(t.id(), player);
        return dao.removeMember(t.id(), player);
    }

    public CompletableFuture<Boolean> kick(UUID leader, UUID target) {
        Optional<Team> tOpt = getTeam(leader);
        if (tOpt.isEmpty() || !tOpt.get().leader().equals(leader))
            return CompletableFuture.completedFuture(false);
        Team t = tOpt.get();
        if (!t.isMember(target) || t.leader().equals(target))
            return CompletableFuture.completedFuture(false);
        t.removeMember(target);
        membership.remove(target);
        effects.clearPlayerFromTeam(t.id(), target);
        return dao.removeMember(t.id(), target).thenApply(v -> true);
    }

    public CompletableFuture<Boolean> promote(UUID leader, UUID target) {
        Optional<Team> tOpt = getTeam(leader);
        if (tOpt.isEmpty() || !tOpt.get().leader().equals(leader))
            return CompletableFuture.completedFuture(false);
        Team t = tOpt.get();
        if (!t.isMember(target))
            return CompletableFuture.completedFuture(false);
        t.setLeader(target);
        return dao.setLeader(t.id(), target).thenApply(v -> true);
    }

    public CompletableFuture<Boolean> toggleFriendlyFire(UUID leader) {
        Optional<Team> tOpt = getTeam(leader);
        if (tOpt.isEmpty() || !tOpt.get().leader().equals(leader))
            return CompletableFuture.completedFuture(false);
        Team t = tOpt.get();
        t.setFriendlyFire(!t.friendlyFire());
        return dao.setFriendlyFire(t.id(), t.friendlyFire()).thenApply(v -> t.friendlyFire());
    }

    // --- Spell helpers ---
    public void healTeamPercent(Player source, double fraction, double maxAmount) {
        List<Player> mates = getNearbyTeammates(source, 40.0); // sensible default AoE
        double amount = Math.min(maxAmount,
                source.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() * fraction);
        for (Player p : mates) {
            double maxHp = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            double newHp = Math.min(maxHp, p.getHealth() + amount);
            p.setHealth(newHp);
        }
    }

    public TeamEffects effects() {
        return effects;
    }
}
