package me.vivian.displayerutils;

import me.vivian.displayer.DisplayPlugin;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class WorldGuardIntegrationWrapper { // loads WorldGuardIntegration if worldguard exists, otherwise assumes player can edit anywhere.
    public static Object worldGuardIntegration;
    public static boolean worldGuardExists = false;
    public static Method canEditDisplayMethod;
    public static Method canEditThisDisplayMethod;

    public static void init(DisplayPlugin plugin) {
        worldGuardIntegration = getWorldGuardIntegration(plugin);
        if (worldGuardIntegration != null) {
            try {
                canEditDisplayMethod = worldGuardIntegration.getClass().getMethod("canEditDisplay", Player.class);
                canEditThisDisplayMethod = worldGuardIntegration.getClass().getMethod("canEditThisDisplay", Player.class, VivDisplay.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getWorldGuardIntegration(DisplayPlugin plugin) {
        Plugin worldGuardPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin == null) {
            System.out.println("displayer: worldguard is null, continuing without region checks.");
            return null;
        }

        // load the WorldGuardHandler class ONLY if worldguard exists
        try {
            Class<?> worldGuardHandlerClass = Class.forName("me.vivian.displayerutils.WorldGuardIntegration");
            worldGuardExists = true;
            return worldGuardHandlerClass.getConstructor(Plugin.class).newInstance(worldGuardPlugin);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean canEditDisplay(Player player) {
        if (worldGuardIntegration == null) return true;

        try {
            return (boolean) canEditDisplayMethod.invoke(worldGuardIntegration, player);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean canEditThisDisplay(Player player, VivDisplay vivDisplay) {
        if (worldGuardIntegration == null) return true;

        try {
            return (boolean) canEditThisDisplayMethod.invoke(worldGuardIntegration, player, vivDisplay);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
