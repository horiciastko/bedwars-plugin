package me.horiciastko.bedwars.logic;

import lombok.RequiredArgsConstructor;
import me.horiciastko.bedwars.BedWars;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class SoundManager {

    private final BedWars plugin;
    private FileConfiguration soundConfig;
    private File soundFile;

    public void load() {
        String lang = plugin.getConfig().getString("default-language", "en");
        File langDir = new File(plugin.getDataFolder(), "Languages/" + lang);
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        soundFile = new File(langDir, "sounds.yml");
        if (!soundFile.exists()) {
            plugin.saveResource("Languages/" + lang + "/sounds.yml", false);
        }
        soundConfig = YamlConfiguration.loadConfiguration(soundFile);
    }

    public void reload() {
        soundConfig = YamlConfiguration.loadConfiguration(soundFile);
    }

    public void playSound(Player player, String path) {
        if (soundConfig == null)
            return;

        String soundName = soundConfig.getString(path);
        if (soundName == null || soundName.isEmpty())
            return;

        com.cryptomorin.xseries.XSound.matchXSound(soundName).ifPresent(s -> s.play(player));
    }

    public void playSound(org.bukkit.Location loc, String path) {
        if (soundConfig == null)
            return;

        String soundName = soundConfig.getString(path);
        if (soundName == null || soundName.isEmpty())
            return;

        com.cryptomorin.xseries.XSound.matchXSound(soundName).ifPresent(s -> s.play(loc));
    }
}
