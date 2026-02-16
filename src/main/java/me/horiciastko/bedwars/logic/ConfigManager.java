package me.horiciastko.bedwars.logic;

import me.horiciastko.bedwars.BedWars;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final BedWars plugin;

    private FileConfiguration shopConfig;
    private File shopFile;

    private FileConfiguration upgradesConfig;
    private File upgradesFile;

    private FileConfiguration messagesConfig;
    private File messagesFile;

    private FileConfiguration generatorConfig;
    private File generatorFile;

    private FileConfiguration teamConfig;
    private File teamFile;

    private FileConfiguration npcConfig;
    private File npcFile;

    private FileConfiguration permissionsConfig;
    private File permissionsFile;


    public ConfigManager(BedWars plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        String lang = plugin.getConfig().getString("default-language", "en");
        File langDir = new File(plugin.getDataFolder(), "Languages/" + lang);
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        shopFile = new File(langDir, "shop.yml");
        if (!shopFile.exists()) {
            plugin.saveResource("Languages/" + lang + "/shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopFile);

        upgradesFile = new File(langDir, "upgrades.yml");
        if (!upgradesFile.exists()) {
            plugin.saveResource("Languages/" + lang + "/upgrades.yml", false);
        }
        upgradesConfig = YamlConfiguration.loadConfiguration(upgradesFile);

        messagesFile = new File(langDir, "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("Languages/" + lang + "/messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        generatorFile = new File(langDir, "generators.yml");
        if (!generatorFile.exists()) {
            plugin.saveResource("Languages/" + lang + "/generators.yml", false);
        }
        generatorConfig = YamlConfiguration.loadConfiguration(generatorFile);

        teamFile = new File(langDir, "teams.yml");
        if (!teamFile.exists()) {
            plugin.saveResource("Languages/" + lang + "/teams.yml", false);
        }
        teamConfig = YamlConfiguration.loadConfiguration(teamFile);

        npcFile = new File(langDir, "npc.yml");
        if (!npcFile.exists()) {
            plugin.saveResource("Languages/" + lang + "/npc.yml", false);
        }
        npcConfig = YamlConfiguration.loadConfiguration(npcFile);

        permissionsFile = new File(plugin.getDataFolder(), "permissions.yml");
        if (!permissionsFile.exists()) {
            plugin.saveResource("permissions.yml", false);
        }
        permissionsConfig = YamlConfiguration.loadConfiguration(permissionsFile);
    }

    public FileConfiguration getShopConfig() {
        if (shopConfig == null)
            reloadShopConfig();
        return shopConfig;
    }

    public FileConfiguration getUpgradesConfig() {
        if (upgradesConfig == null)
            reloadUpgradesConfig();
        return upgradesConfig;
    }

    public void reloadShopConfig() {
        if (shopFile != null)
            shopConfig = YamlConfiguration.loadConfiguration(shopFile);
    }

    public void reloadUpgradesConfig() {
        if (upgradesFile != null)
            upgradesConfig = YamlConfiguration.loadConfiguration(upgradesFile);
    }

    public void saveShopConfig() {
        try {
            if (shopConfig != null && shopFile != null)
                shopConfig.save(shopFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save shop.yml!");
        }
    }

    public void saveUpgradesConfig() {
        try {
            if (upgradesConfig != null && upgradesFile != null)
                upgradesConfig.save(upgradesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save upgrades.yml!");
        }
    }

    public FileConfiguration getMessagesConfig() {
        if (messagesConfig == null)
            reloadMessagesConfig();
        return messagesConfig;
    }

    public void reloadMessagesConfig() {
        if (messagesFile != null)
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadAll() {
        plugin.reloadConfig();
        loadConfigs();
        if (plugin.getSoundManager() != null)
            plugin.getSoundManager().reload();
        if (plugin.getLanguageManager() != null)
            plugin.getLanguageManager().load();
    }

    public FileConfiguration getTeamConfig() {
        if (teamConfig == null)
            reloadTeamConfig();
        return teamConfig;
    }

    public void reloadTeamConfig() {
        if (teamFile != null)
            teamConfig = YamlConfiguration.loadConfiguration(teamFile);
    }

    public FileConfiguration getGeneratorConfig() {
        if (generatorConfig == null)
            reloadGeneratorConfig();
        return generatorConfig;
    }

    public void reloadGeneratorConfig() {
        if (generatorFile != null)
            generatorConfig = YamlConfiguration.loadConfiguration(generatorFile);
    }

    public FileConfiguration getShopConfig(String group) {
        if (group == null || group.equalsIgnoreCase("Default")) {
            return getShopConfig();
        }

        String fileName = plugin.getConfig().getString("groups." + group + ".shop-file");
        if (fileName == null)
            fileName = "shop.yml";

        String lang = plugin.getConfig().getString("default-language", "en");
        File file = new File(plugin.getDataFolder(), "Languages/" + lang + "/" + fileName);

        if (!file.exists()) {
            return getShopConfig();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getUpgradesConfig(String group) {
        if (group == null || group.equalsIgnoreCase("Default")) {
            return getUpgradesConfig();
        }

        String fileName = plugin.getConfig().getString("groups." + group + ".upgrades-file");
        if (fileName == null)
            fileName = "upgrades.yml";

        String lang = plugin.getConfig().getString("default-language", "en");
        File file = new File(plugin.getDataFolder(), "Languages/" + lang + "/" + fileName);

        if (!file.exists()) {
            return getUpgradesConfig();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getNpcConfig() {
        if (npcConfig == null)
            reloadNpcConfig();
        return npcConfig;
    }

    public void reloadNpcConfig() {
        if (npcFile != null)
            npcConfig = YamlConfiguration.loadConfiguration(npcFile);
    }

    public FileConfiguration getPermissionsConfig() {
        if (permissionsConfig == null)
            reloadPermissionsConfig();
        return permissionsConfig;
    }

    public void reloadPermissionsConfig() {
        if (permissionsFile != null)
            permissionsConfig = YamlConfiguration.loadConfiguration(permissionsFile);
    }
}
