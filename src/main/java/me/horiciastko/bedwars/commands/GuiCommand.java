package me.horiciastko.bedwars.commands;

import me.horiciastko.bedwars.gui.ArenaSelectorGUI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GuiCommand implements SubCommand {

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public String getDescription() {
        return "Opens the arena selector GUI";
    }

    @Override
    public String getSyntax() {
        return "/bw gui [group]";
    }

    @Override
    public void perform(Player player, String[] args) {
        new ArenaSelectorGUI().open(player);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }
}
