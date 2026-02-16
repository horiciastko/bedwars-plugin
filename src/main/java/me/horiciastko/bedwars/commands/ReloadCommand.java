package me.horiciastko.bedwars.commands;

import me.horiciastko.bedwars.BedWars;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements SubCommand {

    private final BedWars plugin;

    public ReloadCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads all configuration files";
    }

    @Override
    public String getSyntax() {
        return "/bw admin reload";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!player.hasPermission("bedwars.command.reload")) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-no-permission"));
            return;
        }

        plugin.getConfigManager().reloadAll();
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-reload-success"));
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
