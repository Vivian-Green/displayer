// WorldGuardIntegration.java
package me.vivian.displayerutils;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;

public class WorldGuardIntegration {
    public static Object worldGuardHandler;
    public static boolean worldGuardExists = false;
    public static Method canEditDisplayHereMethod;
    public static Method playerCanFlagMethod;
    public static Method canEditDisplayMethod;
    public static Method canEditThisDisplayMethod;

    public WorldGuardIntegration() {
        // ohgod reflection go brrrrr
        //      todo: sweep off the coke
        worldGuardHandler = getWorldGuardHandler();
        if (worldGuardHandler != null) {
            try {
                canEditDisplayHereMethod = worldGuardHandler.getClass().getMethod("canEditDisplayHere", Player.class, Vector.class);
                playerCanFlagMethod = worldGuardHandler.getClass().getMethod("playerCanFlag", Player.class, Class.forName("com.sk89q.worldguard.protection.flags.StateFlag"));
                canEditDisplayMethod = worldGuardHandler.getClass().getMethod("canEditDisplay", Player.class);
                canEditThisDisplayMethod = worldGuardHandler.getClass().getMethod("canEditThisDisplay", Player.class, VivDisplay.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getWorldGuardHandler() {
        Plugin plugin = CommandHandler.getPlugin().getServer().getPluginManager().getPlugin("WorldGuard");

        if (plugin == null) {
            return null;
        }

        // Use reflection to load the WorldGuardHandler class
        try {
            Class<?> worldGuardHandlerClass = Class.forName("me.vivian.displayerutils.WorldGuardHandler");
            worldGuardExists = true;
            return worldGuardHandlerClass.getConstructor(Plugin.class).newInstance(plugin);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean canEditDisplayHere(Player player, Vector position) {
        if (worldGuardHandler == null) {
            return true;
        }

        try {
            return (boolean) canEditDisplayHereMethod.invoke(worldGuardHandler, player, position);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean canEditDisplay(Player player) {
        if (worldGuardHandler == null) {
            return true;
        }

        try {
            return (boolean) canEditDisplayMethod.invoke(worldGuardHandler, player);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean canEditThisDisplay(Player player, VivDisplay vivDisplay) {
        if (worldGuardHandler == null) {
            return true;
        }

        try {
            return (boolean) canEditThisDisplayMethod.invoke(worldGuardHandler, player, vivDisplay);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
