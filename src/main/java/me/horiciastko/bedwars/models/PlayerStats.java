package me.horiciastko.bedwars.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.horiciastko.bedwars.BedWars;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStats {
    private UUID uuid;
    private int wins;
    private int kills;
    private int deaths;
    private int finalKills;
    private int bedsBroken;
    private int experience;

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
        this.wins = 0;
        this.kills = 0;
        this.deaths = 0;
        this.finalKills = 0;
        this.bedsBroken = 0;
        this.experience = 0;
    }

    public static int getXpForLevel(int level) {
        try {
            return BedWars.getInstance().getLevelsManager().getXpForLevel(level);
        } catch (Exception e) {
            return 500 * level * level + 500 * level;
        }
    }

    public int getLevel() {
        int level = 1;
        while (experience >= getXpForLevel(level)) {
            level++;
        }
        return Math.max(1, level - 1);
    }

    public double getProgressPercent() {
        int currentLevel = getLevel();
        int xpForCurrentLevel = getXpForLevel(currentLevel);
        int xpForNextLevel = getXpForLevel(currentLevel + 1);
        int currentLevelXp = experience - xpForCurrentLevel;
        int requiredXp = xpForNextLevel - xpForCurrentLevel;
        
        if (requiredXp <= 0 || currentLevelXp < 0) return 0;
        return Math.min(100.0, Math.max(0.0, (currentLevelXp * 100.0) / requiredXp));
    }

    public int getCurrentLevelXp() {
        int currentLevel = getLevel();
        int xpForCurrentLevel = getXpForLevel(currentLevel);
        return experience - xpForCurrentLevel;
    }

    public int getRequiredXpForNextLevel() {
        int currentLevel = getLevel();
        return getXpForLevel(currentLevel + 1) - getXpForLevel(currentLevel);
    }

    public String getProgressBar() {
        double percent = getProgressPercent();
        int filled = (int) Math.round(percent / 10.0);
        
        StringBuilder bar = new StringBuilder("§b");
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                bar.append("■");
            } else {
                if (i == filled) bar.append("§7");
                bar.append("■");
            }
        }
        return bar.toString();
    }

    public String getRank() {
        try {
            return BedWars.getInstance().getLevelsManager().getRankForLevel(getLevel());
        } catch (Exception e) {
            int level = getLevel();
            if (level >= 100) return "§6§lLegend";
            if (level >= 75) return "§c§lMaster";
            if (level >= 50) return "§5§lExpert";
            if (level >= 25) return "§b§lAdvanced";
            if (level >= 10) return "§a§lIntermediate";
            return "§7Beginner";
        }
    }

    public int addExperience(int xp) {
        int oldLevel = getLevel();
        this.experience += xp;
        int newLevel = getLevel();
        return (newLevel > oldLevel) ? newLevel : 0;
    }

    public int getCoins() {
        return 0;
    }
}
