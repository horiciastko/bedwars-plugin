package me.horiciastko.bedwars.logic;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.PlayerStats;
import me.horiciastko.bedwars.models.Team;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIExpansion extends PlaceholderExpansion {

    private final BedWars plugin;

    public PAPIExpansion(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bw";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Horiciastko";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null)
            return "";

        PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());
        Arena arena = plugin.getArenaManager().getPlayerArena(player);

        switch (params.toLowerCase()) {
            case "wins":
                return stats != null ? String.valueOf(stats.getWins()) : "0";
            case "kills":
                return stats != null ? String.valueOf(stats.getKills()) : "0";
            case "final_kills":
                return stats != null ? String.valueOf(stats.getFinalKills()) : "0";
            case "deaths":
                return stats != null ? String.valueOf(stats.getDeaths()) : "0";
            case "beds_broken":
                return stats != null ? String.valueOf(stats.getBedsBroken()) : "0";
            case "level":
                return stats != null ? String.valueOf(stats.getLevel()) : "1";
            case "coins":
                return stats != null ? String.valueOf(stats.getCoins()) : "0";
            case "arena":
                return arena != null ? arena.getName() : "None";
            case "team":
                if (arena != null) {
                    Team team = plugin.getGameManager().getPlayerTeam(arena, player);
                    return team != null ? team.getName() : "None";
                }
                return "None";
            case "team_color":
                if (arena != null) {
                    Team team = plugin.getGameManager().getPlayerTeam(arena, player);
                    return team != null ? team.getColor().toString() : "§7";
                }
                return "§7";
        }

        return null;
    }
}
