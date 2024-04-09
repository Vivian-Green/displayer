package me.vivian.displayer.config;

import me.vivian.displayer.DisplayPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Texts {
    private static FileConfiguration tCfg;
    private static FileConfiguration dtCfg;
    public static int loadState = 0;
    public static Map<String, String> messages;
    public static Map<String, String> errors;

    public static String getError(String errorName){
        String nativeErr = errors.get(errorName);
        if (nativeErr == null) {
            String defaultErr = errors.get("default");
            if (defaultErr == null) defaultErr = "no error found for $errorname";

            defaultErr = defaultErr.replace("$errorname", errorName);
            return defaultErr;
        }
        return nativeErr;
    }

    public static String getText(String key) {
        String nativeStr = tCfg.getString(key);
        if (nativeStr == null) {
            String defaultStr = dtCfg.getString(key);
            if (defaultStr == null) return "[text not found for key " + key + "]";
            return "[en " + key + "] " + defaultStr;
        }

        return nativeStr;
    }

    public static List<String> getTexts(String key) {
        return (List<String>) tCfg.getList(key); // todo: add check against default texts & default texts
    }


    public static void loadTexts(DisplayPlugin plugin) {
        System.out.println("[displayer] trying to load texts...");
        if (loadState == 0) {
            Boolean defaulted = false;

            String defaultTextsFileName = Config.defaultTextsFileName;
            if (defaultTextsFileName == null || defaultTextsFileName.isEmpty()) defaultTextsFileName = "texts_en.yml";

            String textsFileName = Config.textsFileName;
            if (textsFileName == null || textsFileName.isEmpty()) textsFileName = "texts_en.yml";

            File textsFile = new File(plugin.getDataFolder(), textsFileName);
            File defaultTextsFile = new File(plugin.getDataFolder(), defaultTextsFileName);
            // handle case file doesn't exist, default to Config.defaultTextsFileName.
            if (!textsFile.exists()) {
                System.out.println("Texts file specified in Config.yml doesn't exist, using default");
                textsFile = defaultTextsFile;
                defaulted = true;
            }

            // handle case Config.defaultTextsFileName file doesn't exist either, return early
            if (!textsFile.exists()) {
                System.out.println("Failed to load texts from default file");
                return;
            }
            tCfg = YamlConfiguration.loadConfiguration(textsFile);
            if (!defaulted) {
                dtCfg = YamlConfiguration.loadConfiguration(defaultTextsFile);
            } else {
                dtCfg = tCfg;
            }

            loadState = 1;

            errors = loadMessages("errors"); // loadState++++
            messages = loadMessages("messages"); // loadState++++
        }
    }

    public static Map<String, String> loadMessages(String section, FileConfiguration fCfg) {
        Map<String, Object> messagesAsObjects = fCfg.getConfigurationSection(section).getValues(false);
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

    public static Map<String, String> loadMessages(String section) {
        Map<String, String> nativeMessages = loadMessages(section, tCfg);
        if (Objects.equals(Config.defaultTextsFileName, Config.textsFileName)) return nativeMessages; // using en, return it

        Map<String, String> defaultMessages = loadMessages(section, dtCfg); // using another lang, merge it with en, return that
        for (Map.Entry<String, String> entry : defaultMessages.entrySet()) {
            nativeMessages.putIfAbsent(entry.getKey(), Config.defaultTextsFileName + "." + entry.getKey() + ": " + entry.getValue());
        }

        return nativeMessages;
    }
}

