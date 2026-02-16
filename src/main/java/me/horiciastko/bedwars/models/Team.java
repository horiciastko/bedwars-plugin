package me.horiciastko.bedwars.models;

import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
@Data
public class Team {

    private final String name;
    private final String displayName;
    private final ChatColor color;
    private final Material material;
    private final List<Player> members = new ArrayList<>();
    private Location spawnLocation;
    private Location bedLocation;
    private Location shopLocation;
    private Location upgradeLocation;
    private Location basePos1;
    private Location basePos2;
    private final List<Location> generators = new ArrayList<>();
    private boolean bedBroken = false;
    private boolean eliminated = false;
    private final Map<String, Integer> upgrades = new HashMap<>();
    private final List<String> activeTraps = new ArrayList<>();

    private int generatorTier = 1;
    private int ironCooldown = 0;
    private int goldCooldown = 0;
    private final Map<String, Integer> resourceCooldowns = new HashMap<>();

    public Team(String name, String displayName, ChatColor color, Material material) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.material = material;
        resourceCooldowns.put("IRON_INGOT", 0);
        resourceCooldowns.put("GOLD_INGOT", 0);
    }

    public int getUpgradeLevel(String key) {
        return upgrades.getOrDefault(key, 0);
    }

    public void setUpgradeLevel(String key, int level) {
        upgrades.put(key, level);
    }

    public void reset() {
        members.clear();
        upgrades.clear();
        activeTraps.clear();
        bedBroken = false;
        eliminated = false;
        generatorTier = 1;
        ironCooldown = 0;
        goldCooldown = 0;
        resourceCooldowns.clear();
    }
}
