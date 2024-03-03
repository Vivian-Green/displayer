package me.vivian.displayer.commands;

import me.vivian.displayer.config.Texts;
import me.vivian.displayerutils.ParticleHandler;
import me.vivian.displayer.display.DisplayGroupHandler;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.joml.Vector3d;

import java.util.Map;

public class DisplayGroupCommands { // todo: move errs to texts.yml[errors]
    static Map<String, String> errMap = Texts.getErrors();

    /**
     * Handles the copy and paste command for a player's selected display group.
     *
     * @param player The player issuing the command.
     * @param args   Command arguments:
     *               - /display copypaste
     */
    public static void handleDisplayGroupCopyPasteCommand(Player player, String[] args) {
        // Get the player's selected VivDisplay
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);

        // If the player has not selected a VivDisplay, send an error message and return
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("noSelectedDisplay"));
            return;
        }

        // Get the player's current location as the new location for the copied VivDisplay
        Location newLocation = player.getLocation();

        // Copy and paste the hierarchy of the selected VivDisplay at the new location
        DisplayGroupHandler.copyAndPasteHierarchy(selectedVivDisplay, player, newLocation);

        // Send a success message to the player
        CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupPasteSuccess"));
    }

    // Sets the parent of the (player)'s selected VivDisplay.
    public static void handleDisplayGroupSetParentCommand(Player player, String[] args) {
        if (args.length < 2) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupSetParentUsage"));
            return;
        }

        String parentName = args[1]; // Get the parent display name to set

        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);

        // Find the parent display by name
        Display parentDisplay = DisplayHandler.getVivDisplayByName(player, parentName);
        if (parentDisplay == null) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupSetParentNoParent_Begin") + parentName + errMap.get("displayGroupSetParentNoParent_End"));
            return;
        }

        CommandHandler.sendPlayerMessageIfExists(player, selectedVivDisplay.setParent(parentDisplay));
    }

    // Unsets the parent of the (player)'s selected VivDisplay.
    static void handleDisplayGroupUnparentCommand(Player player) {
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = CommandHandler.selectedVivDisplays.get(player);

        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("noSelectedDisplay"));
            return;
        }

        CommandHandler.sendPlayerMessageIfExists(player, selectedVivDisplay.unsetParent());
    }

    /**
     * Handles the rotate command for a player's selected VivDisplay group.
     *
     * @param player The player issuing the command.
     * @param args   Command arguments:
     *               - /displaygroup rotate <xRotation> <yRotation> <zRotation>
     */
    public static void handleDisplayGroupRotateCommand(Player player, String[] args) {
        // Check if the correct number of arguments are provided
        if (args.length != 4) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupRotateUsage"));
            return;
        }

        // Parse the rotation from the arguments
        double pitch, yaw, roll;
        try {
            pitch = Double.parseDouble(args[1]);
            yaw = Double.parseDouble(args[2]);
            roll = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupInvalidRotation"));
            return;
        }

        // Get the player's selected VivDisplay
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);

        // If the player has not selected a VivDisplay, send an error message and return
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("noSelectedDisplay"));
            return;
        }

        // yaw, pitch, roll

        // Rotate the hierarchy of the selected VivDisplay
        DisplayGroupHandler.rotateHierarchy(selectedVivDisplay, new Vector3d(roll, yaw, pitch));

        // Send a success message to the player
        CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupRotateSuccess"));
    }

    /**
     * Handles the translate command for a player's selected VivDisplay group.
     *
     * @param player The player issuing the command.
     * @param args   Command arguments:
     *               - /displaygroup translate <xTranslation> <yTranslation> <zTranslation>
     */
    public static void handleDisplayGroupTranslateCommand(Player player, String[] args) {
        // Check if the correct number of arguments are provided
        if (args.length != 4) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupTranslateUsage"));
            return;
        }

        // Parse the translation from the arguments
        double xTranslation, yTranslation, zTranslation;
        try {
            xTranslation = Double.parseDouble(args[1]);
            yTranslation = Double.parseDouble(args[2]);
            zTranslation = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupTranslateInvalidTranslation"));
            return;
        }

        // Get the player's selected VivDisplay
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);

        // If the player has not selected a VivDisplay, send an error message and return
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("noSelectedDisplay"));
            return;
        }

        // Translate the hierarchy of the selected VivDisplay
        DisplayGroupHandler.translateHierarchy(selectedVivDisplay, new Vector3d(xTranslation, yTranslation, zTranslation));

        // Send a success message to the player
        CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupTranslateSuccess"));
    }

    public static void handleDisplayGroupShowCommand(Player player, String[] args) {
        // Check if the correct number of arguments is provided
        if (args.length != 1) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupShowUsage"));
            return;
        }

        // Get the player's selected VivDisplay by name
        VivDisplay selectedVivDisplay = CommandHandler.selectedVivDisplays.get(player);

        // Check if the selected VivDisplay exists
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMessageIfExists(player, errMap.get("noSelectedDisplay"));
            return;
        }

        // Spawn particles at every display in the hierarchy
        Particle particle = null;
        int particleCount = 5000;
        ParticleHandler.spawnParticlesAtHierarchy(selectedVivDisplay, particle, particleCount);

        // Send a success message to the player
        CommandHandler.sendPlayerMessageIfExists(player, errMap.get("displayGroupShowSuccess"));
    }
}
