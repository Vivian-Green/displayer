// WorldGuardHandler.java
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
import me.vivian.displayer.config.Config;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class WorldGuardHandler {
    public StateFlag displayEditingFlag = null;
    public WorldGuardPlugin worldGuardPlugin;

    public WorldGuardHandler(Plugin worldGuardPlugin) {
        this.worldGuardPlugin = (WorldGuardPlugin) worldGuardPlugin;
        loadFlagFromConfig();
    }

    private void loadFlagFromConfig() {
        String flagName = Config.getConfig().getString("worldguardFlag");
        assert flagName != null;
        displayEditingFlag = (StateFlag) Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);
    }

    public boolean canEditDisplayHere(Player player, Vector position) {
        if (displayEditingFlag == null) {
            return true;
        }

        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(player);
        BlockVector3 thisBlockVec3 = BlockVector3.at(position.getX(), position.getY() + 1, position.getZ()); // +1 to Y coordinate for head location

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(localPlayer.getWorld());

        if (regionManager == null) {
            return true;
        }

        ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(thisBlockVec3);

        return applicableRegionSet.testState(localPlayer, displayEditingFlag);
    }
    public boolean playerCanFlag(Player player, StateFlag flag) {
        return playerCanFlagHere(player, flag, player.getLocation().toVector());
    }

    public boolean canEditDisplay(Player player) {
        return canEditDisplayHere(player, player.getLocation().toVector());
    }

    public boolean canEditThisDisplay(Player player, VivDisplay vivDisplay) {
        return canEditDisplayHere(player, vivDisplay.display.getLocation().toVector());
    }

    private boolean playerCanFlagHere(Player player, StateFlag flag, Vector position) {
        if (displayEditingFlag == null) {
            return true;
        }

        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(player);
        BlockVector3 thisBlockVec3 = BlockVector3.at(position.getX(), position.getY() + 1, position.getZ()); // +1 to Y coordinate for head location

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(localPlayer.getWorld());

        if (regionManager == null) {
            return true;
        }

        ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(thisBlockVec3);

        return applicableRegionSet.testState(localPlayer, flag);
    }
}