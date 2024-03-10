package me.vivian.displayer.commands;

import me.vivian.displayer.config.Texts;
import me.vivian.displayerutils.GUIBuilder;
import me.vivian.displayer.display.DisplayGroupHandler;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

public class DisplayGroupCommands {
    static Map<String, String> errMap = Texts.getErrors();
    static Map<String, String> msgMap = Texts.getMessages();

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
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
            return;
        }

        // Get the player's current location as the new location for the copied VivDisplay
        Location newLocation = player.getLocation();

        // Copy and paste the hierarchy of the selected VivDisplay at the new location
        DisplayGroupHandler.copyAndPasteHierarchy(selectedVivDisplay, player, newLocation);

        // Send a success message to the player
        CommandHandler.sendPlayerMsgIfMsg(player, msgMap.get("displayGroupPasteSuccess"));
    }

    // Sets the parent of the (player)'s selected VivDisplay.
    public static void handleDisplayGroupSetParentCommand(Player player, String[] args) {
        if (args.length < 2) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayGroupSetParentUsage"));
            return;
        }

        String parentName = args[1]; // Get the parent display name to set

        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);

        // Find the parent display by name
        Display parentDisplay = DisplayHandler.getDisplayByName(player, parentName);
        if (parentDisplay == null) {
            if (!errMap.get("displayGroupSetParentNoParent_Begin").isEmpty() || !errMap.get("displayGroupSetParentNoParent_End").isEmpty()) {
                CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayGroupSetParentNoParent_Begin") + parentName + errMap.get("displayGroupSetParentNoParent_End"));
            }
            return;
        }

        player.sendMessage(selectedVivDisplay.setParent(parentDisplay));
    }

    // Unsets the parent of the (player)'s selected VivDisplay.
    static void handleDisplayGroupUnparentCommand(Player player) {
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = CommandHandler.selectedVivDisplays.get(player);

        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
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
        // Check if the correct number of arguments are provided
        if (args.length != 4) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayGroupRotateUsage"));
            return;
        }

        // Parse the rotation from the arguments
        double pitch, yaw, roll;
        try {
            pitch = Double.parseDouble(args[1]);
            yaw = Double.parseDouble(args[2]);
            roll = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayGroupInvalidRotation"));
            return;
        }

        // Get the player's selected VivDisplay
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);

        // If the player has not selected a VivDisplay, send an error message and return
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
            return;
        }

        // yaw, pitch, roll

        // Rotate the hierarchy of the selected VivDisplay
        DisplayGroupHandler.rotateHierarchy(selectedVivDisplay, new Vector3d(roll, yaw, pitch));

        // Send a success message to the player
        CommandHandler.sendPlayerMsgIfMsg(player, msgMap.get("displayGroupRotateSuccess"));
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
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayGroupTranslateUsage"));
            return;
        }

        // Parse the translation from the arguments
        double xTranslation, yTranslation, zTranslation;
        try {
            xTranslation = Double.parseDouble(args[1]);
            yTranslation = Double.parseDouble(args[2]);
            zTranslation = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayGroupTranslateInvalidTranslation"));
            return;
        }

        // Get the player's selected VivDisplay
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);

        // If the player has not selected a VivDisplay, send an error message and return
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
            return;
        }

        // Translate the hierarchy of the selected VivDisplay
        DisplayGroupHandler.translateHierarchy(selectedVivDisplay, new Vector3d(xTranslation, yTranslation, zTranslation));

        // Send a success message to the player
        CommandHandler.sendPlayerMsgIfMsg(player, msgMap.get("displayGroupTranslateSuccess"));
    }

    public static void handleDisplayGroupShowCommand(Player player, String[] args) {
        // Check if the correct number of arguments is provided
        if (args.length != 1) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayGroupShowUsage"));
            return;
        }

        // Get the player's selected VivDisplay by name
        VivDisplay selectedVivDisplay = CommandHandler.selectedVivDisplays.get(player);

        // Check if the selected VivDisplay exists
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
            return;
        }

        List<VivDisplay> hierarchy = DisplayGroupHandler.getAllDisplaysInHierarchy(selectedVivDisplay);
        if (hierarchy == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("hierarchyIsNull"));
            return;
        }

        Inventory inventory = GUIBuilder.displaySelectorGUIBuilder(hierarchy, Texts.getText("displayGroupShowGUITitle"), false);
        player.openInventory(inventory);

        // Send a success message to the player
        // todo: lel?
        CommandHandler.sendPlayerMsgIfMsg(player, msgMap.get("displayGroupShowSuccess"));
    }
}
