package me.horiciastko.bedwars.commands;

import lombok.RequiredArgsConstructor;
import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@RequiredArgsConstructor
public class CreateTeamCommand implements SubCommand {

    private final BedWars plugin;

    @Override
    public String getName() {
        return "createTeam";
    }

    @Override
    public String getDescription() {
        return "Create a team for an arena";
    }

    @Override
    public String getSyntax() {
        return "/bw createTeam <name> <color>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!player.hasPermission("bedwars.setup")) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-no-permission"));
            return;
        }

        Arena arena = plugin.getArenaManager().getEditArena(player);
        if (arena == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "setup-not-editing"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "createteam-usage"));
            return;
        }

        String name = args[1];
        String colorStr = args[2].toUpperCase();
        ChatColor color;
        try {
            color = ChatColor.valueOf(colorStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "createteam-invalid-color").replace("%color%", colorStr));
            return;
        }

        if (arena.getTeams().stream().anyMatch(t -> t.getName().equalsIgnoreCase(name))) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "createteam-exists").replace("%team%", name));
            return;
        }

        Material mat = com.cryptomorin.xseries.XMaterial.matchXMaterial(color.name() + "_WOOL")
                .orElse(com.cryptomorin.xseries.XMaterial.WHITE_WOOL).parseMaterial();

        Team team = new Team(name, name, color, mat);
        arena.getTeams().add(team);
        plugin.getArenaManager().saveArena(arena);
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "createteam-created").replace("%team%", name).replace("%color%", color.name()));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (!player.hasPermission("bedwars.setup"))
            return new ArrayList<>();
        if (args.length == 3) {
            return Arrays.stream(ChatColor.values())
                    .filter(ChatColor::isColor)
                    .map(Enum::name)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
