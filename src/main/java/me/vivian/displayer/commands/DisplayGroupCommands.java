package me.vivian.displayer.commands;

import me.vivian.displayer.config.Config;
import me.vivian.displayer.config.Texts;
import me.vivian.displayerutils.CommandParsing;
import me.vivian.displayerutils.GUIBuilder;
import me.vivian.displayer.display.DisplayGroupHandler;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.List;

public class DisplayGroupCommands {
    static String displayGroupShowSuccessMsg;
    static String displayGroupTranslateSuccessMsg;
    static String displayGroupShowGUITitle;
    static String displayGroupRotateSuccessMsg;
    static String displayGroupPasteSuccessMsg;

    static String hierarchyIsNullErr;
    static String displayGroupShowUsageErr;
    static String displayGroupTranslateInvalidTranslationErr;
    static String displayGroupTranslateUsageErr;
    static String displayGroupInvalidRotationErr;
    static String displayGroupRotateUsageErr;
    static String displayGroupSetParentRecursiveErr;
    static String displayGroupSetParentNoParentErr;
    static String displayGroupSetParentUsageErr;
    static String noSelectedDisplayErr;

    public static void init(){
        displayGroupShowSuccessMsg = Texts.getText("displayGroupShowSuccess");
        displayGroupTranslateSuccessMsg = Texts.getText("displayGroupTranslateSuccess");
        displayGroupShowGUITitle = Texts.getText("displayGroupShowGUITitle");
        displayGroupRotateSuccessMsg = Texts.getText("displayGroupRotateSuccess");
        displayGroupPasteSuccessMsg = Texts.getText("displayGroupPasteSuccess");

        hierarchyIsNullErr = Texts.getError("hierarchyIsNull");
        displayGroupShowUsageErr = Texts.getError("displayGroupShowUsage");
        displayGroupTranslateInvalidTranslationErr = Texts.getError("displayGroupTranslateInvalidTranslation");
        displayGroupTranslateUsageErr = Texts.getError("displayGroupTranslateUsage");
        displayGroupInvalidRotationErr = Texts.getError("displayGroupInvalidRotation");
        displayGroupRotateUsageErr = Texts.getError("displayGroupRotateUsage");
        displayGroupSetParentRecursiveErr = Texts.getError("displayGroupSetParentRecursive");
        displayGroupSetParentNoParentErr = Texts.getError("displayGroupSetParentNoParent");
        displayGroupSetParentUsageErr = Texts.getError("displayGroupSetParentUsage");
        noSelectedDisplayErr = Texts.getError("noSelectedDisplay");
    }

    /**
     * Handles the copy and paste command for a player's selected display group.
     *
     * @param player The player issuing the command.
     * @param args   Command arguments:
     *               - /display copypaste
     */
    public static void handleDisplayGroupCopyPasteCommand(Player player, String[] args) {
        // Get the player's selected VivDisplay
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());

