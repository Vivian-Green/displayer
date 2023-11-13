package me.vivian.displayer;

import me.vivian.displayerutils.ItemManipulation;
import me.vivian.displayerutils.NBTMagic;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.Transformation;
import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.*;

// handles commands for creating, manipulating, and interacting with displays
public class DisplayCommands implements CommandExecutor {
    private PluginDescriptionFile pluginDesc;
    private Plugin plugin;

    public DisplayCommands(Plugin thisPlugin) {
        plugin = thisPlugin;
        pluginDesc = plugin.getDescription();
        nbtm = new NBTMagic(plugin);
    }
    ItemManipulation im = new ItemManipulation();
    NBTMagic nbtm;

    private static final Map<String, VivDisplay> vivDisplays = new HashMap<>();
    private static final Map<Player, VivDisplay> selectedVivDisplays = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            boolean isPlayer = false;
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "destroy":
                    Player player = Bukkit.getPlayer(args[4]);
                    ///display destroy nearby 10 10 GreensUsername
                    handleDisplayDestroyCommand(player, args);
                    break;
            }
            return true;
        }
        boolean isPlayer = true;
        Player player = (Player) sender;

        if (label.equalsIgnoreCase("display") || label.equalsIgnoreCase("displayer")) {
            if (args.length < 1) {
                player.sendMessage("Usage: /display create | nearby [radius] | destroy [nearby] [maxCount] [radius] | gui | help");
                return false;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "create":
                    handleDisplayCreateCommand(player, args);
                    break;
                case "closest":
                    handleDisplayClosestCommand(player);
                    break;
                case "nearby":
                    handleDisplayNearbyCommand(player, args);
                    break;
                case "gui":
                    handleDisplayGUICommand(player);
                    break;
                case "destroy":
                    handleDisplayDestroyCommand(player, args);
                    break;
                case "help":
                    handleDisplayHelpCommand(player);
                    break;
                default:
                    player.sendMessage("Invalid subcommand. Try /display help");
            }
        } else if (label.equalsIgnoreCase("advdisplay")) {
            if (args.length < 1) {
                player.sendMessage("Usage: /advdisplay select <index> | setrotation <yaw> <pitch> | changerotation <yawOffset> <pitchOffset> | setposition <x> <y> <z> | changeposition <xOffset> <yOffset> <zOffset> | setsize <size> | changesize <sizeOffset> | rename <name>");
                return false;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "select":
                    handleDisplaySelectCommand(player, args);
                    break;
                case "setrotation":
                case "changerotation":
                    handleDisplayRotationCommand(player, args);
                    break;
                case "setposition":
                case "changeposition":
                    handleDisplayPositionCommand(player, args);
                    break;
                case "setsize":
                case "changesize":
                    handleDisplaySizeCommand(player, args);
                    break;
                case "rename":
                    handleDisplayRenameCommand(player, args);
                    break;
                case "details":
                    handleDisplayDetailsCommand(player);
                    break;
                default:
                    player.sendMessage("Invalid subcommand for /advdisplay. Try /advdisplay help");
            }
        } else if (label.equalsIgnoreCase("displaygroup")) {
            if (args.length < 1) {
                player.sendMessage("Usage: /displaygroup setparent <parentname>");
                return false;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "parent":
                    handleDisplaySetParentCommand(player, args);
                    break;
                case "unparent":
                    handleDisplayUnparentCommand(player);
                    break;
                case "copypaste":
                    handleDisplayCopyPasteCommand(player, args);
                    break;
                case "rotate":
                    handleDisplayGroupRotateCommand(player, args);
                    break;
                case "translate":
                    handleDisplayGroupTranslateCommand(player, args);
                    break;
                case "show":
                    handleDisplayGroupShowCommand(player, args);
                    break;
                default:
                    player.sendMessage("Invalid subcommand for /displaygroup. Try /displaygroup help");
            }
        }
        return true;
    }

    // Sends detailed info about the selected display to the (player). Mostly for debug purposes
    private void handleDisplayDetailsCommand(Player player) {
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = selectedVivDisplays.get(player);

        if (selectedVivDisplay == null) {
            player.sendMessage("You haven't selected a display.");
            return;
        }

        // Get display information directly from the selected VivDisplay
        Location displayLocation = locationRoundedTo(selectedVivDisplay.display.getLocation(), 2);
        double currentYaw = roundTo(displayLocation.getYaw(), 2);
        double currentPitch = roundTo(displayLocation.getPitch(), 2);
        double currentRoll = roundTo(getTransRoll(selectedVivDisplay.display.getTransformation()), 2);

        // Send the details to the player
        player.sendMessage("Display Name: " + selectedVivDisplay.displayName);
        player.sendMessage("Display Material: " + selectedVivDisplay.getItemStack().getType());
        player.sendMessage("Display Position: X=" + displayLocation.getX() + " Y=" + displayLocation.getY() + " Z=" + displayLocation.getZ());
        player.sendMessage("Display Rotation: Yaw=" + currentYaw + " Pitch=" + currentPitch + " Roll=" + currentRoll);
        player.sendMessage("Display Size: " + roundTo(selectedVivDisplay.display.getTransformation().getScale().x, 2));
        player.sendMessage("Distance to Display: " + roundTo(player.getLocation().distance(displayLocation), 2));

        // Send NBT data related to parent and child
        if (selectedVivDisplay.isChild) {
            player.sendMessage("Parent UUID: " + selectedVivDisplay.parentUUID);
        }
        player.sendMessage("Is Parent: " + selectedVivDisplay.isThisParent());
    }

    // Sets the parent of the (player)'s selected VivDisplay.
    private void handleDisplaySetParentCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /displaygroup setparent <parentname>");
            return;
        }

        String parentName = args[1]; // Get the parent display name to set

        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = getSelectedVivDisplay(player);

        // Find the parent display by name
        Display parentDisplay = getVivDisplayByName(player, parentName);
        if (parentDisplay == null) {
            player.sendMessage("No nearby display with the name '" + parentName + "' found.");
            return;
        }

        sendPlayerMessageIfExists(player, selectedVivDisplay.setParent(parentDisplay));
    }

    // Unsets the parent of the (player)'s selected VivDisplay.
    private void handleDisplayUnparentCommand(Player player) {
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = selectedVivDisplays.get(player);

        if (selectedVivDisplay == null) {
            player.sendMessage("You haven't selected a display to unparent.");
            return;
        }

        sendPlayerMessageIfExists(player, selectedVivDisplay.unsetParent());
    }

    /**
     * Handles the renaming of a (player)'s selected display by adding a custom NBT tag with a given arg name
     *
     * @param player The player who issued the command.
     * @param args   Command arguments:
     *              - /advdisplay rename <name>
     */
    private void handleDisplayRenameCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /advdisplay rename <name>");
            return;
        }
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = selectedVivDisplays.get(player);

        String name = args[1]; // Get the name to set
        sendPlayerMessageIfExists(player, selectedVivDisplay.rename(name));
    }

    /**
     * writes an awful, technical, /help message
     *
     * @param player the player to send the help messages to
     */
    private void handleDisplayHelpCommand(Player player) {
        player.sendMessage("Displayer Help:");

        Map<String, Map<String, Object>> commands = pluginDesc.getCommands();

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
     * Parses a double from the specified (index) of (args) and ensures
     * it is >= a given (minValue), defaulting to (defaultValue)
     *
     * @param args          The string array containing the arguments to parse.
     * @param index         The index at which to parse the number.
     * @param minValue      The minimum value that the parsed number must be greater than or equal to.
     * @param defaultValue  The default value to return if the index is out of bounds.
     * @param player        The player to whom the error message should be sent.
     * @param errorMessage  The error message to send if parsing fails or the value is invalid.
     * @return              The parsed number, or the default value if parsing fails or the value is invalid.
     */
    private double parseNumberFromArgs(String[] args, int index, double minValue, double defaultValue, Player player, String errorMessage) {
        if (args.length <= index) {
            return defaultValue;
        }

        try {
            double value = Double.parseDouble(args[index]);
            if (value < minValue) {
                player.sendMessage(errorMessage);
                return defaultValue; // Return the default value
            }
            return value;
        } catch (NumberFormatException e) {
            player.sendMessage(errorMessage);
            return defaultValue; // Return the default value
        }
    }

    /**
     * Handles the destruction of displays for a (player)
     *
     * @param player The player issuing the command.
     * @param args   Command arguments:
     *              - For destroying the selected display: /display destroy
     *              - For destroying nearby displays: /display destroy nearby [max count] [radius]
     *                - max count: The maximum number of nearby displays to destroy (default is 1).
     *                - radius: The radius to search for nearby displays (default is 5 blocks).
     */
    private void handleDisplayDestroyCommand(Player player, String[] args) {
        if (args.length < 2) {
            // Destroy the selected display
            VivDisplay selectedVivDisplay = selectedVivDisplays.get(player);
            if (selectedVivDisplay == null) {
                player.sendMessage("You must first select a Display to destroy.");
            } else {
                selectedVivDisplay.destroy(player, vivDisplays, selectedVivDisplays);
            }
            return;
        }

        if (!args[1].equalsIgnoreCase("nearby")) {
            player.sendMessage("Usage: /display destroy [nearby [max count] [radius]]");
            return;
        }

        int maxCount = (int) parseNumberFromArgs(args, 2, 0, 1, player, "Invalid max count"); // default max count to 1
        double radius = parseNumberFromArgs(args, 3, 0.0, 5.0, player, "Invalid radius"); // default radius to 5

        if (maxCount <= 0 || radius <= 0.0) {
            return; // Invalid max count or radius, error message already sent in parsing functions
        }

        List<VivDisplay> nearbyVivDisplays = getNearbyVivDisplays(player, (int) radius);

        if (nearbyVivDisplays.isEmpty()) {
            player.sendMessage("No nearby Displays found within " + radius + " blocks.");
            return;
        }

        // Destroy nearby displays up to the specified max count
        int destroyedCount = 0;
        for (VivDisplay vivDisplay : nearbyVivDisplays) {
            vivDisplay.destroy(player, vivDisplays, selectedVivDisplays);
            destroyedCount++;

            if (destroyedCount >= maxCount) {
                return;
            }
        }
    }

    /**
     * changes (+=) or sets (=) the (player)'s selected VivDisplay's size.
     *
     * @param player The player performing the command.
     * @param args   Command arguments:
     *              - For changing the size: /display changesize <size offset: x y z>
     *              - For setting the size: /display setsize <size: x y x>
     */
    private void handleDisplaySizeCommand(Player player, String[] args) {
        boolean isChange = args.length > 0 && "changesize".equalsIgnoreCase(args[0]);

        String errorMessage = isChange ?
                "Invalid size offset value. Usage: /display changesize <size offset: number>" :
                "Invalid size value. Usage: /display setsize <size: number>";

        VivDisplay selectedVivDisplay = selectedVivDisplays.get(player);

        if (selectedVivDisplay == null) {
            player.sendMessage("You must first select a Display");
            return;
        }

        Transformation transformation = selectedVivDisplay.display.getTransformation();
        double currentSize = transformation.getScale().x;
        double minSize = isChange ? -currentSize : 0.0;
        double sizeArg = parseNumberFromArgs(args, 1, minSize, minSize+1, player, errorMessage);

        if (sizeArg < minSize) {
            player.sendMessage("Invalid value for size");
            return;
        }

        double newScale = isChange ? (currentSize + sizeArg) : sizeArg;

        if (newScale > 0.0) {
            transformation.getScale().set(newScale);
            selectedVivDisplay.display.setTransformation(transformation);
        } else {
            player.sendMessage("Invalid size value. Size must be > 0");
        }
    }



    // Reduces the count of the (player)'s held item by 1. If the new count <= 0, the remove it.
    private void takeFromHeldItem(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        heldItem.setAmount(heldItem.getAmount() - 1);
        if (heldItem.getAmount() <= 0) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            player.getInventory().setItemInMainHand(heldItem);
        }
    }

    // Checks if a (player) is holding a displayable item
    private boolean isHeldItemValid(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            player.sendMessage("You must be holding an item or block to create a display.");
            return false;
        }
        return true;
    }

    /**
     * creates a VivDisplay for a (player), optionally, at the currently selected display
     *
     * @param player The player creating the VivDisplay.
     * @param args   Command arguments:
     *               - /display create [block (defaults to item)] [atSelected]
     */
    private void handleDisplayCreateCommand(Player player, String[] args) {
        VivDisplay selectedVivDisplay = selectedVivDisplays.get(player);
        String displayTypeStr = "item";
        if (args.length >= 2 && Objects.equals(args[1], "block")) {
            displayTypeStr = "block";
        }
        boolean atSelected = (args.length >= 3 && args[2].equalsIgnoreCase("atselected"));

        if (atSelected && selectedVivDisplay == null) {
            player.sendMessage("You need to select a display first!");
            return;
        }
        if (!isHeldItemValid(player)) {
            player.sendMessage("You need to hold an item first!");
            return;
        }

        ItemStack heldItemStack = player.getInventory().getItemInMainHand();
        Location eyeLocation = player.getEyeLocation();
        World world = player.getWorld();
        VivDisplay vivDisplay;

        if (displayTypeStr.equals("block")) {
            if (!heldItemStack.getType().isBlock()) {
                player.sendMessage("Invalid block!");
                return;
            }
            BlockData blockData = heldItemStack.getType().createBlockData();
            vivDisplay = new VivDisplay(plugin, world, atSelected ? selectedVivDisplay.display.getLocation() : eyeLocation, EntityType.BLOCK_DISPLAY, blockData);
        } else {
            vivDisplay = new VivDisplay(plugin, world, atSelected ? selectedVivDisplay.display.getLocation() : eyeLocation, EntityType.ITEM_DISPLAY, heldItemStack);
        }

        takeFromHeldItem(player);

        if (atSelected) {
            vivDisplay.display.setTransformation(selectedVivDisplay.display.getTransformation());
        } else {
            vivDisplay.display.setRotation(eyeLocation.getYaw(), eyeLocation.getPitch());
        }

        vivDisplays.put(vivDisplay.display.getUniqueId().toString(), vivDisplay);
        selectedVivDisplays.put(player, vivDisplay);
    }

    /**
     * finds nearby VivDisplay objects within a given radius,
     * sends messages to the (player) with hyperlinks to each
     *
     * @param player The player who issued the command.
     * @param args   Command arguments:
     *               - args[1]: (Optional) The radius within which to search for VivDisplays. Defaults to 5
     */
    private void handleDisplayNearbyCommand(Player player, String[] args) {
        double radius = parseNumberFromArgs(args, 1, 1, 5, player, "Invalid radius specified.");

        List<VivDisplay> nearbyVivDisplays = getNearbyVivDisplays(player, (int)radius);

        if (nearbyVivDisplays.isEmpty()) {
            player.sendMessage("No nearby Displays found within " + (int)radius + " blocks.");
            return;
        }

        player.sendMessage("Nearby Displays:");
        int maxDisplaysToShow = 10;
        for (int index = 0; index < maxDisplaysToShow && index < nearbyVivDisplays.size(); index++) {
            createHyperlink(player, nearbyVivDisplays.get(index), index + 1);
        }
    }

    // Gets Displays near a (player) within in a given (radius)
    private List<Display> getNearbyDisplays(Player player, double radius) {
        double maxTaxicabDistance = Math.sqrt(3) * radius; // maximum taxicab distance
        Location playerLocation = player.getLocation();

        List<Display> allDisplays = (List<Display>) player.getWorld().getEntitiesByClass(Display.class);
        List<Display> nearbyDisplays = new ArrayList<>();
        for (Display display : allDisplays) {
            Location displayLocation = display.getLocation();

            double xDistance = Math.abs(playerLocation.getX() - displayLocation.getX());
            double yDistance = Math.abs(playerLocation.getY() - displayLocation.getY());
            double zDistance = Math.abs(playerLocation.getZ() - displayLocation.getZ());

            double totalDistance = xDistance + yDistance + zDistance;

            // do pythagorean after passing taxicab
            if (totalDistance <= maxTaxicabDistance && playerLocation.distance(displayLocation) <= radius) {
                nearbyDisplays.add(display);
            }
        }
        return nearbyDisplays;
    }

    // Gets VivDisplay objects near the (player) within a given (radius), sorted by distance
    private List<VivDisplay> getNearbyVivDisplays(Player player, int radius) {
        List<VivDisplay> nearbyVivDisplays = new ArrayList<>();
        List<Display> nearbyDisplays = getNearbyDisplays(player, radius);

        for (Display display : nearbyDisplays) {
            // Get the UUID of the display
            String displayUUID = String.valueOf(display.getUniqueId());

            // Check if it exists in the VivDisplays map
            if (!vivDisplays.containsKey(displayUUID)) {
                // Instantiate a new VivDisplay and add it to the list
                VivDisplay vivDisplay = new VivDisplay(plugin, display);
                nearbyVivDisplays.add(vivDisplay);

                // Add the newly created VivDisplay to the map with its UUID as the key
                vivDisplays.put(displayUUID, vivDisplay);
            }else{
                nearbyVivDisplays.add(vivDisplays.get(displayUUID));
            }
        }
        nearbyVivDisplays.sort(Comparator.comparingDouble(vivDisplay -> vivDisplay.display.getLocation().distance(player.getLocation())));

        return nearbyVivDisplays;
    }

    // Selects the closest VivDisplay to the (player)'s location within a specified radius.
    private void handleDisplayClosestCommand(Player player) {
        int radius = 5;
        List<VivDisplay> nearbyVivDisplays = getNearbyVivDisplays(player, radius);

        if (nearbyVivDisplays.isEmpty()) {
            player.sendMessage("No nearby Displays found within " + radius + " blocks.");
            return;
        }

        VivDisplay closestVivDisplay = nearbyVivDisplays.get(0);
        selectedVivDisplays.put(player, closestVivDisplay);
        closestVivDisplay.spawnParticle(null, null);
        player.sendMessage("Closest Display selected.");
    }

    // Selects a VivDisplay for the (player) given an index.
    private void handleDisplaySelectCommand(Player player, String[] args) { // this should never be executed by the player
        // todo: early return if player
        if (args.length < 2) {return;}

        // if index specified
        int index = Integer.parseInt(args[1]);
        List<VivDisplay> nearbyVivDisplays = getNearbyVivDisplays(player, 5);

        if (index < 1 || index > nearbyVivDisplays.size()) {return;}

        // if index not oob
        VivDisplay selectedVivDisplay = nearbyVivDisplays.get(index - 1);
        selectedVivDisplays.put(player, selectedVivDisplay);
        selectedVivDisplay.spawnParticle(null, null);

        // open gui if selecting from here
        player.performCommand("display gui");
    }

    /**
     * Rotates the selected VivDisplay for a player. Allows changing (+=) or setting (=) the rotation around 2 or 3 axis
     *
     * @param player The player performing the command.
     * @param args   Command arguments:
     *              - For changing the rotation: /display changerotation <yawOffset> <pitchOffset> [rollOffset]
     *              - For setting the rotation: /display setrotation <yawOffset> <pitchOffset> [rollOffset]
     */
    private void handleDisplayRotationCommand(Player player, String[] args) {
        boolean isChange = args.length > 0 && "changerotation".equalsIgnoreCase(args[0]);

        if (args.length < 3) {
            player.sendMessage("Usage: /display " + (isChange ? "changeposition" : "setposition") + " <pitchOffset> <yawOffset> [rollOffset]");
            return;
        }

        float yawOffset, pitchOffset, rollOffset = 0f;

        try {
            yawOffset = Float.parseFloat(args[1]);
            pitchOffset = Float.parseFloat(args[2]);

            if (args.length >= 4) {
                rollOffset = Float.parseFloat(args[3]);
            }
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid pitch, yaw, or roll offset values.");
            return;
        }

        VivDisplay selectedVivDisplay = selectedVivDisplays.get(player);

        if (selectedVivDisplay == null) {
            player.sendMessage("You must first select a Display");
            return;
        }

        boolean success = isChange ?
                selectedVivDisplay.changeRotation(yawOffset, pitchOffset, rollOffset, player) :
                selectedVivDisplay.setRotation(yawOffset, pitchOffset, rollOffset, player);

        if (!success) {
            player.sendMessage("Failed to apply rotation change.");
        }
    }

    /**
     * gets the roll in degrees from a given (transformation)'s right rotation component.
     *
     * @param transformation The transformation containing rotation information.
     * @return The roll angle in degrees.
     */
    public static float getTransRoll(Transformation transformation) {
        // Get the right rotation component
        Quaternionf rollRotation = transformation.getRightRotation();

        // Calculate the roll in degrees from the quaternion
        return (float) Math.toDegrees(2.0 * Math.atan2(rollRotation.x, rollRotation.w));
    }

    public void drawParticleLine(Location loc1, Location loc2, Particle particle, Integer count) {
        if (particle == null) {
            particle = Particle.DOLPHIN;
        }
        if (count == null) {
            count = 100;
        }

        World world = loc1.getWorld();
        Vector3d vector1 = new Vector3d(loc1.getX(), loc1.getY(), loc1.getZ());
        Vector3d vector2 = new Vector3d(loc2.getX(), loc2.getY(), loc2.getZ());
        // Vector3d vectorBetween = new Vector3d(vector2.x-vector1.x, vector2.y-vector1.y, vector2.z-vector1.z);

        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            double t = (double) i / (count - 1);

            Vector3d pointOnLine = vector1.lerp(vector2, t);
            Vector3d randomOffset = new Vector3d(-0.1 + (0.1 - (-0.1)) * rand.nextDouble(), -0.1 + (0.1 - (-0.1)) * rand.nextDouble(), -0.1 + (0.1 - (-0.1)) * rand.nextDouble());
            Location pointLocation = new Location(world, pointOnLine.x + randomOffset.x, pointOnLine.y + randomOffset.y, pointOnLine.z + randomOffset.z);


            world.spawnParticle(particle, pointLocation, 1);
        }
    }

    /**
     * Handles the positioning of a (player)'s selected VivDisplay
     * Allows changing or setting the position with optional offsets.
     *
     * @param player The player performing the command.
     * @param args   Command arguments:
     *              - For changing the position: /display changeposition <xOffset> <yOffset> <zOffset>
     *              - For setting the position: /display setposition <x> <y> <z>
     */
    private void handleDisplayPositionCommand(Player player, String[] args) {
        boolean isChange = args.length > 0 && "changeposition".equalsIgnoreCase(args[0]);

        if (args.length != 4) {
            player.sendMessage(isChange ?
                    "Usage: /display changeposition <xOffset> <yOffset> <zOffset>" :
                    "Usage: /display setposition <x> <y> <z>");
            return;
        }

        double x, y, z;

        try {
            x = Double.parseDouble(args[1]);
            y = Double.parseDouble(args[2]);
            z = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid coordinates. Please provide valid numbers for X, Y, and Z.");
            return;
        }

        VivDisplay selectedVivDisplay = selectedVivDisplays.get(player);

        if (selectedVivDisplay == null) {
            player.sendMessage("You must first select a Display");
            return;
        }

        Vector3d translation = new Vector3d(x, y, z);

        // Translate hierarchy if VivDisplay is a parent
        if (selectedVivDisplay.isThisParent()) {
            translateHierarchy(selectedVivDisplay, translation);
        } else {
            boolean success = isChange ?
                    selectedVivDisplay.changePosition(x, y, z) :
                    selectedVivDisplay.setPosition(x, y, z, player);

            if (!success) {
                player.sendMessage("Failed to apply position change.");
            }
        }
    }


    // Creates an ItemStack in the (inventory) with the specified (material) and (displayName) at the given (x, y) coordinates.
    private void createButtonAtXY(Inventory inventory, Material material, String displayName, int x, int y) {
        ItemStack button = new ItemStack(material);
        button = im.itemWithName(button, displayName);
        im.setInventoryItemXY(inventory, button, x, y);
    }

    // Creates & opens the display-editing inventory-GUI for a (player) with buttons for adjusting position, rotation, and size.
    private void handleDisplayGUICommand(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "display GUI");

        Material posButtonMaterial = Material.ORANGE_CONCRETE;
        Material rotButtonMaterial = Material.LIME_CONCRETE;
        Material sizeButtonMaterial = Material.LIGHT_BLUE_CONCRETE;

        // position buttons
        createButtonAtXY(inventory, posButtonMaterial, "+x", 1, 1);
        createButtonAtXY(inventory, posButtonMaterial, "-x", 1, 2);
        createButtonAtXY(inventory, posButtonMaterial, "+y", 2, 1);
        createButtonAtXY(inventory, posButtonMaterial, "-y", 2, 2);
        createButtonAtXY(inventory, posButtonMaterial, "+z", 3, 1);
        createButtonAtXY(inventory, posButtonMaterial, "-z", 3, 2);

        // rotation buttons
        createButtonAtXY(inventory, rotButtonMaterial, "+yaw", 4, 1);
        createButtonAtXY(inventory, rotButtonMaterial, "-yaw", 4, 2);
        createButtonAtXY(inventory, rotButtonMaterial, "+pitch", 5, 1);
        createButtonAtXY(inventory, rotButtonMaterial, "-pitch", 5, 2);
        createButtonAtXY(inventory, rotButtonMaterial, "+roll", 6, 1);
        createButtonAtXY(inventory, rotButtonMaterial, "-roll", 6, 2);

        // size buttons
        createButtonAtXY(inventory, sizeButtonMaterial, "+size", 7, 1);
        createButtonAtXY(inventory, sizeButtonMaterial, "-size", 7, 2);

        player.openInventory(inventory);
    }

    // rounds a double (num)'s position to (places)
    private double roundTo(double num, int places) {
        double mult = Math.pow(10, places);
        return Math.round(num * mult)/mult;
    }

    // rounds a (location)'s position to (places)
    private Location locationRoundedTo(Location location, int places) {
        double x = roundTo((float) location.getX(), places);
        double y = roundTo((float) location.getY(), places);
        double z = roundTo((float) location.getZ(), places);

        return new Location(location.getWorld(), x, y, z, location.getYaw(), location.getPitch());
    }

    //self-explanatory
    public static VivDisplay getSelectedVivDisplay(Player player) {
        VivDisplay selectedVivDisplay = selectedVivDisplays.get(player);
        if (selectedVivDisplay == null) {
            player.sendMessage("You haven't selected a display to set as a child.");
        }
        return selectedVivDisplay;
    }

    /**
     * Searches for a display with a specific name within a radius of 5 blocks around the player.
     *
     * @param player The player around whom to search for displays.
     * @param displayName The name of the display to search for.
     * @return The found Display object, or null if no display with the given name is found.
     */
    private Display getVivDisplayByName(Player player, String displayName) { //todo: is this supposed to return a display & not a VivDisplay?
        // Get the nearby displays within a radius of 5 blocks
        List<Display> nearbyDisplays = getNearbyDisplays(player, 5);

        // Find the first display with the specified "VivDisplayName" NBT tag equal to displayName
        for (Display display : nearbyDisplays) {
            String currentDisplayName = nbtm.getNBT(display, "VivDisplayName", String.class);
            if (currentDisplayName != null && currentDisplayName.equals(displayName)) {
                return display; // Found the display, return it
            }
        }

        return null; // No display found with the given name
    }

    // Sends a message to a player, but only if the message is not empty.
    void sendPlayerMessageIfExists(Player player, String message) {
        if (!message.isEmpty()) {
            player.sendMessage(message);
        }
    }

    // Sends (player) a hyperlink to select a (vivDisplay) with a given (index)
    private void createHyperlink(Player player, VivDisplay vivDisplay, int index) {
        assert vivDisplay != null;

        Location location = vivDisplay.display.getLocation();
        Location playerLocation = player.getLocation();

        // get distance rounded to 2 places
        double distance = roundTo(location.distance(playerLocation), 2);

        String name = nbtm.getNBT(vivDisplay.display, "VivDisplayName", String.class);
        if (name == null) {
            name = "";
        }

        Material displayMaterial;
        String displayTypeStr;

        // get material & type of display
        if (vivDisplay.display instanceof BlockDisplay) {
            displayMaterial = ((BlockDisplay) vivDisplay.display).getBlock().getMaterial();
            displayTypeStr = "[BlockDisplay]";
        } else if (vivDisplay.display instanceof ItemDisplay) {
            ItemStack itemStack = ((ItemDisplay) vivDisplay.display).getItemStack();
            assert itemStack != null;
            displayMaterial = itemStack.getType();
            displayTypeStr = "[ItemDisplay]";
        } else {
            displayMaterial = Material.AIR;
            displayTypeStr = "[???]";
            System.out.println("createHyperlink(): This boy ain't right. Was I passed a text display?");
            return; // Exit early if display is borked
        }

        // create & send message to select this display, if it's not borked
        TextComponent message = new TextComponent(ChatColor.BLUE + "Click to select " + displayTypeStr + " '" + name + "' holding " + displayMaterial.toString() + ", " + distance + " blocks away");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/advdisplay select " + index));

        player.spigot().sendMessage(message);
    }

    private static VivDisplay getHighestVivDisplay(VivDisplay vivDisplay) {
        if (vivDisplay == null){
            System.out.println("getHighestVivDisplay: vivDisplay is null");
        }else{
            System.out.println(vivDisplay.displayName);
        }

        int maxDepth = 25;
        while (maxDepth > 0 && vivDisplay != null && vivDisplay.parentUUID != null && vivDisplays.get(vivDisplay.parentUUID) != null) {
            // todo: check for recursive trees
            vivDisplay = vivDisplays.get(vivDisplay.parentUUID);
            maxDepth--;
        }
        if (maxDepth < 1) {
            // handle recursive group
            return vivDisplay;
        }

        return vivDisplay;
    }


    // Function to get all descendants of a VivDisplay
    private static List<VivDisplay> getAllDescendants(VivDisplay parentVivDisplay) {
        List<VivDisplay> descendants = new ArrayList<>();
        for (VivDisplay vivDisplay : vivDisplays.values()) {
            if (vivDisplay.parentUUID != null && vivDisplay.parentUUID.equals(parentVivDisplay.display.getUniqueId().toString())) {
                descendants.add(vivDisplay);
                descendants.addAll(getAllDescendants(vivDisplay));  // Recursive call
                System.out.println(vivDisplay.displayName);
            }
        }
        return descendants;
    }

    // Function to get all displays in a hierarchy
    public static List<VivDisplay> getAllDisplaysInHierarchy(VivDisplay vivDisplay) {
        if (vivDisplay == null){
            System.out.println("translateHierarchy: vivDisplay is null");
        }else{
            System.out.println(vivDisplay.displayName);
        }
        VivDisplay topmostParent = getHighestVivDisplay(vivDisplay);
        if (topmostParent == null){
            System.out.println("translateHierarchy: topmostParent is null");
        }else{
            System.out.println(topmostParent.displayName);
        }
        List<VivDisplay> hierarchy = getAllDescendants(topmostParent);
        hierarchy.add(vivDisplay);
        System.out.println(vivDisplay.displayName);
        return hierarchy;
    }

    // Function to copy a VivDisplay
    private VivDisplay copyVivDisplay(VivDisplay original) {
        // Create a new VivDisplay with the same properties as the original
        VivDisplay copy = new VivDisplay(original.plugin, original.display);
        copy.displayName = original.displayName;
        copy.isChild = original.isChild;
        copy.isParent = original.isThisParent();
        // Don't copy the parentUUID, because we'll set it when we paste the copy
        return copy;
    }

    // Function to paste a VivDisplay at a new location
    private void pasteVivDisplay(VivDisplay copy, Location newLocation) {
        // Translate the copy's position to the new location
        copy.setPosition(newLocation.getX(), newLocation.getY(), newLocation.getZ(), null);
        // If VivDisplay has a method to set rotation, you can set it here
        // copy.setRotation(newLocation.getYaw(), newLocation.getPitch());
        // Add the copy to the vivDisplays map
        vivDisplays.put(copy.display.getUniqueId().toString(), copy);
    }


    // Function to copy and paste all displays in a hierarchy
    public void copyAndPasteHierarchy(VivDisplay vivDisplay, Player player, Location newLocation) {
        // Record the player's selected display before copying
        VivDisplay originalSelectedDisplay = selectedVivDisplays.get(player);

        // Get all displays in the hierarchy
        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);

        // Create a map to store the copies of each VivDisplay
        Map<String, VivDisplay> copies = new HashMap<>();

        // Copy all VivDisplays in the hierarchy
        for (VivDisplay original : hierarchy) {
            VivDisplay copy = copyVivDisplay(original);
            copies.put(original.display.getUniqueId().toString(), copy);
        }

        // Update the parentUUIDs of the copies
        for (VivDisplay original : hierarchy) {
            VivDisplay copy = copies.get(original.display.getUniqueId().toString());
            if (original.parentUUID != null) {
                VivDisplay parentCopy = copies.get(original.parentUUID);
                copy.parentUUID = parentCopy.display.getUniqueId().toString();
            }
        }

        // Calculate the translation vector
        VivDisplay topmostParent = getHighestVivDisplay(vivDisplay);
        Vector3d translation = new Vector3d(newLocation.getX() - topmostParent.display.getLocation().getX(),
                newLocation.getY() - topmostParent.display.getLocation().getY(),
                newLocation.getZ() - topmostParent.display.getLocation().getZ());

        // Paste all copies at the new location
        for (VivDisplay copy : copies.values()) {
            Vector3d copyPosition = new Vector3d(copy.display.getLocation().getX() + translation.x,
                    copy.display.getLocation().getY() + translation.y,
                    copy.display.getLocation().getZ() + translation.z);
            pasteVivDisplay(copy, new Location(copy.display.getLocation().getWorld(),
                    copyPosition.x, copyPosition.y, copyPosition.z));
        }

        // Set the player's selected display back to what it was before copying
        selectedVivDisplays.put(player, originalSelectedDisplay);
    }

    /**
     * Handles the copy and paste command for a player's selected VivDisplay.
     *
     * @param player The player issuing the command.
     * @param args   Command arguments:
     *               - /display copypaste
     */
    public void handleDisplayCopyPasteCommand(Player player, String[] args) {
        // Get the player's selected VivDisplay
        VivDisplay selectedVivDisplay = getSelectedVivDisplay(player);

        // If the player has not selected a VivDisplay, send an error message and return
        if (selectedVivDisplay == null) {
            player.sendMessage("You must first select a Display");
            return;
        }

        // Get the player's current location as the new location for the copied VivDisplay
        Location newLocation = player.getLocation();

        // Copy and paste the hierarchy of the selected VivDisplay at the new location
        copyAndPasteHierarchy(selectedVivDisplay, player, newLocation);

        // Send a success message to the player
        player.sendMessage("Successfully copied and pasted the selected Display's hierarchy at your current location.");
    }

    // Function to rotate a VivDisplay around a point using degrees
    private static void rotateVivDisplayAroundPoint(VivDisplay vivDisplay, Vector3d point, Vector3d rotationDegrees) {
        // Convert degrees to radians
        //roll, yaw, pitch
        double xRotation = Math.toRadians(rotationDegrees.x);
        double yRotation = Math.toRadians(rotationDegrees.y);
        double zRotation = Math.toRadians(rotationDegrees.z);

        // Translate the VivDisplay's position so that the rotation point is at the origin
        Vector3d translatedPosition = new Vector3d(vivDisplay.display.getLocation().getX() - point.x,
                vivDisplay.display.getLocation().getY() - point.y,
                vivDisplay.display.getLocation().getZ() - point.z);

        // Calculate the rotation matrix
        // Matrix3d rotationMatrix = new Matrix3d().rotateXYZ(xRotation, yRotation, zRotation);
        Matrix3d rotationMatrix = new Matrix3d().rotateXYZ(xRotation, zRotation, -yRotation);

        // Apply the rotation matrix to the translated position
        Vector3d rotatedPosition = rotationMatrix.transform(translatedPosition);

        // Translate the rotated position back
        Vector3d newPosition = new Vector3d(rotatedPosition.x + point.x,
                rotatedPosition.y + point.y,
                rotatedPosition.z + point.z);

        // Set the VivDisplay's position and rotation
        vivDisplay.setPosition(newPosition.x, newPosition.y, newPosition.z, null);
        vivDisplay.changeRotation((float) rotationDegrees.x, (float) rotationDegrees.y, (float) rotationDegrees.z, null);
    }


    // Function to rotate all displays in a hierarchy
    public static void rotateHierarchy(VivDisplay vivDisplay, Vector3d rotation) {
        if (vivDisplay == null){
            System.out.println("translateHierarchy: vivDisplay is null");
        }else{
            System.out.println(vivDisplay.displayName);
        }
        // Get all displays in the hierarchy
        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);

        // Get the highest VivDisplay in the hierarchy
        VivDisplay highestVivDisplay = getHighestVivDisplay(vivDisplay);

        Location rotationCenter = highestVivDisplay.display.getLocation();
        Vector3d rotationCenterPos = new Vector3d(rotationCenter.getX(), rotationCenter.getY(), rotationCenter.getZ());
        // Rotate all VivDisplays in the hierarchy
        for (VivDisplay vivDisplayToRotate : hierarchy) {
            rotateVivDisplayAroundPoint(vivDisplayToRotate, rotationCenterPos, rotation);
        }
    }

    /**
     * Handles the rotate command for a player's selected VivDisplay group.
     *
     * @param player The player issuing the command.
     * @param args   Command arguments:
     *               - /displaygroup rotate <xRotation> <yRotation> <zRotation>
     */
    public void handleDisplayGroupRotateCommand(Player player, String[] args) {
        // Check if the correct number of arguments are provided
        if (args.length != 4) {
            player.sendMessage("Usage: /displaygroup rotate <xRotation> <yRotation> <zRotation>");
            return;
        }

        // Parse the rotation from the arguments
        double pitch, yaw, roll;
        try {
            pitch = Double.parseDouble(args[1]);
            yaw = Double.parseDouble(args[2]);
            roll = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid rotation. Please provide valid numbers for xRotation, yRotation, and zRotation.");
            return;
        }

        // Get the player's selected VivDisplay
        VivDisplay selectedVivDisplay = getSelectedVivDisplay(player);

        // If the player has not selected a VivDisplay, send an error message and return
        if (selectedVivDisplay == null) {
            player.sendMessage("You must first select a Display");
            return;
        }

        // yaw, pitch, roll

        // Rotate the hierarchy of the selected VivDisplay
        rotateHierarchy(selectedVivDisplay, new Vector3d(roll, yaw, pitch));

        // Send a success message to the player
        player.sendMessage("Successfully rotated the selected Display's hierarchy.");
    }

    // Function to translate all displays in a hierarchy
    public static void translateHierarchy(VivDisplay vivDisplay, Vector3d translation) {
        // Get all displays in the hierarchy
        if (vivDisplay == null){
            System.out.println("translateHierarchy: vivDisplay is null");
        }else{
            System.out.println(vivDisplay.displayName);
        }
        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);

        // Translate all VivDisplays in the hierarchy
        for (VivDisplay vivDisplayToTranslate : hierarchy) {
            vivDisplayToTranslate.changePosition(translation.x, translation.y, translation.z);
        }
    }

    // Function to resize all displays in a hierarchy
    public static void resizeHierarchy(VivDisplay vivDisplay, float size) {
        // Get all displays in the hierarchy
        if (vivDisplay == null){
            System.out.println("resizeHierarchy: vivDisplay is null");
        } else {
            System.out.println(vivDisplay.displayName);
        }
        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);

        // Get the position of the highest parent
        Vector3d parentPosition = hierarchy.get(0).getPosition();

        // Resize all VivDisplays in the hierarchy
        for (VivDisplay vivDisplayToResize : hierarchy) {
            // Scale the position of the VivDisplay relative to the highest parent
            Vector3d position = vivDisplayToResize.getPosition();
            Vector3d scaledPosition = new Vector3d(
                    parentPosition.x + (position.x - parentPosition.x) * size,
                    parentPosition.y + (position.y - parentPosition.y) * size,
                    parentPosition.z + (position.z - parentPosition.z) * size
            );
            vivDisplayToResize.setPosition(scaledPosition.x, scaledPosition.y, scaledPosition.z, null);

            // Scale the size of the VivDisplay
            vivDisplayToResize.setScale(vivDisplayToResize.getScale() * size, null);
        }
    }


    /**
     * Handles the translate command for a player's selected VivDisplay group.
     *
     * @param player The player issuing the command.
     * @param args   Command arguments:
     *               - /displaygroup translate <xTranslation> <yTranslation> <zTranslation>
     */
    public void handleDisplayGroupTranslateCommand(Player player, String[] args) {
        // Check if the correct number of arguments are provided
        if (args.length != 4) {
            player.sendMessage("Usage: /displaygroup translate <xTranslation> <yTranslation> <zTranslation>");
            return;
        }

        // Parse the translation from the arguments
        double xTranslation, yTranslation, zTranslation;
        try {
            xTranslation = Double.parseDouble(args[1]);
            yTranslation = Double.parseDouble(args[2]);
            zTranslation = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid translation. Please provide valid numbers for xTranslation, yTranslation, and zTranslation.");
            return;
        }

        // Get the player's selected VivDisplay
        VivDisplay selectedVivDisplay = getSelectedVivDisplay(player);

        // If the player has not selected a VivDisplay, send an error message and return
        if (selectedVivDisplay == null) {
            player.sendMessage("You must first select a Display");
            return;
        }

        // Translate the hierarchy of the selected VivDisplay
        translateHierarchy(selectedVivDisplay, new Vector3d(xTranslation, yTranslation, zTranslation));

        // Send a success message to the player
        player.sendMessage("Successfully translated the selected display group.");
    }

    public void spawnParticlesAtHierarchy(VivDisplay vivDisplay, Particle particle, int particleCount) {
        // Get all displays in the hierarchy
        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);

        // Spawn particles at each display
        for (VivDisplay display : hierarchy) {
            // Location displayLocation = display.display.getLocation();
            drawParticleLine(display.display.getLocation(), vivDisplay.display.getLocation(), particle, particleCount);
            //display.spawnParticle(particle, particleCount);
        }
    }

    public void handleDisplayGroupShowCommand(Player player, String[] args) {
        // Check if the correct number of arguments is provided
        if (args.length != 1) {
            player.sendMessage("Usage: /displaygroup show");
            return;
        }

        // Get the player's selected VivDisplay by name
        VivDisplay selectedVivDisplay = selectedVivDisplays.get(player);

        // Check if the selected VivDisplay exists
        if (selectedVivDisplay == null) {
            player.sendMessage("Display group not found or not selected.");
            return;
        }

        // Spawn particles at every display in the hierarchy
        Particle particle = null;
        int particleCount = 5000;
        spawnParticlesAtHierarchy(selectedVivDisplay, particle, particleCount);

        // Send a success message to the player
        player.sendMessage("Particles shown at every display in the group.");
    }
}