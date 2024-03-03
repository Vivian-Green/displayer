package me.vivian.displayer.config;

import me.vivian.displayer.commands.CommandHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    private static FileConfiguration config;

    public static void loadConfig() {
        File configFile = new File(CommandHandler.getPlugin().getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static FileConfiguration getConfig() {
        return config;
    }
}

