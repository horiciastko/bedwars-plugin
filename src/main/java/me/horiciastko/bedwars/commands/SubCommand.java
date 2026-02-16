package me.horiciastko.bedwars.commands;

import org.bukkit.entity.Player;

import java.util.List;

public interface SubCommand {
    String getName();

    String getDescription();

    String getSyntax();

    void perform(Player player, String[] args);

    List<String> tabComplete(Player player, String[] args);
}
