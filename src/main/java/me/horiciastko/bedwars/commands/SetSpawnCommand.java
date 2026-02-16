package me.horiciastko.bedwars.commands;

import lombok.RequiredArgsConstructor;
import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SetSpawnCommand implements SubCommand {

    private final BedWars plugin;

    @Override
    public String getName() {
        return "setSpawn";
    }

    @Override
    public String getDescription() {
        return "Set the spawn for a team";
    }

    @Override
    public String getSyntax() {
        return "/bw setSpawn <team>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!player.hasPermission("bedwars.setup")) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-no-permission"));
            return;
        }

        Arena arena = plugin.getArenaManager().getEditArena(player);
        if (arena == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "setup-not-editing"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "spawn-usage"));
            return;
        }

        String teamName = args[1];

        Team team = arena.getTeams().stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);

        if (team == null) {
            player.sendMessage(
                    plugin.getLanguageManager().getMessage(player.getUniqueId(), "spawn-team-not-found").replace("%team%", teamName));
            return;
        }

        team.setSpawnLocation(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        plugin.getVisualizationManager().showHolograms(player, arena);
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "spawn-set").replace("%team%", team.getDisplayName()));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (!player.hasPermission("bedwars.setup"))
            return new ArrayList<>();
        if (args.length == 2 && plugin.getArenaManager().getEditArena(player) != null) {
            return plugin.getArenaManager().getEditArena(player).getTeams().stream()
                    .map(Team::getName)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
