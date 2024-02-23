package me.vivian.displayer.commands;

import me.vivian.displayer.ParticleHandler;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.GUIHandler;
import me.vivian.displayerutils.TransformMath;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.ItemManipulation;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DisplayCommands {

    static Map<String, String> errMap = Texts.getErrors();

    // Sends detailed info about the selected display to the (player). Mostly for debug purposes
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
        if (selectedVivDisplay.isChild) {
            player.sendMessage("Parent UUID: " + selectedVivDisplay.parentUUID);
        }
        player.sendMessage("Is Parent: " + selectedVivDisplay.isThisParent());
    }

    /**
     * writes an awful, technical, /help message
     *
     * @param player the player to send the help messages to
     */
    static void handleDisplayHelpCommand(Player player) { // todo: EW AAAAA GROSS EW NO
        player.sendMessage("Displayer Help:");

        Map<String, Map<String, Object>> commands = CommandHandler.pluginDesc.getCommands();

        for (Map.Entry<String, Map<String, Object>> entry : commands.entrySet()) {
            String command = entry.getKey();
            Map<String, Object> commandInfo = entry.getValue();

            // Check if the command has a description in the plugin.yml
            if (commandInfo.containsKey("description")) {
                String description = (String) commandInfo.get("description");
                player.sendMessage("§6/" + command + "§r - §7" + description);
            } else {
                // If no description is found, just list the command
                player.sendMessage("§6/" + command);
            }

            if (!commandInfo.containsKey("subcommands")) {
                continue; // don't check subcommands if they aren't real
            }

            Map<String, Map<String, Object>> subcommands = (Map<String, Map<String, Object>>) commandInfo.get("subcommands");
            for (Map.Entry<String, Map<String, Object>> subcommandEntry : subcommands.entrySet()) {
                String subcommand = subcommandEntry.getKey();
                Map<String, Object> subcommandInfo = subcommandEntry.getValue();

                // Check if the subcommand has a description in the plugin.yml
                if (subcommandInfo.containsKey("description")) {
                    String subcommandDescription = (String) subcommandInfo.get("description");
                    player.sendMessage("§6/" + command + " §b" + subcommand + "§r - §7" + subcommandDescription);
                } else {
                    // If no description is found, just list the subcommand
                    player.sendMessage("§6/" + command + " §b" + subcommand);
                }
            }
        }
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
            player.sendMessage(errMap.get("advDisplayRenameUsage"));
            return;
        }
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedDisplayIfExists(player);
        if (selectedVivDisplay == null) {return;}

        String name = args[1]; // Get the name to set
        CommandHandler.sendPlayerMessageIfExists(player, selectedVivDisplay.rename(name));
    }

    static void handleDisplayDestroyCommand(Player player, String[] args) {
        if (args.length < 2) {
            DisplayHandler.destroySelectedDisplay(player);
            return;
        }

        if (!args[1].equalsIgnoreCase("nearby")) {
            player.sendMessage(errMap.get("advDisplayDestroyUsage"));
            return;
        }

        DisplayHandler.destroyNearbyDisplays(player, args);
    }

    static void handleDisplayCreateCommand(Player player, String[] args) {
        boolean isBlock = args.length >= 2 && Objects.equals(args[1], "block");
        boolean atSelected = args.length >= 3 && args[2].equalsIgnoreCase("atselected");

        // todo: where put this when it just kinda figured itself out on errs? player.sendMessage(errMap.get("displayCreateUsage"));

        if (!ItemManipulation.isHeldItemValid(player)) {
            player.sendMessage(errMap.get("displayCreateEmptyHand"));
            return;
        }

        if (atSelected && CommandHandler.selectedVivDisplays.get(player) == null) {
            player.sendMessage(errMap.get("noSelectedDisplay"));
            return;
        }


        if (isBlock) {
            DisplayHandler.createBlockDisplay(player, args);
        } else {
            DisplayHandler.createItemDisplay(player, args);
        }

        // todo: check for creative mode before taking shit, also whatever perms are good enough for that idk
        ItemManipulation.takeFromHeldItem(player);
    }

    /**
     * finds nearby VivDisplay objects within a given radius,
     * sends messages to the (player) with hyperlinks to each
     *
     * @param player The player who issued the command.
     * @param args   Command arguments:
     *               - args[1]: (Optional) The radius within which to search for VivDisplays. Defaults to 5
     */
    static void handleDisplayNearbyCommand(Player player, String[] args) {
        double radius = Parsing.parseNumberFromArgs(args, 1, 1, 5, player, "Invalid radius specified.");

        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(player, (int) radius);

        if (nearbyVivDisplays.isEmpty()) {
            // errs in func
            return;
        }

        player.sendMessage(errMap.get("displayNearbyTitle"));
        int maxDisplaysToShow = 10;
        for (int index = 0; index < maxDisplaysToShow && index < nearbyVivDisplays.size(); index++) {
            createHyperlink(player, nearbyVivDisplays.get(index), index + 1);
        }
    }

    // Selects the closest VivDisplay to the (player)'s location within a specified radius.
    static void handleDisplayClosestCommand(Player player) {
        int radius = 5;
        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(player, radius);

        if (nearbyVivDisplays.isEmpty()) {
            // errs in func
            return;
        }

        VivDisplay closestVivDisplay = nearbyVivDisplays.get(0);
        CommandHandler.selectedVivDisplays.put(player, closestVivDisplay);
        ParticleHandler.spawnParticle(closestVivDisplay.display, null, null);
        player.sendMessage(errMap.get("displayClosestSuccess"));
    }

    // Selects a VivDisplay for the (player) given an index.
    static void handleAdvDisplaySelectCommand(Player player, String[] args) { // this should never be executed by the player
        // todo: early return if player
        if (args.length < 2) {
            return;
        }

        // if index specified
        int index = Integer.parseInt(args[1]);
        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(player, 5);

        if (index < 1 || index > nearbyVivDisplays.size()) {
            return;
        }

        // if index not oob
        VivDisplay selectedVivDisplay = nearbyVivDisplays.get(index - 1);
        CommandHandler.selectedVivDisplays.put(player, selectedVivDisplay);
        ParticleHandler.spawnParticle(selectedVivDisplay.display, null, null);

        // open gui if selecting from here
        player.performCommand("display gui");
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
            if (isChange) {
                player.sendMessage(errMap.get("advDisplayChangeRotationUsage"));
            } else {
                player.sendMessage(errMap.get("advDisplaySetRotationUsage"));
            }
            return;
        }

        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedDisplayIfExists(player);
        if (selectedVivDisplay == null) {return;}

        float[] rotationOffsets = Parsing.parseRotationOffsets(player, args);
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
            if (isChange) {
                player.sendMessage(errMap.get("advDisplayChangePositionUsage"));
            } else {
                player.sendMessage(errMap.get("advDisplaySetPositionUsage"));
            }
            return;
        }

        double[] positionOffsets = Parsing.parsePositionOffsets(args, player);
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
        if (selectedVivDisplay == null) {return;}

        Transformation transformation = selectedVivDisplay.display.getTransformation();
        double currentSize = transformation.getScale().x;
        double minSize = isChange ? -currentSize : 0.0;
        double sizeArg = Parsing.parseNumberFromArgs(args, 1, minSize, minSize + 1, player, errorMessage);

        if (sizeArg < minSize) {
            // player.sendMessage(errorMessage);
            // will err in parsing iirc?
            // todo: ensure ^
            return;
        }

        double newScale = isChange ? (currentSize + sizeArg) : sizeArg;

        if (newScale > 0.0) {
            transformation.getScale().set(newScale);
            selectedVivDisplay.display.setTransformation(transformation);
        } else {
            player.sendMessage(errorMessage);
        }
    }

    // Creates & opens the display-editing inventory-GUI for a (player) with buttons for adjusting position, rotation, and size.
    static void handleDisplayGUICommand(Player player) {
        Inventory inventory = GUIHandler.displayGUIBuilder();
        player.openInventory(inventory);
    }

    // Sends (player) a hyperlink to select a (vivDisplay) with a given (index)
    public static void createHyperlink(Player player, VivDisplay vivDisplay, int index) {
        assert vivDisplay != null;

        Location location = vivDisplay.display.getLocation();
        Location playerLocation = player.getLocation();

        // get distance rounded to 2 places
        double distance = TransformMath.roundTo(location.distance(playerLocation), 2);

        String name = CommandHandler.nbtm.getNBT(vivDisplay.display, "VivDisplayName", String.class);
        if (name == null) name = "";

        Material displayMaterial;
        String displayTypeStr;

        // get material & type of display
        if (vivDisplay.display instanceof BlockDisplay) {
            displayMaterial = ((BlockDisplay) vivDisplay.display).getBlock().getMaterial();
            displayTypeStr = errMap.get("displayNearbyHyperlink_BlockDisplayDisplayText");
        } else if (vivDisplay.display instanceof ItemDisplay) {
            ItemStack itemStack = ((ItemDisplay) vivDisplay.display).getItemStack();
            assert itemStack != null;
            displayMaterial = itemStack.getType();
            displayTypeStr = errMap.get("displayNearbyHyperlink_ItemDisplayDisplayText");
        } else {
            displayMaterial = Material.AIR;
            displayTypeStr = errMap.get("displayNearbyHyperlink_UnknownDisplayDisplayText");
            System.out.println(errMap.get("displayNearbyFoundUnknownItem"));
            return; // Exit early if display is borked
        }

        // create & send message to select this display, if it's not borked
        String hyperLinkText = errMap.get("displayNearbyHyperlinkText");
        hyperLinkText = hyperLinkText.replace("$DisplayTypeDisplayText", displayTypeStr);
        hyperLinkText = hyperLinkText.replace("$DisplayName", name);
        hyperLinkText = hyperLinkText.replace("$DisplayMaterial", displayMaterial.toString());
        hyperLinkText = hyperLinkText.replace("$Distance", distance+"");

        TextComponent message = new TextComponent(hyperLinkText);

        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/advdisplay select " + index));

        player.spigot().sendMessage(message);
    }
}
