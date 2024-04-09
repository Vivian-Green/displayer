package me.vivian.displayer.config;

import me.vivian.displayer.DisplayPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    public static FileConfiguration config = null;
    public static String textsFileName = "";
    public static String defaultTextsFileName = "";
    public static void loadConfig(DisplayPlugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        textsFileName = config.getString("textsPath");
        defaultTextsFileName = config.getString("textsDefaultPath");
    }
}

