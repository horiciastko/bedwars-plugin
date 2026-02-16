package me.horiciastko.bedwars.commands;

import me.horiciastko.bedwars.gui.ShopGUI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ShopCommand implements SubCommand {

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public String getDescription() {
        return "Open the shop GUI";
    }

    @Override
    public String getSyntax() {
        return "/bw shop";
    }

    @Override
    public void perform(Player player, String[] args) {
        new ShopGUI().open(player);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }
}
