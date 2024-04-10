package me.vivian.displayerutils;

import me.vivian.displayer.DisplayPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class LuckPermsIntegration { // loads WorldGuardIntegration if worldguard exists, otherwise assumes player can edit anywhere.
    public static Plugin luckPerms;
    public static boolean luckPermsExists = false;

    public static Method playerHasPermMethod;

    public static void init(DisplayPlugin plugin) {
        luckPerms = getLuckPerms(plugin);
    }

    public static Plugin getLuckPerms(DisplayPlugin plugin) {
        Plugin luckPermsPlugin = plugin.getServer().getPluginManager().getPlugin("LuckPerms");
        if (luckPermsPlugin == null) {
            System.out.println("displayer: LuckPerms is null, continuing without it");
            return null;
        }

        // load the WorldGuardHandler class ONLY if worldguard exists
        try {
            luckPermsExists = true;
            return luckPermsPlugin;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean playerHasPerm(Player player, String perm) {
        if (luckPerms == null) return true;

        try {
            LuckPerms api = LuckPermsProvider.get();
            User user = api.getUserManager().getUser(player.getUniqueId());

            if (user != null) {
                QueryOptions queryOptions = api.getContextManager().getQueryOptions(player);
                return user.getCachedData().getPermissionData(queryOptions).checkPermission(perm).asBoolean();
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
