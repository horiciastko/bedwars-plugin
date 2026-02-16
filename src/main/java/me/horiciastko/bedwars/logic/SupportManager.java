package me.horiciastko.bedwars.logic;

import me.horiciastko.bedwars.BedWars;

import me.horiciastko.bedwars.logic.hooks.PAPIHook;
import me.horiciastko.bedwars.logic.hooks.VaultHook;
import me.horiciastko.bedwars.models.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SupportManager {

    private final BedWars plugin;
    private boolean citizensEnabled = false;
    private boolean vaultEnabled = false;
    private boolean placeholderApiEnabled = false;
    private Object economy = null;

    public SupportManager(BedWars plugin) {
        this.plugin = plugin;
        loadSupports();
    }

    private void loadSupports() {
        if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            this.citizensEnabled = true;
            plugin.getLogger().info("Citizens found and hooked!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.economy = VaultHook.getEconomy();
            this.vaultEnabled = (this.economy != null);
            if (this.vaultEnabled) {
                plugin.getLogger().info("Vault found and economy hooked!");
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderApiEnabled = true;
            PAPIHook.registerExpansion(plugin);
            plugin.getLogger().info("PlaceholderAPI found and BedWars expansion registered!");
        }
    }

    public void createCitizensNPC(Player player, String type) {
        me.horiciastko.bedwars.npc.BedWarsNPC npc = plugin.getNpcManager().createStandaloneNPC(player.getLocation(),
                type);

        if (npc != null) {
            String npcMode = citizensEnabled ? "Citizens" : "Vanilla";
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "npc-spawned").replace("%mode%", npcMode));
            if (citizensEnabled) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "npc-citizens-save"));
            }
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "npc-spawn-failed"));
        }
    }

    public boolean isCitizensEnabled() {
        return citizensEnabled;
    }

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    public boolean isPlaceholderApiEnabled() {
        return placeholderApiEnabled;
    }

    public Object getEconomy() {
        return economy;
    }

    public String parsePlaceholders(Player player, String text) {
        if (!placeholderApiEnabled)
            return text;
        return PAPIHook.setPlaceholders(player, text);
    }

    public String getStatPlaceholder(Player player, String identifier) {
        PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());
        if (stats == null)
            return "0";

        switch (identifier.toLowerCase()) {
            case "wins":
                return String.valueOf(stats.getWins());
            case "kills":
                return String.valueOf(stats.getKills());
            case "deaths":
                return String.valueOf(stats.getDeaths());
            case "level":
                return String.valueOf(stats.getLevel());
            case "team":
                me.horiciastko.bedwars.models.Team team = plugin.getGameManager().getPlayerTeam(
                        plugin.getArenaManager().getPlayerArena(player),
                        player);
                return team != null ? team.getColor().name() : "None";
            default:
                return "";
        }
    }
}