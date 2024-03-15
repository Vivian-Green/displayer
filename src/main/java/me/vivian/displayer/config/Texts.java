package me.vivian.displayer.config;

import me.vivian.displayer.commands.CommandHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Texts {
    private static FileConfiguration tCfg;
    public static int loadState = 0;

    public static void loadTexts() {
        System.out.println("trying to load texts...");
        if (loadState == 0) {
            // todo: just copy a gd default file, this is already getting too long
            //       alternatively, make this so long that someone else is more disgusted than you & does it for you
            File textsFile = new File(CommandHandler.getPlugin().getDataFolder(), "texts.yml");
            tCfg = YamlConfiguration.loadConfiguration(textsFile);
            loadState = 1;
        }
    }

    public static FileConfiguration getTextsConfig() { // todo: getter & setter for just the gd texts
        if (tCfg == null) {
            loadTexts();
        }
        return tCfg;
    }

    public static String getText(String key) {
        if (tCfg == null) {
            loadTexts();
        }
        return tCfg.getString(key);
    }

    public static List<String> getTexts(String key) {
        if (tCfg == null) {
            loadTexts();
        }

        return (List<String>) tCfg.getList(key);
    }

    public static Map<String, String> loadMessages(String section) {
        if (loadState == 0){
            loadTexts();
        }


        if (tCfg == null) {
            loadTexts();
            if (tCfg == null) {
                return null;
            }
        }

        Map<String, Object> messagesAsObjects = tCfg.getConfigurationSection(section).getValues(false);
        Map<String, String> messages = new HashMap<>();

        loadState = 2;

        for (Map.Entry<String, Object> entry : messagesAsObjects.entrySet()) {
            if (entry.getValue() instanceof String) {
                messages.put(entry.getKey(), ((String) entry.getValue()).replace("&&", "§"));
            } else {
                System.out.println("Error loading messages on key " + entry.getKey());
            }
        }

        loadState = 3;

        return messages;
    }

    public static Map<String, String> getErrors() {
        return loadMessages("errors");
    }

    public static Map<String, String> getMessages() {
        return loadMessages("messages");
    }
}

