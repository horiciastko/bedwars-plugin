package me.horiciastko.bedwars.logic;

import me.horiciastko.bedwars.BedWars;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LevelsManager {

    private final BedWars plugin;
    private FileConfiguration levelsConfig;
    private File levelsFile;

    public LevelsManager(BedWars plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        levelsFile = new File(plugin.getDataFolder(), "levels.yml");
        
        if (!levelsFile.exists()) {
            plugin.saveResource("levels.yml", false);
        }
        
        levelsConfig = YamlConfiguration.loadConfiguration(levelsFile);
    }

    public void reloadConfig() {
        levelsConfig = YamlConfiguration.loadConfiguration(levelsFile);
    }

    public int getXpForAction(String action) {
        return levelsConfig.getInt("xp-rewards." + action, 0);
    }

    public int getXpForLevel(int level) {
        boolean useFormula = levelsConfig.getBoolean("use-formula", true);
        
        if (useFormula) {
            return calculateFormulaXp(level);
        } else {
            return getManualLevelXp(level);
        }
    }

    private int calculateFormulaXp(int level) {
        if (level <= 1) return 0;
        
        double base = levelsConfig.getDouble("formula.base", 100.0);
        double exponent = levelsConfig.getDouble("formula.exponent", 2.0);
        double multiplier = levelsConfig.getDouble("formula.multiplier", 1.0);
        

        double xp = base * Math.pow(level - 1, exponent) + (base * multiplier * (level - 1));
        return (int) Math.round(xp);
    }

    private int getManualLevelXp(int level) {
        if (level <= 1) return 0;
        
        ConfigurationSection manualLevels = levelsConfig.getConfigurationSection("manual-levels");
        if (manualLevels == null) {
            return (level - 1) * 100;
        }

        int closestLevel = 1;
        for (String key : manualLevels.getKeys(false)) {
            try {
                int configLevel = Integer.parseInt(key);
                if (configLevel <= level && configLevel > closestLevel) {
                    closestLevel = configLevel;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (manualLevels.contains(String.valueOf(level))) {
            return manualLevels.getInt(String.valueOf(level));
        }

        double levelMultiplier = levelsConfig.getDouble("formula.level-multiplier", 1.10);
        int baseXp = manualLevels.getInt(String.valueOf(closestLevel));
        
        for (int i = closestLevel; i < level; i++) {
            baseXp = (int) (baseXp * levelMultiplier);
        }
        
        return baseXp;
    }

    public String getRankForLevel(int level) {
        ConfigurationSection ranks = levelsConfig.getConfigurationSection("ranks");
        if (ranks == null) {
            return "§7Beginner";
        }

        String currentRank = "§7Beginner";
        int highestLevel = 0;

        for (String key : ranks.getKeys(false)) {
            try {
                int rankLevel = Integer.parseInt(key);
                if (level >= rankLevel && rankLevel > highestLevel) {
                    highestLevel = rankLevel;
                    currentRank = ranks.getString(key);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return org.bukkit.ChatColor.translateAlternateColorCodes('&', currentRank);
    }

    public FileConfiguration getConfig() {
        return levelsConfig;
    }
}
