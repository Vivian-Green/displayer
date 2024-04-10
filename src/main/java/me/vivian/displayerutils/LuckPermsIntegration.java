package me.vivian.displayerutils;

import me.vivian.displayer.DisplayPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class LuckPermsIntegration { // Handled LuckPerms integrations of LuckPerms exists, otherwise assumes player can yes
    public static Plugin luckPerms = null;

    public static void init(DisplayPlugin plugin) {
        getLuckPerms(plugin);
    }

    public static void getLuckPerms(DisplayPlugin plugin) {
        luckPerms = plugin.getServer().getPluginManager().getPlugin("LuckPerms");
        if (luckPerms == null) {
            System.out.println("displayer: LuckPerms is null, continuing without it");
        }
    }


    public static boolean playerHasPerm(Player player, String perm) {
        if (luckPerms == null) return true;

        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            QueryOptions queryOptions = api.getContextManager().getQueryOptions(player);
            return user.getCachedData().getPermissionData(queryOptions).checkPermission(perm).asBoolean();
        }

        return false;
    }
}
