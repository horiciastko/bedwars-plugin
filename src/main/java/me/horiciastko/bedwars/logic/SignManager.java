package me.horiciastko.bedwars.logic;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

@SuppressWarnings("deprecation")
public class SignManager {

    private final BedWars plugin;

    public SignManager(BedWars plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Arena arena : plugin.getArenaManager().getArenas()) {
                updateSigns(arena);
            }
        }, 20L, 100L);
    }

    public void updateSigns(Arena arena) {
        arena.getJoinSigns().removeIf(loc -> {
            Block block = loc.getBlock();
            if (!(block.getState() instanceof org.bukkit.block.Sign)) {
                return true;
            }
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();

            if (!arena.isEnabled()) {
                sign.setLine(0, "§8[§bBedWars§8]");
                sign.setLine(1, "§a§l" + arena.getName());
                sign.setLine(2, "§8DISABLED");
                sign.setLine(3, "§8- / -");
                sign.update();
                return false;
            }

            sign.setLine(0, "§8[§bBedWars§8]");
            sign.setLine(1, "§a§l" + arena.getName());
            if (arena.getGroup().equalsIgnoreCase("default")) {
                sign.setLine(2, getStateColor(arena.getState()) + arena.getState().name());
            } else {
                sign.setLine(2, "§7" + arena.getGroup());
                sign.setLine(3,
                        getStateColor(arena.getState()) + arena.getPlayers().size() + "/" + arena.getMaxPlayers());
                sign.update();
                return false;
            }
            sign.setLine(3, "§8" + arena.getPlayers().size() + " / " + arena.getMaxPlayers());
            sign.update();
            return false;
        });
    }

    private String getStateColor(Arena.GameState state) {
        switch (state) {
            case WAITING:
                return "§a";
            case STARTING:
                return "§e";
            case IN_GAME:
                return "§c";
            case ENDING:
                return "§6";
            default:
                return "§7";
        }
    }
}
