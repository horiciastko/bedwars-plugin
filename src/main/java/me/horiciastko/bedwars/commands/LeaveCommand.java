package me.horiciastko.bedwars.commands;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LeaveCommand implements SubCommand {
    private final BedWars plugin;

    public LeaveCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Leaves the current arena";
    }

    @Override
    public String getSyntax() {
        return "/bw leave";
    }

    @Override
    public void perform(Player player, String[] args) {
        Arena arena = plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "leave-not-in-arena"));
            return;
        }

        plugin.getArenaManager().leaveArena(player);
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "leave-success"));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }
}
