package me.horiciastko.bedwars.listeners;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign))
            return;

        Player player = event.getPlayer();

        for (Arena arena : BedWars.getInstance().getArenaManager().getArenas()) {
            if (arena.getJoinSigns().contains(block.getLocation())) {
                joinArena(player, arena);
                return;
            }
        }
    }

    private void joinArena(Player player, Arena arena) {
        BedWars.getInstance().getArenaManager().joinArena(player, arena);
    }
}
