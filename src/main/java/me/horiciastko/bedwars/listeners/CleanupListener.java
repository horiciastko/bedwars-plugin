package me.horiciastko.bedwars.listeners;

import me.horiciastko.bedwars.BedWars;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class CleanupListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BedWars.getInstance().getVisualizationManager().hideHolograms(event.getPlayer());
        BedWars.getInstance().getArenaManager().setEditArena(event.getPlayer(), null);
        BedWars.getInstance().getArenaManager().leaveArena(event.getPlayer());
        BedWars.getInstance().getStatsManager().unloadStats(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        BedWars.getInstance().getVisualizationManager().clearAll();
    }
}
