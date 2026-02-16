package me.horiciastko.bedwars.logic.hooks;

import org.bukkit.entity.Player;

public class PAPIHook {

    public static String setPlaceholders(Player player, String text) {
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
    }

    public static void registerExpansion(me.horiciastko.bedwars.BedWars plugin) {
        new me.horiciastko.bedwars.logic.PAPIExpansion(plugin).register();
    }
}
