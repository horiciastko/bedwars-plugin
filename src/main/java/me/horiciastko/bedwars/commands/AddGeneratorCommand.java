package me.horiciastko.bedwars.commands;

import lombok.RequiredArgsConstructor;
import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class AddGeneratorCommand implements SubCommand {

    private final BedWars plugin;

    @Override
    public String getName() {
        return "addGenerator";
    }

    @Override
    public String getDescription() {
        return "Add a generator (Iron/Gold/Emerald/Diamond)";
    }

    @Override
    public String getSyntax() {
        return "/bw addGenerator <type>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!player.hasPermission("bedwars.setup")) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "generator-no-permission"));
            return;
        }

        Arena arena = plugin.getArenaManager().getEditArena(player);
        if (arena == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "generator-not-editing"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "generator-usage"));
            return;
        }

        String type = args[1].toUpperCase();
        Location loc = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);

        switch (type) {
            case "DIAMOND":
                arena.getDiamondGenerators().add(loc);
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "generator-diamond-added"));
                break;
            case "EMERALD":
                arena.getEmeraldGenerators().add(loc);
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "generator-emerald-added"));
                break;
            case "IRON":
            case "GOLD":
                me.horiciastko.bedwars.models.Team nearest = findNearestTeam(arena, loc);
                if (nearest != null) {
                    nearest.getGenerators().add(loc);
                    String msg = plugin.getLanguageManager().getMessage(player.getUniqueId(), "generator-team-added")
                        .replace("%type%", type)
                        .replace("%team%", nearest.getName());
                    player.sendMessage(msg);
                } else {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "generator-no-team-nearby"));
                }
                break;
            default:
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "generator-invalid-type"));
                return;
        }

        plugin.getArenaManager().saveArena(arena);
        plugin.getVisualizationManager().showHolograms(player, arena);
    }

    private me.horiciastko.bedwars.models.Team findNearestTeam(Arena arena, Location loc) {
        me.horiciastko.bedwars.models.Team best = null;
        double minDist = Double.MAX_VALUE;
        for (me.horiciastko.bedwars.models.Team t : arena.getTeams()) {
            if (t.getSpawnLocation() != null && t.getSpawnLocation().getWorld().equals(loc.getWorld())) {
                double d = t.getSpawnLocation().distance(loc);
                if (d < 20 && d < minDist) {
                    minDist = d;
                    best = t;
                }
            }
        }
        return best;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (!player.hasPermission("bedwars.setup"))
            return new ArrayList<>();
        if (args.length == 2) {
            return Arrays.asList("Iron", "Gold", "Diamond", "Emerald");
        }
        return new ArrayList<>();
    }
}
