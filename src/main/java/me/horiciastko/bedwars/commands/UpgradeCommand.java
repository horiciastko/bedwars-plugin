package me.horiciastko.bedwars.commands;

import me.horiciastko.bedwars.gui.UpgradeGUI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UpgradeCommand implements SubCommand {

    @Override
    public String getName() {
        return "upgrade";
    }

    @Override
    public String getDescription() {
        return "Open the upgrades GUI";
    }

    @Override
    public String getSyntax() {
        return "/bw upgrade";
    }

    @Override
    public void perform(Player player, String[] args) {
        new UpgradeGUI().open(player);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }
}
