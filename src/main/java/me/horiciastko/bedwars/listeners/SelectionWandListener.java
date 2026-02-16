package me.horiciastko.bedwars.listeners;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SelectionWandListener implements Listener {

    private final BedWars plugin;

    public SelectionWandListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) {
            return;
        }

        String teamName = me.horiciastko.bedwars.utils.ItemTagUtils.getTag(item, "bw_wand_team");
        String arenaName = me.horiciastko.bedwars.utils.ItemTagUtils.getTag(item, "bw_wand_arena");

        if (teamName == null || arenaName == null) {
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "wand-arena-not-exists")
                .replace("%arena%", arenaName));
            event.setCancelled(true);
            return;
        }

        Team team = arena.getTeams().stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);

        if (team == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "wand-team-not-exists")
                .replace("%team%", teamName)
                .replace("%arena%", arenaName));
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            team.setBasePos1(clickedBlock.getLocation());
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "wand-pos1-set")
                .replace("%color%", team.getColor().toString())
                .replace("%team%", team.getDisplayName())
                .replace("%location%", formatLocation(clickedBlock.getLocation())));
            plugin.getArenaManager().saveArena(arena);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            team.setBasePos2(clickedBlock.getLocation());
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "wand-pos2-set")
                .replace("%color%", team.getColor().toString())
                .replace("%team%", team.getDisplayName())
                .replace("%location%", formatLocation(clickedBlock.getLocation())));
            plugin.getArenaManager().saveArena(arena);
        }

        if (team.getBasePos1() != null && team.getBasePos2() != null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "wand-region-complete"));
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        if (!item.hasItemMeta()) {
            return;
        }

        if (me.horiciastko.bedwars.utils.ItemTagUtils.getTag(item, "bw_wand_team") != null) {
            event.getItemDrop().remove();
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage(event.getPlayer().getUniqueId(), "wand-removed"));
        }
    }

    private String formatLocation(org.bukkit.Location loc) {
        return String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
