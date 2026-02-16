package me.horiciastko.bedwars.listeners;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileListener implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Fireball) {
            Fireball fireball = (Fireball) event.getEntity();
            if (!(fireball.getShooter() instanceof Player))
                return;
            Player player = (Player) fireball.getShooter();

            Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
            if (arena == null || arena.getState() != Arena.GameState.IN_GAME)
                return;
        }
    }
}
