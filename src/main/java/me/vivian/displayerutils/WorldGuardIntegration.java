package me.vivian.displayerutils;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Config;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;

public class WorldGuardIntegration {
    public static StateFlag displayEditingFlag = null;
    public static WorldGuardPlugin worldGuardPlugin;

    public WorldGuardIntegration() {
        worldGuardPlugin = getWorldGuard();
    }



    public static boolean playerCanFlagHere(Player player, StateFlag flag, Vector position){ // Flags.BUILD
        if(worldGuardPlugin == null || displayEditingFlag == null){
            System.out.println("displayer: worldguard is null or display editing flag is null");
            return true;
        }

        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(player);

        BlockVector3 thisBlockVec3 = BlockVector3.at(position.getX(), position.getY() + 1, position.getZ()); // +1 to Y coordinate for head location

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(localPlayer.getWorld());

        if (regionManager == null) {
            System.out.println("displayer: worldguard region manager is null");
            return true;
        }

        ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(thisBlockVec3);

        return applicableRegionSet.testState(localPlayer, flag);
    }

    public static boolean playerCanFlag(Player player, StateFlag flag){ // Flags.BUILD
        return playerCanFlagHere(player, flag, player.getLocation().toVector());
    }

    public static boolean canEditDisplayHere(Player player, Vector position) {
        return playerCanFlagHere(player, displayEditingFlag, position);
    }

    public static boolean canEditDisplay(Player player) {
        return playerCanFlagHere(player, displayEditingFlag, player.getLocation().toVector());
    }

    public static boolean canEditThisDisplay(Player player, VivDisplay vivDisplay) {
        return playerCanFlagHere(player, displayEditingFlag, vivDisplay.display.getLocation().toVector());
    }

    public static WorldGuardPlugin getWorldGuard(){
        Plugin plugin = CommandHandler.getPlugin().getServer().getPluginManager().getPlugin("WorldGuard");

        if(!(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        loadFlagFromConfig();
        return (WorldGuardPlugin) plugin;
    }

    private static void loadFlagFromConfig() {
        String flagName = Config.getConfig().getString("worldguardFlag");
        assert flagName != null;
        displayEditingFlag = (StateFlag) Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);
    }
}
