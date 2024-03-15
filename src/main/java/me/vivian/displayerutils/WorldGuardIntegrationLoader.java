// WorldGuardIntegration.java
package me.vivian.displayerutils;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class WorldGuardIntegrationLoader {
    public static Object worldGuardHandler;
    public static boolean worldGuardExists = false;
    public static Method canEditDisplayMethod;
    public static Method canEditThisDisplayMethod;

    public WorldGuardIntegrationLoader() {
        // ohgod reflection go brrrrr
        //      todo: sweep off the coke
        worldGuardHandler = getWorldGuardHandler();
        if (worldGuardHandler != null) {
            try {
                canEditDisplayMethod = worldGuardHandler.getClass().getMethod("canEditDisplay", Player.class);
                canEditThisDisplayMethod = worldGuardHandler.getClass().getMethod("canEditThisDisplay", Player.class, VivDisplay.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getWorldGuardHandler() {
        Plugin worldGuardPlugin = CommandHandler.getPlugin().getServer().getPluginManager().getPlugin("WorldGuard");
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
        if (worldGuardHandler == null) return true;

        try {
            return (boolean) canEditDisplayMethod.invoke(worldGuardHandler, player);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean canEditThisDisplay(Player player, VivDisplay vivDisplay) {
        if (worldGuardHandler == null) return true;

        try {
            return (boolean) canEditThisDisplayMethod.invoke(worldGuardHandler, player, vivDisplay);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
