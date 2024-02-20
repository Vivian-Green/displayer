package me.vivian.displayer;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {

    private final File configFile;
    private static FileConfiguration config;

    public Config(File configFile) {
        this.configFile = configFile;
    }

    public void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        // Set defaults if the configuration file doesn't exist
        config.addDefault("doDisplayGroups", false);
        config.options().copyDefaults(true);

        // Save the defaults to the configuration file if it doesn't exist
        saveConfigToFile();
    }

    public void saveConfigToFile() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration getConfig() {
        return config;
    }
}

