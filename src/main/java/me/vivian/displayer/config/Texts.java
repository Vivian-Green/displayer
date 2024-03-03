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

    public static void loadTexts() {
        // todo: just copy a gd default file, this is already getting too long
        //       alternatively, make this so long that someone else is more disgusted than you & does it for you
        File textsFile = new File(CommandHandler.getPlugin().getDataFolder(), "texts.yml");
        tCfg = YamlConfiguration.loadConfiguration(textsFile);
    }

    public static FileConfiguration getTextsConfig() { // todo: getter & setter for just the gd texts
        return tCfg;
    }

    public static String getText(String key) {
        return tCfg.getString(key);
    }

    public static List<String> getTexts(String key) {
        // todo: ensure not null before cast if necessary?
        return (List<String>) tCfg.getList(key);
    }

    public static Map<String, String> loadMessages(String section) {
        Map<String, Object> messagesAsObjects = tCfg.getConfigurationSection(section).getValues(false);
        Map<String, String> messages = new HashMap<>();

        for (Map.Entry<String, Object> entry : messagesAsObjects.entrySet()) {
            if (entry.getValue() instanceof String) {
                messages.put(entry.getKey(), (String) entry.getValue());
            } else {
                System.out.println("Error loading messages on key " + entry.getKey());
            }
        }

        return messages;
    }

    public static Map<String, String> getErrors() {
        return loadMessages("errors");
    }

    public static Map<String, String> getMessages() {
        return loadMessages("messages");
    }
}

