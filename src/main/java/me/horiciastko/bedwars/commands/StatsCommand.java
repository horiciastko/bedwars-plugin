package me.horiciastko.bedwars.commands;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StatsCommand implements SubCommand {

    public StatsCommand(me.horiciastko.bedwars.BedWars plugin) {
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "View your BedWars stats";
    }

    @Override
    public String getSyntax() {
        return "/bw stats";
    }

    @Override
    public void perform(Player player, String[] args) {
        Player target = player;
        if (args.length > 1) {
            Player t = org.bukkit.Bukkit.getPlayer(args[1]);
            if (t != null)
                target = t;
        }
        new me.horiciastko.bedwars.gui.StatsGUI(target).open(player);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }
}
