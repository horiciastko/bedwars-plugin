package me.horiciastko.bedwars.commands;

import lombok.RequiredArgsConstructor;
import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JoinCommand implements SubCommand {

    private final BedWars plugin;

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Join a BedWars arena or group";
    }

    @Override
    public String getSyntax() {
        return "/bw join [arena/group]";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            new me.horiciastko.bedwars.gui.JoinGUI().open(player);
            return;
        }

        String target = args[1];

        Arena arena = plugin.getArenaManager().getArena(target);
        if (arena != null) {
            plugin.getArenaManager().joinArena(player, arena);
            return;
        }

        List<Arena> groupArenas = plugin.getArenaManager().getArenas().stream()
                .filter(a -> a.isEnabled() || player.hasPermission("bedwars.admin"))
                .filter(a -> a.getGroup().equalsIgnoreCase(target))
                .filter(a -> a.getState() == Arena.GameState.WAITING || a.getState() == Arena.GameState.STARTING)
                .collect(Collectors.toList());

        if (!groupArenas.isEmpty()) {
            groupArenas.sort((a1, a2) -> Integer.compare(a2.getPlayers().size(), a1.getPlayers().size()));
            plugin.getArenaManager().joinArena(player, groupArenas.get(0));
            return;
        }

        if (target.equalsIgnoreCase("random")) {
            List<Arena> allAvailable = plugin.getArenaManager().getArenas().stream()
                    .filter(a -> a.isEnabled() || player.hasPermission("bedwars.admin"))
                    .filter(a -> a.getState() == Arena.GameState.WAITING || a.getState() == Arena.GameState.STARTING)
                    .collect(Collectors.toList());
            if (!allAvailable.isEmpty()) {
                allAvailable.sort((a1, a2) -> Integer.compare(a2.getPlayers().size(), a1.getPlayers().size()));
                plugin.getArenaManager().joinArena(player, allAvailable.get(0));
            } else {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "join-no-arenas"));
            }
            return;
        }

        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "join-not-found").replace("%target%", target));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("random");
            suggestions.addAll(plugin.getArenaManager().getArenas().stream()
                    .map(Arena::getName)
                    .collect(Collectors.toList()));
            suggestions.addAll(plugin.getArenaManager().getArenas().stream()
                    .map(Arena::getGroup)
                    .distinct()
                    .collect(Collectors.toList()));
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
