package me.horiciastko.bedwars.logic;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CompassTracker extends BukkitRunnable {

    private final BedWars plugin;

    public CompassTracker(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player);
            
            if (arena == null || arena.getState() != Arena.GameState.IN_GAME) {
                continue;
            }

            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            
            boolean hasCompass = isTrackerCompass(mainHand) || isTrackerCompass(offHand);
            
            if (!hasCompass) {
                continue;
            }

            Team playerTeam = plugin.getGameManager().getPlayerTeam(arena, player);
            if (playerTeam == null) {
                continue;
            }

            Player nearestEnemy = findNearestEnemy(player, arena, playerTeam);
            
            if (nearestEnemy != null) {
                player.setCompassTarget(nearestEnemy.getLocation());
            } else {
                Location nearestSpawn = findNearestEnemySpawn(player, arena, playerTeam);
                if (nearestSpawn != null) {
                    player.setCompassTarget(nearestSpawn);
                }
            }
        }
    }

    private boolean isTrackerCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) {
            return false;
        }
        
        if (!item.hasItemMeta()) {
            return false;
        }
        
        String special = me.horiciastko.bedwars.utils.ItemTagUtils.getTag(item, "special_item");
        return "tracker".equals(special);
    }

    private Player findNearestEnemy(Player player, Arena arena, Team playerTeam) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Team team : arena.getTeams()) {
            if (team.getName().equals(playerTeam.getName())) {
                continue;
            }

            for (Player member : team.getMembers()) {
                if (member.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
                    continue;
                }

                if (member.getLocation().getWorld() == null || 
                    player.getLocation().getWorld() == null ||
                    !member.getLocation().getWorld().equals(player.getLocation().getWorld())) {
                    continue;
                }

                double distance = player.getLocation().distance(member.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = member;
                }
            }
        }

        return nearest;
    }

    private Location findNearestEnemySpawn(Player player, Arena arena, Team playerTeam) {
        Location nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Team team : arena.getTeams()) {
            if (team.getName().equals(playerTeam.getName())) {
                continue;
            }

            if (team.isEliminated()) {
                continue;
            }

            Location spawn = team.getSpawnLocation();
            if (spawn == null || spawn.getWorld() == null) {
                continue;
            }

            if (player.getLocation().getWorld() == null ||
                !spawn.getWorld().equals(player.getLocation().getWorld())) {
                continue;
            }

            double distance = player.getLocation().distance(spawn);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = spawn;
            }
        }

        return nearest;
    }
}
