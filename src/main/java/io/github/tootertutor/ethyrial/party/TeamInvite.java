package io.github.tootertutor.ethyrial.party;

import java.util.UUID;

public final class TeamInvite {
    private final UUID token;
    private final UUID teamId;
    private final UUID inviter;
    private final UUID invitee;
    private final long createdAt; // epoch millis
    private final long expiresAt; // epoch millis

    public TeamInvite(UUID token, UUID teamId, UUID inviter, UUID invitee, long createdAt, long expiresAt) {
        this.token = token;
        this.teamId = teamId;
        this.inviter = inviter;
        this.invitee = invitee;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID getToken() {
        return token;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getInviter() {
        return inviter;
    }

    public UUID getInvitee() {
        return invitee;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
