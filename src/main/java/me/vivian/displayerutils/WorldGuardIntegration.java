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
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class WorldGuardIntegration {
    public static WorldGuardPlugin worldGuardPlugin;

    public WorldGuardIntegration() {
        worldGuardPlugin = getWorldGuard();
    }

    private ArrayList<Player> entered = new ArrayList<>();
    private ArrayList<Player> left = new ArrayList<>();

    public static boolean playerCanFlagHere(Player player, StateFlag flag, Vector position){ // Flags.BUILD
        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(player);

        BlockVector3 thisBlockVec3 = BlockVector3.at(position.getX(), position.getY() + 1, position.getZ()); // +1 to Y coordinate for head location

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(localPlayer.getWorld());
        ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(thisBlockVec3);

        return applicableRegionSet.testState(localPlayer, flag);
    }

    public static boolean playerCanFlag(Player player, StateFlag flag){ // Flags.BUILD
        return playerCanFlagHere(player, flag, player.getLocation().toVector());
    }

    public static boolean canEditDisplayHere(Player player, Vector position) {
        // todo: change perms required here permissions worldguard luckperms luckpermissions lp config
        //      alternatively: config perms required here-
        return playerCanFlagHere(player, Flags.BUILD, position);
    }

    public static boolean canEditDisplay(Player player) {
        // todo: change perms required here permissions worldguard luckperms luckpermissions lp config
        //      alternatively: config perms required here-
        return playerCanFlagHere(player, Flags.BUILD, player.getLocation().toVector());
    }

    public static boolean canEditThisDisplay(Player player, VivDisplay vivDisplay) {
        // todo: change perms required here permissions worldguard luckperms luckpermissions lp config
        //      alternatively: config perms required here-
        return playerCanFlagHere(player, Flags.BUILD, vivDisplay.display.getLocation().toVector());
    }


    public static WorldGuardPlugin getWorldGuard(){
        Plugin plugin = CommandHandler.getPlugin().getServer().getPluginManager().getPlugin("WorldGuard");

        if(!(plugin instanceof WorldGuardPlugin)) {
            return null;
        }

        return (WorldGuardPlugin) plugin;
    }
}
