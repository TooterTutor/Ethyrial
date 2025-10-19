package io.github.tootertutor.ethyrial.party;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Team {
    private final UUID id;
    private final String name;
    private UUID leader;
    private boolean friendlyFire;
    private final Set<UUID> members = ConcurrentHashMap.newKeySet(); // includes leader

    public Team(UUID id, String name, UUID leader, boolean friendlyFire, Collection<UUID> initialMembers) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.friendlyFire = friendlyFire;
        this.members.addAll(initialMembers);
        this.members.add(leader);
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public UUID leader() {
        return leader;
    }

    public void setLeader(UUID l) {
        this.leader = l;
    }

    public boolean friendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean v) {
        this.friendlyFire = v;
    }

    public Set<UUID> members() {
        return Collections.unmodifiableSet(members);
    }

    public boolean isMember(UUID p) {
        return members.contains(p);
    }

    public void addMember(UUID p) {
        members.add(p);
    }

    public void removeMember(UUID p) {
        members.remove(p);
    }
}
