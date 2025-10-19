package io.github.tootertutor.ethyrial.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.tootertutor.ethyrial.party.Team;
import io.github.tootertutor.ethyrial.party.TeamInvite;
import io.github.tootertutor.ethyrial.party.TeamRole;

public final class TeamDAO {
    private final SQLiteDatabaseManager db;

    public TeamDAO(SQLiteDatabaseManager db) {
        this.db = db;
    }

    /** Create tables / indexes (idempotent). */
    public CompletableFuture<Void> init() {
        return db.runAsyncQuery(() -> {
            String sqlTeams = """
                    CREATE TABLE IF NOT EXISTS teams (
                      id TEXT PRIMARY KEY,
                      name TEXT NOT NULL,
                      leader_uuid TEXT NOT NULL,
                      friendly_fire INTEGER NOT NULL DEFAULT 0,
                      created_at INTEGER NOT NULL
                    );
                    """;

            String sqlMembers = """
                    CREATE TABLE IF NOT EXISTS team_members (
                      team_id TEXT NOT NULL,
                      player_uuid TEXT NOT NULL,
                      role TEXT NOT NULL,
                      joined_at INTEGER NOT NULL,
                      PRIMARY KEY (team_id, player_uuid),
                      FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
                    );
                    """;

            String sqlInvites = """
                    CREATE TABLE IF NOT EXISTS team_invites (
                      token TEXT PRIMARY KEY,
                      team_id TEXT NOT NULL,
                      inviter_uuid TEXT NOT NULL,
                      invitee_uuid TEXT NOT NULL,
                      created_at INTEGER NOT NULL,
                      expires_at INTEGER NOT NULL,
                      FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
                    );
                    """;

            String idxMembers = "CREATE INDEX IF NOT EXISTS idx_team_members_player ON team_members(player_uuid);";
            String idxInvites = "CREATE INDEX IF NOT EXISTS idx_team_invites_invitee ON team_invites(invitee_uuid);";

            try (Connection c = db.getConnection(); Statement st = c.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON;");
                st.execute(sqlTeams);
                st.execute(sqlMembers);
                st.execute(sqlInvites);
                st.execute(idxMembers);
                st.execute(idxInvites);
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.init()", e);
            }
        });
    }

    public CompletableFuture<Void> insertTeam(Team team) {
        return db.runAsyncQuery(() -> {
            String insertTeam = "INSERT INTO teams (id, name, leader_uuid, friendly_fire, created_at) VALUES (?, ?, ?, ?, ?)";
            String insertMember = "INSERT INTO team_members (team_id, player_uuid, role, joined_at) VALUES (?, ?, ?, ?)";

            try (Connection c = db.getConnection()) {
                c.setAutoCommit(false);
                try (PreparedStatement ps = c.prepareStatement(insertTeam)) {
                    ps.setString(1, team.id().toString());
                    ps.setString(2, team.name());
                    ps.setString(3, team.leader().toString());
                    ps.setInt(4, team.friendlyFire() ? 1 : 0);
                    ps.setLong(5, System.currentTimeMillis());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(insertMember)) {
                    long now = System.currentTimeMillis();
                    for (UUID m : team.members()) {
                        ps.setString(1, team.id().toString());
                        ps.setString(2, m.toString());
                        ps.setString(3, m.equals(team.leader()) ? "LEADER" : "MEMBER");
                        ps.setLong(4, now);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.insertTeam()", e);
            }
        });
    }

    public CompletableFuture<Optional<Team>> loadTeam(UUID teamId) {
        return db.runAsyncQuery(() -> {
            String qTeam = "SELECT name, leader_uuid, friendly_fire FROM teams WHERE id = ?";
            String qMembers = "SELECT player_uuid FROM team_members WHERE team_id = ?";
            try (Connection c = db.getConnection()) {
                String name;
                UUID leader;
                boolean ff;
                try (PreparedStatement ps = c.prepareStatement(qTeam)) {
                    ps.setString(1, teamId.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            return Optional.empty();
                        name = rs.getString(1);
                        leader = UUID.fromString(rs.getString(2));
                        ff = rs.getInt(3) != 0;
                    }
                }
                List<UUID> members = new ArrayList<>();
                try (PreparedStatement ps = c.prepareStatement(qMembers)) {
                    ps.setString(1, teamId.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next())
                            members.add(UUID.fromString(rs.getString(1)));
                    }
                }
                return Optional.of(new Team(teamId, name, leader, ff, members));
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.loadTeam()", e);
            }
        });
    }

    public CompletableFuture<Optional<UUID>> findTeamIdByMember(UUID player) {
        return db.runAsyncQuery(() -> {
            String q = "SELECT team_id FROM team_members WHERE player_uuid = ? LIMIT 1";
            try (Connection c = db.getConnection();
                    PreparedStatement ps = c.prepareStatement(q)) {
                ps.setString(1, player.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(UUID.fromString(rs.getString(1))) : Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.findTeamIdByMember()", e);
            }
        });
    }

    public CompletableFuture<Void> upsertMember(UUID teamId, UUID player, TeamRole role) {
        return db.runAsyncQuery(() -> {
            // SQLite UPSERT
            String sql = """
                      INSERT INTO team_members (team_id, player_uuid, role, joined_at)
                      VALUES (?, ?, ?, ?)
                      ON CONFLICT(team_id, player_uuid) DO UPDATE SET role = excluded.role
                    """;
            try (Connection c = db.getConnection();
                    PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, teamId.toString());
                ps.setString(2, player.toString());
                ps.setString(3, role.name());
                ps.setLong(4, System.currentTimeMillis());
                ps.executeUpdate();
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.upsertMember()", e);
            }
        });
    }

    public CompletableFuture<Void> removeMember(UUID teamId, UUID player) {
        return db.runAsyncQuery(() -> {
            String sql = "DELETE FROM team_members WHERE team_id = ? AND player_uuid = ?";
            try (Connection c = db.getConnection();
                    PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, teamId.toString());
                ps.setString(2, player.toString());
                ps.executeUpdate();
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.removeMember()", e);
            }
        });
    }

