package io.github.tootertutor.ethyrial.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterCommand;
import io.github.tootertutor.ethyrial.interfaces.Subcommand;
import io.github.tootertutor.ethyrial.party.Team;
import io.github.tootertutor.ethyrial.party.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class TeamCommand implements Subcommand, AutoRegisterCommand {
    private final Ethyrial plugin;
    private final TeamManager teams;
    private final String BASE_COMMAND = "/ethyrial party";

    public TeamCommand(Ethyrial plugin, TeamManager teams) {
        this.plugin = plugin;
        this.teams = teams;
    }

    public String getUsage() {
        return "/ethyrial party <create|invite|accept|decline|leave|disband|kick|promote|ff|info>";
    }

    @Override
    public String getName() {
        return "party";
    }

    @Override
    public String getDescription() {
        return "Create, manage and join parties.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("party");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("Players only.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            p.sendMessage(Component.text(getUsage(), NamedTextColor.GRAY));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (teams.getTeam(p.getUniqueId()).isPresent()) {
                    p.sendMessage(Component.text("You are already in a party.", NamedTextColor.RED));
                    return true;
                }
                String name = (args.length >= 2) ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
                        : (p.getName() + "'s Party");
                teams.createTeam(p.getUniqueId(), name)
                        .thenAccept(team -> p.sendMessage(Component.text("Created party: ", NamedTextColor.GRAY)
                                .append(Component.text(team.name(), NamedTextColor.GOLD))));
            }
            case "invite" -> {
                if (args.length < 2) {
                    p.sendMessage(Component.text("Usage: " + BASE_COMMAND + " invite <player>", NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    p.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                    return true;
                }
                Optional<Team> t = teams.getTeam(p.getUniqueId());
                if (t.isEmpty() || !t.get().leader().equals(p.getUniqueId())) {
                    p.sendMessage(Component.text("Only the party leader can invite.", NamedTextColor.RED));
                    return true;
                }
                teams.invite(p.getUniqueId(), target.getUniqueId()).thenAccept(inv -> {
                    sendInviteMessage(target, inv.getToken(), t.get().name(), p.getName());
                    p.sendMessage(Component.text("Invite sent to " + target.getName(), NamedTextColor.GREEN));
                });
            }
            case "accept" -> {
                if (args.length < 2) {
                    p.sendMessage(Component.text("Usage: " + BASE_COMMAND + " accept <token>", NamedTextColor.RED));
                    return true;
                }
                try {
                    UUID token = UUID.fromString(args[1]);
                    teams.accept(p.getUniqueId(), token).thenAccept(ok -> p.sendMessage(ok
                            ? Component.text("You joined the party!", NamedTextColor.GREEN)
                            : Component.text("Invite invalid or expired.", NamedTextColor.RED)));
                } catch (IllegalArgumentException e) {
                    p.sendMessage(Component.text("Invalid token.", NamedTextColor.RED));
                }
            }
            case "decline" -> {
                if (args.length < 2) {
                    p.sendMessage(Component.text("Usage: " + BASE_COMMAND + " decline <token>", NamedTextColor.RED));
                    return true;
                }
                try {
                    UUID token = UUID.fromString(args[1]);
                    teams.decline(p.getUniqueId(), token)
                            .thenRun(() -> p.sendMessage(Component.text("Invite declined.", NamedTextColor.YELLOW)));
                } catch (IllegalArgumentException e) {
                    p.sendMessage(Component.text("Invalid token.", NamedTextColor.RED));
                }
            }
            case "leave" -> teams.leave(p.getUniqueId())
                    .thenRun(() -> p.sendMessage(Component.text("You left your party.", NamedTextColor.YELLOW)));
            case "disband" -> teams.disbandTeam(p.getUniqueId())
                    .thenRun(() -> p.sendMessage(Component.text("Party disbanded.", NamedTextColor.YELLOW)));
            case "kick" -> {
                if (args.length < 2) {
                    p.sendMessage(Component.text("Usage: " + BASE_COMMAND + " kick <player>", NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    p.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                    return true;
                }
                teams.kick(p.getUniqueId(), target.getUniqueId()).thenAccept(
                        ok -> p.sendMessage(ok ? Component.text("Kicked " + target.getName(), NamedTextColor.YELLOW)
                                : Component.text("Failed to kick.", NamedTextColor.RED)));
            }
            case "promote" -> {
                if (args.length < 2) {
                    p.sendMessage(Component.text("Usage: " + BASE_COMMAND + " promote <player>", NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    p.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                    return true;
                }
                teams.promote(p.getUniqueId(), target.getUniqueId()).thenAccept(
                        ok -> p.sendMessage(ok ? Component.text("Promoted " + target.getName(), NamedTextColor.YELLOW)
                                : Component.text("Promotion failed.", NamedTextColor.RED)));
            }
            case "ff" -> teams.toggleFriendlyFire(p.getUniqueId()).thenAccept(enabled -> {
                if (enabled != null)
                    p.sendMessage(Component.text("Friendly fire: " + (enabled ? "ON" : "OFF"),
                            enabled ? NamedTextColor.RED : NamedTextColor.GREEN));
            });
            case "info" -> {
                Optional<Team> t = teams.getTeam(p.getUniqueId());
                if (t.isEmpty()) {
                    p.sendMessage(Component.text("You are not in a party.", NamedTextColor.GRAY));
                    return true;
                }
                Team tm = t.get();
                Component list = Component.text(
                        "Party " + tm.name() + " [" + (tm.friendlyFire() ? "FF:ON" : "FF:OFF") + "]",
                        NamedTextColor.GOLD);
                p.sendMessage(list);
                tm.members().forEach(uuid -> {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                    boolean leader = tm.leader().equals(uuid);
                    String name = op.getName() != null ? op.getName() : op.getUniqueId().toString().substring(0, 8);
                    p.sendMessage(Component.text(" - " + (leader ? "★ " : "") + name,
                            leader ? NamedTextColor.AQUA : NamedTextColor.GRAY));

                });
            }
            default -> p.sendMessage(Component.text(getUsage(), NamedTextColor.GRAY));

        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("create", "invite", "accept", "decline", "leave", "disband", "kick", "promote", "ff", "info")
                    .stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("invite") || sub.equals("kick") || sub.equals("promote")) {
                String prefix = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }

    private void sendInviteMessage(Player invitee, UUID token, String teamName, String inviterName) {

        Component base = Component.text(inviterName, NamedTextColor.AQUA)
                .append(Component.text(" invited you to join party ", NamedTextColor.GRAY))
                .append(Component.text(teamName, NamedTextColor.GOLD));

        Component accept = Component.text("[ACCEPT]", NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Click to join " + teamName, NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.runCommand(BASE_COMMAND + " accept " + token));

        Component decline = Component.text("[DECLINE]", NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Click to decline", NamedTextColor.RED)))
                .clickEvent(ClickEvent.runCommand(BASE_COMMAND + " decline " + token));

        invitee.sendMessage(base);
        invitee.sendMessage(accept.append(Component.space()).append(decline));
    }

}
