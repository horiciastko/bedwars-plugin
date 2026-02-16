package me.horiciastko.bedwars.commands;

import lombok.RequiredArgsConstructor;
import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RejoinCommand implements SubCommand {

    private final BedWars plugin;

    @Override
    public String getName() {
        return "rejoin";
    }

    @Override
    public String getDescription() {
        return "Rejoin your last arena";
    }

    @Override
    public String getSyntax() {
        return "/rejoin";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!player.hasPermission("bedwars.command.rejoin")) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-no-permission"));
            return;
        }

        String lastArenaName = plugin.getArenaManager().getLastArena(player.getUniqueId());
        if (lastArenaName == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "rejoin-no-arena"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(lastArenaName);
        if (arena == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "rejoin-arena-not-exists"));
            return;
        }

        plugin.getArenaManager().joinArena(player, arena);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }
}
