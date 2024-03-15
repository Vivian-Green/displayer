package me.vivian.displayer.commands;

import me.vivian.displayer.config.Config;
import me.vivian.displayerutils.ParticleHandler;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.TransformMath;
import me.vivian.displayerutils.WorldGuardIntegrationWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdvDisplayCommands {
    static FileConfiguration config = Config.getConfig();
    static Map<String, String> errMap = Texts.getErrors();

    /**
     * writes an awful, technical, details message
     *
     * @param player The player who issued the command.
     */
    static void handleAdvDisplayDetailsCommand(Player player) { // todo: EW AAAAA GROSS EW NO
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);

        if (selectedVivDisplay == null) return;
        if(!WorldGuardIntegrationWrapper.canEditThisDisplay(player, selectedVivDisplay)) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("cantEditDisplayHere"));
            return;
        }

        // Get display information directly from the selected VivDisplay
        Location displayLocation = TransformMath.locationRoundedTo(selectedVivDisplay.display.getLocation(), 2);
        double currentYaw = TransformMath.roundTo(displayLocation.getYaw(), 2);
        double currentPitch = TransformMath.roundTo(displayLocation.getPitch(), 2);
        double currentRoll = TransformMath.roundTo(TransformMath.getTransRoll(selectedVivDisplay.display.getTransformation()), 2);

        // Send the details to the player
        player.sendMessage("Display Name: " + selectedVivDisplay.displayName);

        if (selectedVivDisplay.display instanceof ItemDisplay || selectedVivDisplay.display instanceof BlockDisplay) {
            player.sendMessage("Display Material: " + selectedVivDisplay.getMaterial());
            player.sendMessage("Display Size: " + TransformMath.roundTo(selectedVivDisplay.display.getTransformation().getScale().x, 2));
        }

        player.sendMessage("Display Position: X=" + displayLocation.getX() + " Y=" + displayLocation.getY() + " Z=" + displayLocation.getZ());
        player.sendMessage("Display Rotation: Yaw=" + currentYaw + " Pitch=" + currentPitch + " Roll=" + currentRoll);


        player.sendMessage("Distance to Display: " + TransformMath.roundTo(player.getLocation().distance(displayLocation), 2));

        // Send NBT data related to parent and child
        CommandHandler.sendPlayerAifBelseC(player, "Parent UUID: " + selectedVivDisplay.parentUUID, selectedVivDisplay.isChild);

        player.sendMessage("Is Parent: " + selectedVivDisplay.isParentDisplay());
    }

    /**
     * Selects a display for the player given a UUID.
     *
     * @param player The player who issued the command.
     * @param args   Command arguments: - args[1]: The UUID of the display to select.
     */
    static void handleAdvDisplaySelectCommand(Player player, String[] args) { // this should never be executed by the player
        // todo: early return if player
        if (args.length < 2) {
            return;
        }

        // if UUID specified
        UUID displayUUID;
        try {
            displayUUID = UUID.fromString(args[1]);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("handleAdvDisplaySelectCommand: failed to create a UUID from arg 1 of '/advdisplay select ?'");
            return;
        }

        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(player.getLocation(), config.getInt("maxSearchRadius"), player);

        // Find the VivDisplay with the specified UUID
        VivDisplay selectedVivDisplay = nearbyVivDisplays.stream()
                .filter(vivDisplay -> vivDisplay.display.getUniqueId().equals(displayUUID))
                .findFirst()
                .orElse(null);

        if (selectedVivDisplay == null) {
            return;
        }

        if(!WorldGuardIntegrationWrapper.canEditThisDisplay(player, selectedVivDisplay)) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("cantEditDisplayHere"));
            return;
        }

        CommandHandler.selectedVivDisplays.put(player, selectedVivDisplay);
        ParticleHandler.spawnParticle(selectedVivDisplay.display, null, null);

        // open gui if selecting from here
        player.performCommand("display gui");
    }
}
