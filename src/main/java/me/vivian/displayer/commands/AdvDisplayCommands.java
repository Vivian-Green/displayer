package me.vivian.displayer.commands;

import me.vivian.displayerutils.CommandParsing;
import me.vivian.displayerutils.ParticleHandler;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.TransformMath;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdvDisplayCommands {

    static Map<String, String> errMap = Texts.getErrors();

    /**
     * writes an awful, technical, details message
     *
     * @param player The player who issued the command.
     */
    static void handleAdvDisplayDetailsCommand(Player player) { // todo: EW AAAAA GROSS EW NO
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedDisplayIfExists(player);
        if (selectedVivDisplay == null) {return;}

        // Get display information directly from the selected VivDisplay
        Location displayLocation = TransformMath.locationRoundedTo(selectedVivDisplay.display.getLocation(), 2);
        double currentYaw = TransformMath.roundTo(displayLocation.getYaw(), 2);
        double currentPitch = TransformMath.roundTo(displayLocation.getPitch(), 2);
        double currentRoll = TransformMath.roundTo(TransformMath.getTransRoll(selectedVivDisplay.display.getTransformation()), 2);

        // Send the details to the player
        player.sendMessage("Display Name: " + selectedVivDisplay.displayName);
        player.sendMessage("Display Material: " + selectedVivDisplay.getItemStack().getType());
        player.sendMessage("Display Position: X=" + displayLocation.getX() + " Y=" + displayLocation.getY() + " Z=" + displayLocation.getZ());
        player.sendMessage("Display Rotation: Yaw=" + currentYaw + " Pitch=" + currentPitch + " Roll=" + currentRoll);
        player.sendMessage("Display Size: " + TransformMath.roundTo(selectedVivDisplay.display.getTransformation().getScale().x, 2));
        player.sendMessage("Distance to Display: " + TransformMath.roundTo(player.getLocation().distance(displayLocation), 2));

        // Send NBT data related to parent and child
        CommandHandler.sendPlayerMessageIf(player, "Parent UUID: " + selectedVivDisplay.parentUUID, selectedVivDisplay.isChild);

        player.sendMessage("Is Parent: " + selectedVivDisplay.isThisParent());
    }

    /**
     * Handles the renaming of a (player)'s selected display by adding a custom NBT tag with a given arg name
     *
     * @param player The player who issued the command.
     * @param args   Command arguments:
     *               - /advdisplay rename <name>
     */
    static void handleAdvDisplayRenameCommand(Player player, String[] args) {
        if (args.length < 2) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("advDisplayRenameUsage"));
            return;
        }
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedDisplayIfExists(player);
        if (selectedVivDisplay == null) {return;}

        String name = args[1]; // Get the name to set
        CommandHandler.sendPlayerMessageIfExists(player, selectedVivDisplay.rename(name));
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
            return; // Invalid UUID format
        }

        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(player, 5); // todo: config this-

        // Find the VivDisplay with the specified UUID
        VivDisplay selectedVivDisplay = nearbyVivDisplays.stream()
                .filter(vivDisplay -> vivDisplay.display.getUniqueId().equals(displayUUID))
                .findFirst()
                .orElse(null);

        if (selectedVivDisplay != null) {
            CommandHandler.selectedVivDisplays.put(player, selectedVivDisplay);
            ParticleHandler.spawnParticle(selectedVivDisplay.display, null, null);

            // open gui if selecting from here
            player.performCommand("display gui");
        }
    }


    /**
     * Rotates the selected VivDisplay for a player. Allows changing (+=) or setting (=) the rotation around 2 or 3 axis
     *
     * @param player The player performing the command.
     * @param args   Command arguments:
     *               - For changing the rotation: /display changerotation <yawOffset> <pitchOffset> [rollOffset]
     *               - For setting the rotation: /display setrotation <yawOffset> <pitchOffset> [rollOffset]
     */
    static void handleAdvDisplayRotationCommand(Player player, String[] args) {
        boolean isChange = args.length > 0 && "changerotation".equalsIgnoreCase(args[0]);

        if (args.length < 4) {
            CommandHandler.sendPlayerMessageIf(player, errMap.get("advDisplayChangeRotationUsage"), isChange, errMap.get("advDisplaySetRotationUsage"));
            return;
        }

        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedDisplayIfExists(player);
        if (selectedVivDisplay == null) {return;}

        float[] rotationOffsets = CommandParsing.parseRotationOffsets(player, args);
        if (rotationOffsets == null) {return;}

        boolean success = isChange ?
                selectedVivDisplay.changeRotation(rotationOffsets[0], rotationOffsets[1], rotationOffsets[2], player) :
                selectedVivDisplay.setRotation(rotationOffsets[0], rotationOffsets[1], rotationOffsets[2], player);

        CommandHandler.sendPlayerMessageIf(player, "Failed to apply rotation change.", !success);
    }

    /**
     * Handles the positioning of a (player)'s selected VivDisplay
     * Allows changing or setting the position with optional offsets.
     *
     * @param player The player performing the command.
     * @param args   Command arguments:
     *               - For changing the position: /display changeposition <xOffset> <yOffset> <zOffset>
     *               - For setting the position: /display setposition <x> <y> <z>
     */
    static void handleAdvDisplayPositionCommand(Player player, String[] args) {
        boolean isChange = args.length > 0 && "changeposition".equalsIgnoreCase(args[0]);

        if (args.length != 4) {
            CommandHandler.sendPlayerMessageIf(player, errMap.get("advDisplayChangePositionUsage"), isChange, errMap.get("advDisplaySetPositionUsage"));
            return;
        }

        double[] positionOffsets = CommandParsing.parsePositionOffsets(args, player);
        if (positionOffsets == null) {
            return;
        }

        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedDisplayIfExists(player);
        if (selectedVivDisplay == null) {return;}

        boolean success = isChange ?
                selectedVivDisplay.changePosition(positionOffsets[0], positionOffsets[1], positionOffsets[2]) :
                selectedVivDisplay.setPosition(positionOffsets[0], positionOffsets[1], positionOffsets[2], player);

        CommandHandler.sendPlayerMessageIf(player, "Failed to apply position", !success);
    }

    /**
     * changes (+=) or sets (=) the (player)'s selected VivDisplay's size.
     *
     * @param player The player performing the command.
     * @param args   Command arguments:
     *               - For changing the size: /display changesize <size offset: x y z>
     *               - For setting the size: /display setsize <size: x y x>
     */
    static void handleAdvDisplaySizeCommand(Player player, String[] args) {
        boolean isChange = args.length > 0 && "changesize".equalsIgnoreCase(args[0]);

        String errorMessage = isChange ?
                errMap.get("advDisplayChangeSizeInvalid") :
                errMap.get("advDisplaySetSizeInvalid");

        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedDisplayIfExists(player);
        if (selectedVivDisplay == null) return; // todo: warn?

        Transformation transformation = selectedVivDisplay.display.getTransformation();
        double currentSize = transformation.getScale().x;
        double minSize = isChange ? -currentSize : 0.0;
        double sizeArg = CommandParsing.parseNumberFromArgs(args, 1, minSize, minSize + 1, player, errorMessage);

        if (sizeArg < minSize) return; // todo: warn?

        double newScale = isChange ? (currentSize + sizeArg) : sizeArg;

        if (newScale > 0.0) {
            transformation.getScale().set(newScale);
            selectedVivDisplay.display.setTransformation(transformation);
        } else {
            player.sendMessage(errorMessage);
        }
    }

}