    public CompletableFuture<Void> deleteTeam(UUID teamId) {
        return db.runAsyncQuery(() -> {
            String delMembers = "DELETE FROM team_members WHERE team_id = ?";
            String delTeam = "DELETE FROM teams WHERE id = ?";
            try (Connection c = db.getConnection()) {
                c.setAutoCommit(false);
                try (PreparedStatement ps = c.prepareStatement(delMembers)) {
                    ps.setString(1, teamId.toString());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(delTeam)) {
                    ps.setString(1, teamId.toString());
                    ps.executeUpdate();
                }
                c.commit();
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.deleteTeam()", e);
            }
        });
    }

    public CompletableFuture<Void> setLeader(UUID teamId, UUID leader) {
        return db.runAsyncQuery(() -> {
            String sql = "UPDATE teams SET leader_uuid = ? WHERE id = ?";
            try (Connection c = db.getConnection();
                    PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, leader.toString());
                ps.setString(2, teamId.toString());
                ps.executeUpdate();
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.setLeader()", e);
            }
        });
    }

    public CompletableFuture<Void> setFriendlyFire(UUID teamId, boolean ff) {
        return db.runAsyncQuery(() -> {
            String sql = "UPDATE teams SET friendly_fire = ? WHERE id = ?";
            try (Connection c = db.getConnection();
                    PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, ff ? 1 : 0);
                ps.setString(2, teamId.toString());
                ps.executeUpdate();
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.setFriendlyFire()", e);
            }
        });
    }

    // ---------- Invites ----------

    public CompletableFuture<Void> insertInvite(TeamInvite inv) {
        return db.runAsyncQuery(() -> {
            String sql = "INSERT INTO team_invites (token, team_id, inviter_uuid, invitee_uuid, created_at, expires_at) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection c = db.getConnection();
                    PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, inv.getToken().toString());
                ps.setString(2, inv.getTeamId().toString());
                ps.setString(3, inv.getInviter().toString());
                ps.setString(4, inv.getInvitee().toString());
                ps.setLong(5, inv.getCreatedAt());
                ps.setLong(6, inv.getExpiresAt());
                ps.executeUpdate();
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.insertInvite()", e);
            }
        });
    }

    public CompletableFuture<Optional<TeamInvite>> getInvite(UUID token) {
        return db.runAsyncQuery(() -> {
            String sql = "SELECT team_id, inviter_uuid, invitee_uuid, created_at, expires_at FROM team_invites WHERE token = ?";
            try (Connection c = db.getConnection();
                    PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, token.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        return Optional.empty();
                    UUID teamId = UUID.fromString(rs.getString(1));
                    UUID inviter = UUID.fromString(rs.getString(2));
                    UUID invitee = UUID.fromString(rs.getString(3));
                    long created = rs.getLong(4);
                    long expires = rs.getLong(5);
                    return Optional.of(new TeamInvite(token, teamId, inviter, invitee, created, expires));
                }
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.getInvite()", e);
            }
        });
    }

    public CompletableFuture<Void> deleteInvite(UUID token) {
        return db.runAsyncQuery(() -> {
            String sql = "DELETE FROM team_invites WHERE token = ?";
            try (Connection c = db.getConnection();
                    PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, token.toString());
                ps.executeUpdate();
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.deleteInvite()", e);
            }
        });
    }

    public CompletableFuture<Void> deleteExpiredInvites() {
        return db.runAsyncQuery(() -> {
            String sql = "DELETE FROM team_invites WHERE expires_at < ?";
            try (Connection c = db.getConnection();
                    PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, System.currentTimeMillis());
                ps.executeUpdate();
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("TeamDAO.deleteExpiredInvites()", e);
            }
        });
    }
}
