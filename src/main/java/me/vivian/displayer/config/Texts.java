package me.vivian.displayer.config;

import me.vivian.displayer.EventListeners;
import me.vivian.displayer.commands.CommandHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Texts {


    private static FileConfiguration tCfg;

    private static boolean isLoaded = false;

    public static void loadTexts() {
        // todo: just copy a gd default file, this is already getting too long
        //       alternatively, make this so long that someone else is more disgusted than you & does it for you
        File textsFile = new File(CommandHandler.getPlugin().getDataFolder(), "texts.yml");
        tCfg = YamlConfiguration.loadConfiguration(textsFile);

        isLoaded = true;
    }

    public static FileConfiguration getTextsConfig() { // todo: getter & setter for just the gd texts
        if(!isLoaded) loadTexts();
        return tCfg;
    }

    public static String getText(String key) {
        if(!isLoaded) loadTexts();
        return tCfg.getString(key);
    }

    public static List<String> getTexts(String key) {
        if(!isLoaded) loadTexts();

        // todo: ensure not null before cast if necessary?
        return (List<String>) tCfg.getList(key);
    }

    public static Map<String, String> getErrors() {
        if(!isLoaded) loadTexts();

        // todo: ensure not null before cast if necessary?
        // Map<String, Object> errors = tCfg.getConfigurationSection("errors").getValues(false);

        Map<String, Object> errorsButAsObjectsForNoReason = tCfg.getConfigurationSection("errors").getValues(false);
        Map<String, String> errors = new HashMap<>();

        for (Map.Entry<String, Object> entry : errorsButAsObjectsForNoReason.entrySet()) {
            if (entry.getValue() instanceof String) {
                errors.put(entry.getKey(), (String) entry.getValue());
            } else {
                System.out.println("error loading... errors? on key " + entry.getKey());
            }
        }

        return errors;
    }
}

