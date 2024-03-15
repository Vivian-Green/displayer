package me.vivian.displayer.config;

import me.vivian.displayer.DisplayPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Texts {
    private static FileConfiguration tCfg;
    public static int loadState = 0;
    public static Map<String, String> messages;
    public static Map<String, String> errors;


    public static void loadTexts(DisplayPlugin plugin) {
        System.out.println("trying to load texts...");
        if (loadState == 0) {
            File textsFile = new File(plugin.getDataFolder(), "texts.yml");

            tCfg = YamlConfiguration.loadConfiguration(textsFile);
            loadState = 1;

            errors = loadMessages("errors"); // loadState++++
            messages = loadMessages("messages"); // loadState++++
        }
    }

    public static String getText(String key) {
        return tCfg.getString(key);
    }

    public static List<String> getTexts(String key) {
        return (List<String>) tCfg.getList(key);
    }

    public static Map<String, String> loadMessages(String section) {
        Map<String, Object> messagesAsObjects = tCfg.getConfigurationSection(section).getValues(false);
        Map<String, String> messages = new HashMap<>();

        loadState++;

        for (Map.Entry<String, Object> entry : messagesAsObjects.entrySet()) {
            if (entry.getValue() instanceof String) {
                messages.put(entry.getKey(), ((String) entry.getValue()).replace("&&", "§"));
            } else {
                System.out.println("Error loading messages on key " + entry.getKey());
            }
        }

        loadState++;

        return messages;
    }
}