        // If the player has not selected a VivDisplay, send an error message and return
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }

        // Get the player's current location as the new location for the copied VivDisplay
        Location newLocation = player.getLocation();

        // Copy and paste the hierarchy of the selected VivDisplay at the new location
        DisplayGroupHandler.copyAndPasteHierarchy(selectedVivDisplay, player, newLocation);

        // Send a success message to the player
        CommandHandler.sendPlayerMsgIfMsg(player, displayGroupPasteSuccessMsg);
    }

    // Sets the parent of the (player)'s selected VivDisplay.
    public static void handleDisplayGroupSetParentCommand(Player player, String[] args) {
        if (args.length < 2) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayGroupSetParentUsageErr);
            return;
        }
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }
        // Find the parent display by name
        String parentName = args[1]; // Get the parent display name
        Display parentDisplay = DisplayHandler.getDisplayByName(player, parentName);
        if (parentDisplay == null) {
            if (!displayGroupSetParentNoParentErr.isEmpty()) {
                CommandHandler.sendPlayerMsgIfMsg(player, displayGroupSetParentNoParentErr.replace("$displayName", parentName));
            }
            return;
        }

        // Check for recursive parent before setting
        if (isRecursiveParent(selectedVivDisplay, parentDisplay)) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayGroupSetParentRecursiveErr);
            return;
        }

        player.sendMessage(selectedVivDisplay.setParent(parentDisplay));
    }

    private static boolean isRecursiveParent(VivDisplay vivDisplay, Display potentialParent) {
        if (vivDisplay == null || potentialParent == null) {
            return false;
        }

        VivDisplay highestParent = DisplayGroupHandler.getHighestVivDisplay(vivDisplay);
        return highestParent != null && highestParent.display.equals(potentialParent);
    }

    // Unsets the parent of the (player)'s selected VivDisplay.
    static void handleDisplayGroupUnparentCommand(Player player) {
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());

        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }

        player.sendMessage(selectedVivDisplay.unsetParent());
    }

    /**
     * Handles the rotate command for a player's selected VivDisplay group.
     *
     * @param player The player issuing the command.
     * @param args   Command arguments:
     *               - /displaygroup rotate <xRotation> <yRotation> <zRotation>
     */
    public static void handleDisplayGroupRotateCommand(Player player, String[] args) {
        if (!Config.config.getBoolean("doDisplayGroupRotation")) return;

        // Check if the correct number of arguments are provided
        if (args.length != 4) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayGroupRotateUsageErr);
            return;
        }

        // Parse the rotation from the arguments
        double pitch, yaw, roll;
        try {
            pitch = Double.parseDouble(args[1]);
            yaw = Double.parseDouble(args[2]);
            roll = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayGroupInvalidRotationErr);
            return;
        }

        // Get the player's selected VivDisplay
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());

        // If the player has not selected a VivDisplay, send an error message and return
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }

        // yaw, pitch, roll

        // Rotate the hierarchy of the selected VivDisplay
        DisplayGroupHandler.rotateHierarchy(selectedVivDisplay, yaw);

        // Send a success message to the player
        CommandHandler.sendPlayerMsgIfMsg(player, displayGroupRotateSuccessMsg);
    }

    /**
     * Handles the translate command for a player's selected VivDisplay group.
     *
     * @param player The player issuing the command.
     * @param args   Command arguments:
     *               - /displaygroup translate <xTranslation> <yTranslation> <zTranslation>
     */
    public static void handleDisplayGroupTranslateCommand(Player player, String[] args) {
        // mise en place
        if (args.length != 4) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayGroupTranslateUsageErr);
            return;
        }

        // Parse translation from args
        Vector translation = CommandParsing.parseVectorArgs(args, 1);
        if (translation == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayGroupTranslateInvalidTranslationErr);
            return;
        }

        // ensure selected VivDisplay
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }

        // actually do things
        DisplayGroupHandler.translateHierarchy(selectedVivDisplay, translation.toVector3d());
        CommandHandler.sendPlayerMsgIfMsg(player, displayGroupTranslateSuccessMsg);
    }

    public static void handleDisplayGroupShowCommand(Player player, String[] args) {
        if (!Config.config.getBoolean("doDisplayGroupShow")) return;
        // Check if the correct number of arguments is provided
        if (args.length != 1) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayGroupShowUsageErr);
            return;
        }

        // Get the player's selected VivDisplay by name
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());

        // Check if the selected VivDisplay exists
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }

        List<VivDisplay> hierarchy = DisplayGroupHandler.getAllDescendants(selectedVivDisplay);
        if (hierarchy == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, hierarchyIsNullErr);
            return;
        }

        Inventory inventory = GUIBuilder.displaySelectorGUIBuilder(hierarchy, displayGroupShowGUITitle);
        player.openInventory(inventory);

        // Send a success message to the player
        CommandHandler.sendPlayerMsgIfMsg(player, displayGroupShowSuccessMsg);
    }
}
