package me.vivian.displayer;

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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3i;

import java.util.*;

/**
 * handles commands for creating, manipulating, and interacting with displays.
 */
public class DisplayCommands implements CommandExecutor {
    private PluginDescriptionFile pluginDesc;
    private Plugin plugin;

    /**
     * Constructor
     *
     * @param thisPlugin The plugin
     */
    public DisplayCommands(Plugin thisPlugin) {
        plugin = thisPlugin;
        pluginDesc = plugin.getDescription();
        nbtm = new NBTMagic(plugin);
    }
    ItemManipulation im = new ItemManipulation();
    NBTMagic nbtm;

    private final Map<Player, List<Display>> nearbyDisplays = new HashMap<>();
    private final Map<Player, Display> selectedDisplays = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
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
                default:
                    player.sendMessage("Invalid subcommand for /displaygroup. Try /displaygroup help");
            }
        }
        return true;
    }

    /**
     * Sends detailed info about the selected display to the player.
     *
     * @param player The player who executed the command.
     */
    private void handleDisplayDetailsCommand(Player player) {
        // Get the selected display for the player
        Display selectedDisplay = selectedDisplays.get(player);

        if (selectedDisplay == null) {
            player.sendMessage("You haven't selected a display.");
            return;
        }

        // Get custom NBT data
        String displayName = nbtm.getStringNBT(selectedDisplay, "VivDisplayName");
        String parentUUID = nbtm.getStringNBT(selectedDisplay, "VivDisplayParentUUID");
        boolean isChild = nbtm.getBoolNBT(selectedDisplay, "VivDisplayIsChild");
        boolean isParent = nbtm.getBoolNBT(selectedDisplay, "VivDisplayIsParent");


        // Get display information
        ItemStack displayItem = getItemStackFromDisplay(selectedDisplay);
        Material displayMaterial = displayItem.getType();
        Location displayLocation = locationRoundedTo(selectedDisplay.getLocation(), 2);

        double currentYaw = roundTo(displayLocation.getYaw(), 2);
        double currentPitch = roundTo(displayLocation.getPitch(), 2);
        double currentRoll = roundTo(getTransRoll(selectedDisplay.getTransformation()), 2);
        double currentSize = roundTo(selectedDisplay.getTransformation().getScale().x, 2);

        // Calculate the distance from the player to the display
        double distance = roundTo(player.getLocation().distance(displayLocation), 2);

        // Send the details to the player
        player.sendMessage("Display Name: " + displayName);
        player.sendMessage("Display Material: " + displayMaterial);
        player.sendMessage("Display Position: X=" + displayLocation.getX() + " Y=" + displayLocation.getY() + " Z=" + displayLocation.getZ());
        player.sendMessage("Display Rotation: Yaw=" + currentYaw + " Pitch=" + currentPitch + " Roll=" + currentRoll);
        player.sendMessage("Display Size: " + currentSize);
        player.sendMessage("Distance to Display: " + distance);

        // Send NBT data related to parent and child
        if(isChild){
            player.sendMessage("Parent UUID: " + parentUUID);
        }
        player.sendMessage("Is Parent: " + isParent);
    }



    private void handleDisplaySetParentCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /displaygroup setparent <parentname>");
            return;
        }

        String parentName = args[1]; // Get the parent display name to set

        // Get the nearby displays within a radius of 5 blocks
        List<Display> nearbyDisplays = getNearbyDisplays(player, 5);

        // Find the first display with the specified "VivDisplayName" NBT tag equal to parentName
        Display parentDisplay = null;
        for (Display display : nearbyDisplays) {
            String displayName = nbtm.getStringNBT(display, "VivDisplayName");
            if (displayName != null && displayName.equals(parentName)) {
                parentDisplay = display;
                break; // Found the parent display, exit the loop
            }
        }

        if (parentDisplay == null) {
            player.sendMessage("No nearby display with the name '" + parentName + "' found.");
            return;
        }

        // Get the UUID of the parent display
        UUID parentUUID = parentDisplay.getUniqueId();

        // Get the selected display for the player
        Display selectedDisplay = selectedDisplays.get(player);

        if (selectedDisplay == null) {
            player.sendMessage("You haven't selected a display to set as a child.");
            return;
        }

        // Set the "VivDisplayParentUUID" tag of the selected display to the parent's UUID
        nbtm.setNBT(selectedDisplay, "VivDisplayParentUUID", parentUUID.toString());

        // Set the "VivDisplayIsParent" tag of the parent display to true
        nbtm.setNBT(parentDisplay, "VivDisplayIsParent", true);
        nbtm.setNBT(selectedDisplay, "VivDisplayIsChild", true);

        player.sendMessage("The selected display is now a child of '" + parentName + "'.");
    }

    private void handleDisplayUnparentCommand(Player player) {
        // Get the selected display for the player
        Display selectedDisplay = selectedDisplays.get(player);

        if (selectedDisplay == null) {
            player.sendMessage("You haven't selected a display to unparent.");
            return;
        }

        // Get the UUID of the parent display from the selected display's NBT
        String parentUUIDStr = nbtm.getStringNBT(selectedDisplay, "VivDisplayParentUUID");

        if (parentUUIDStr == null) {
            player.sendMessage("The selected display is not a child display.");
            return;
        }

        UUID parentUUID;
        try {
            parentUUID = UUID.fromString(parentUUIDStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid parent UUID in the selected display's NBT.");
            return;
        }

        // Get the parent display based on the parent UUID
        Display parentDisplay = getDisplayByUUID(player, parentUUID);

        // Unset the parent NBT tags
        nbtm.setNBT(selectedDisplay, "VivDisplayParentUUID", "");
        nbtm.setNBT(selectedDisplay, "VivDisplayIsChild", false);

        // todo: list of children parented to a display, to set a display's IsParent properly
        // todo: ability to delete nbt

        if (parentDisplay == null) {
            player.sendMessage("The parent display no longer exists.");
            return;
        }

        player.sendMessage("The selected display is now unparented.");
    }

    // Helper method to find a display by UUID
    private Display getDisplayByUUID(Player player, UUID uuid) {
        // todo: SURELY you can just getEntityByUUID??
        for (Display display : getAllDisplays(player)) {
            if (display.getUniqueId().equals(uuid)) {
                return display;
            }
        }
        return null;
    }



    /**
     * Handles the renaming of a selected display by adding a custom NBT tag with the name "name."
     * If a valid display is selected and the renaming is successful, it updates the display's name.
     *
     * @param player The player who issued the command.
     * @param args   The command arguments, where args[1] is the new name.
     */
    private void handleDisplayRenameCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /advdisplay rename <name>");
            return;
        }

        String name = args[1]; // Get the name to set

        // Get the selected display for the player
        Display selectedDisplay = selectedDisplays.get(player);

        if (selectedDisplay == null) {
            player.sendMessage("You haven't selected a display to rename.");
            return;
        }

        nbtm.setNBT(selectedDisplay, "VivDisplayName", name);
        player.sendMessage("this display is now called " + name);
    }



    /**
     * writes an awful, technical, /help message
     *
     * @param player the player to send the help messages to
     */
    private void handleDisplayHelpCommand(Player player) {
        // todo: OH GOD WHY
        //  WHY?????
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

            // Check if the command has subcommands
            if (commandInfo.containsKey("subcommands")) {
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
    }




    /**
     * Parses a double from the specified index of a string array (args) and ensures
     * it is greater than or equal to a specified minimum value.
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
            Display selectedDisplay = selectedDisplays.get(player);
            destroyDisplay(player, selectedDisplay);
            return;
        }

        if (args[1].equalsIgnoreCase("nearby")) {
            int maxCount = (int) parseNumberFromArgs(args, 2, 0.0, 1, player, "Invalid max count"); // Default max count
            double radius = parseNumberFromArgs(args, 3, 0.0,  5.0, player, "Invalid radius"); // Default radius

            if (maxCount < 0 || radius < 0.0) {
                return; // Invalid max count or radius, error message already sent in parsing functions
            }

            List<Display> nearbyDisplays = getNearbyDisplays(player, (int) radius);

            if (nearbyDisplays.isEmpty()) {
                player.sendMessage("No nearby Displays found within " + radius + " blocks.");
                return;
            }

            // Sort nearby displays by distance from the player
            nearbyDisplays.sort(Comparator.comparingDouble(entity -> player.getLocation().distance(entity.getLocation())));

            // Destroy nearby displays up to the specified max count
            int destroyedCount = 0;
            for (Display display : nearbyDisplays) {
                destroyDisplay(player, display);
                destroyedCount++;

                if (destroyedCount >= maxCount) {
                    break;
                }
            }
        } else {
            player.sendMessage("Usage: /display destroy [nearby [max count] [radius]]");
        }
    }

    /**
     * Destroys and removes a specified (display) for the given (player).
     *
     * @param player  The player initiating the destruction.
     * @param display The display to be destroyed.
     *
     * This method removes the display from the world and the player's selection.
     * In the event of any exception during the process, it logs the error but still ensures display removal.
     */
    private void destroyDisplay(Player player, Display display) {
        // todo: this can be written safer right??
        //  like there's nothing obviously & blatantly wrong, but my bad code senses are tingling
        if (display != null) {
            try {
                player.getWorld().dropItemNaturally(display.getLocation(), getItemStackFromDisplay(display));
            } catch (Exception e) {
                System.out.println("destroyDisplay(): Failed to spawn item on display destroy. Is this an unsupported display type?");
                System.out.println("destroyDisplay(): Destroying anyway after this stack trace:");
                e.printStackTrace();
            } finally {
                /*
                Always remove the selected display from the world, even after erring on item spawning.
                DO NOT MAKE A FUCKING DUPE.

                It is better to maybe destroy an item and mildly inconvenience a player & staff,
                than it is to leave in a potential dupe that could go unreported.
                ...try to avoid either of these things, obviously
                 */

                if (selectedDisplays.containsKey(player) && selectedDisplays.get(player) == display) {
                    selectedDisplays.remove(player);
                } else {
                    System.out.println("destroyDisplay(): Destroying unselected display... is this intentional?");
                }
                display.remove();

                // Remove it from the selectedDisplays map
                selectedDisplays.remove(player);

                player.sendMessage("Display destroyed.");
            }
        } else {
            System.out.println("destroyDisplay(): tried to destroy a display that was null");
            player.sendMessage("You must first select a Display");
        }
    }

    /**
     * Retrieves an ItemStack from a (display).
     *
     * @param display The Display to get the ItemStack from.
     * @return The ItemStack representing the Display, or null if unsupported.
     */
    private static ItemStack getItemStackFromDisplay(Display display) {
        // todo: consider switch statement when adding TextDisplay
        if (display instanceof ItemDisplay) {
            // If ItemDisplay, return its ItemStack directly
            ItemDisplay itemDisplay = (ItemDisplay) display;
            return itemDisplay.getItemStack();
        } else if (display instanceof BlockDisplay) {
            // If BlockDisplay, create an ItemStack based on the block material
            BlockDisplay blockDisplay = (BlockDisplay) display;
            Material material = blockDisplay.getBlock().getMaterial();
            return new ItemStack(material, 1);
        } else {
            System.out.println("getItemStackFromDisplay(): Unsupported display type");
            return null;
        }
    }


    /**
     * gets a (player)'s selected Display.
     *
     * @param player The player who has a selected Display.
     * @return The selected Display or null if none selected.
     */
    private Display getSelectedDisplay(Player player) {
        Display selectedDisplay = selectedDisplays.get(player);
        if (selectedDisplay == null) {
            player.sendMessage("You must first select a Display");
        }
        return selectedDisplay;
    }

    /**
     * modifies a (player)'s selected Display. Allows changing (+=) or setting (=) the size with optional offsets.
     *
     * @param player The player performing the command.
     * @param args   Command arguments:
     *              - For changing the size: /display changesize <size offset: number>
     *              - For setting the size: /display setsize <size: number>
     */
    private void handleDisplaySizeCommand(Player player, String[] args) {
        boolean isChange = args.length > 0 && "changesize".equalsIgnoreCase(args[0]);

        String errorMessage = isChange ?
                "Invalid size offset value. Usage: /display changesize <size offset: number>" :
                "Invalid size value. Usage: /display setsize <size: number>";

        double sizeChange = parseNumberFromArgs(args, 1, Double.NEGATIVE_INFINITY, -1.0, player, errorMessage);


        if (!isChange && sizeChange < 0.0) {
            return;
        }

        Display selectedDisplay = getSelectedDisplay(player);
        if (selectedDisplay == null) {
            return;
        }

        Transformation transformation = selectedDisplay.getTransformation();
        double newScale = isChange ? (transformation.getScale().x + sizeChange) : sizeChange;

        if (newScale > 0.0) {
            transformation.getScale().set(newScale);
            selectedDisplay.setTransformation(transformation);
        } else {
            player.sendMessage("Invalid scale value. Scale must be greater than 0.0.");
        }
    }

    /**
     * Creates and spawns a Display entity in the specified (world) at a given (location).
     *
     * @param world        The world in which the Display is created.
     * @param location     The location at which the Display is spawned.
     * @param entityType   The EntityType of the Display (e.g., BLOCK_DISPLAY or ITEM_DISPLAY).
     * @param displayData  The data associated with the Display (BlockData for BLOCK_DISPLAY or ItemStack for ITEM_DISPLAY).
     * @return The newly created Display entity.
     */
    private Display createDisplay(World world, Location location, EntityType entityType, Object displayData) {
        Display display = (Display) world.spawnEntity(location, entityType);

        if (display instanceof BlockDisplay && displayData instanceof BlockData) {
            ((BlockDisplay) display).setBlock((BlockData) displayData);
        } else if (display instanceof ItemDisplay && displayData instanceof ItemStack) {
            ((ItemDisplay) display).setItemStack((ItemStack) displayData);
        } else {
            System.out.println("createDisplay: Unhandled display type or display data mismatch.");
        }

        return display;
    }

    /**
     * Checks if the (player) is holding a valid item for creating a Display.
     *
     * @param player The player whose held item is being checked.
     * @return True if the held item is valid; otherwise, false.
     */
    private boolean isHeldItemValid(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            player.sendMessage("You must be holding an item or block to create a display.");
            return false;
        }
        return true;
    }

    /**
     * Creates and sets a (display) for a (player), at their eye location unless overridden by (atSelected)
     *
     * @param player     The player for whom the Display is being created and set.
     * @param display    The Display to be created and set.
     * @param atSelected Set to true if the Display should match the transformation of a selected Display, if any.
     */
    private void createAndSetDisplay(Player player, Display display, boolean atSelected) {
        selectedDisplays.put(player, display);

        if (!atSelected) {
            Location eyeLocation = player.getEyeLocation();
            display.setRotation(eyeLocation.getYaw(), eyeLocation.getPitch());
        }

        Display selectedDisplay = getSelectedDisplay(player);

        if (atSelected && selectedDisplay != null) {
            display.setTransformation(selectedDisplay.getTransformation());
        } else if (atSelected) {
            player.sendMessage("You need to select a display first.");
            display.remove(); // Remove the newly created display since no selection is made
        }
    }

    /**
     * Reduces the count of the (player)'s held item by 1. If the new count <= 0, the remove it.
     *
     * @param player The player whose held item is being modified.
     */
    private void takeFromHeldItem(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        heldItem.setAmount(heldItem.getAmount() - 1);
        if (heldItem.getAmount() <= 0) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            player.getInventory().setItemInMainHand(heldItem);
        }
    }

    /**
     * Handles the creation of a Display for a (player)
     *
     * @param player The player initiating the command.
     * @param args   Command arguments:
     *              - [displayType]: The type of Display to create (e.g., "block" or "item").
     *              - [atSelected]: (Optional) Use "atselected" to create the Display at the selected location.
     */
    private void handleDisplayCreateCommand(Player player, String[] args) {
        String displayType = "item";
        if (args.length >= 2 && Objects.equals(args[1], "block")) {
            displayType = "block";
        }

        // Check for the "atSelected" arg
        Display selectedDisplay = selectedDisplays.get(player);
        boolean atSelected = (args.length >= 3 && args[2].equalsIgnoreCase("atselected"));

        if (atSelected && !selectedDisplays.containsKey(player)) {
            player.sendMessage("You need to select a display first!");
            return;
        }

        if (!isHeldItemValid(player)) {
            return;
        }

        Location eyeLocation = player.getEyeLocation();
        World world = player.getWorld();
        Display display;

        if (displayType.equals("block")) {
            Material heldItemType = player.getInventory().getItemInMainHand().getType();

            if (!heldItemType.isBlock()) {
                player.sendMessage("Invalid block!");
                return;
            }

            BlockData blockData = heldItemType.createBlockData();
            display = createDisplay(world, atSelected ? selectedDisplay.getLocation() : eyeLocation, EntityType.BLOCK_DISPLAY, blockData);

            takeFromHeldItem(player);
        } else {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            ItemDisplay itemDisplay = (ItemDisplay) createDisplay(world, atSelected ? selectedDisplay.getLocation() : eyeLocation, EntityType.ITEM_DISPLAY, itemStack);

            // Set the item count to 1 in the created ItemDisplay
            itemDisplay.setItemStack(itemStack);
            itemDisplay.getItemStack().setAmount(1);

            // Remove only 1 item from the player's inventory
            itemStack.setAmount(itemStack.getAmount() - 1);
            if (itemStack.getAmount() <= 0) {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            } else {
                player.getInventory().setItemInMainHand(itemStack);
            }
            display = itemDisplay;
        }

        createAndSetDisplay(player, display, atSelected);
    }


    /**
     * gets & lists nearby Displays for a (player) within a specified radius.
     *
     * @param player The player using the command.
     * @param args   Command arguments: [radius]
     */
    private void handleDisplayNearbyCommand(Player player, String[] args) {
        int radius = 5;

        if (args.length >= 2) {
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid radius specified.");
                return;
            }
        }

        List<Display> nearbyEntities = getNearbyDisplays(player, radius);

        if (nearbyEntities.isEmpty()) {
            player.sendMessage("No nearby Displays found within " + radius + " blocks.");
        } else {
            nearbyEntities.sort(Comparator.comparingDouble(entity -> player.getLocation().distance(entity.getLocation())));

            nearbyDisplays.put(player, nearbyEntities);

            player.sendMessage("Nearby Displays:");
            int index = 1;
            int maxDisplaysToShow = 10;
            for (Display entity : nearbyEntities) {
                createHyperlink(player, entity, index);

                index++;
                if (index > maxDisplaysToShow) {
                    break;
                }
            }
        }
    }

    /**
     * gets nearby Displays for a player in a given radius.
     *
     * @param player The player to search near.
     * @return A list of nearby Displays.
     */
    private List<Display> getAllDisplays(Player player) {
        // todo: STORE THESE SOMEWHERE???
        List<Display> allDisplays = new ArrayList<>();

        for (Entity entity : player.getWorld().getEntities()) {
            if(entity instanceof Display){
                allDisplays.add((Display) entity);
            }
        }

        return allDisplays;
    }

    /**
     * gets nearby Displays for a player in a given radius.
     *
     * @param player The player to search near.
     * @param radius The search radius for nearby Displays.
     * @return A list of nearby Displays.
     */
    private List<Display> getNearbyDisplays(Player player, int radius) {
        List<Display> nearbyDisplays = new ArrayList<>();

        Vector3i playerLocation = new Vector3i(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());

        int maxTaxicabDistance = (int) Math.ceil(Math.sqrt(3) * radius); //maximum taxicab distance 

        for (Display display : getAllDisplays(player)) {
            Location displayLocation = display.getLocation();

            int xDistance = Math.abs(playerLocation.x - displayLocation.getBlockX());
            int yDistance = Math.abs(playerLocation.y - displayLocation.getBlockY());
            int zDistance = Math.abs(playerLocation.z - displayLocation.getBlockZ());

            int totalDistance = xDistance + yDistance + zDistance;

            if (totalDistance <= maxTaxicabDistance) {
                nearbyDisplays.add(display);
            }
        }
        return nearbyDisplays;
    }

    /**
     * selects the closest Display to the (player)'s location within a specified (radius).
     *
     * @param player The player performing the command.
     */
    private void handleDisplayClosestCommand(Player player) {
        int radius = 5;
        List<Display> nearbyDisplays = getNearbyDisplays(player, radius);

        if (nearbyDisplays.isEmpty()) {
            player.sendMessage("No nearby Displays found within " + radius + " blocks.");
            return;
        }

        nearbyDisplays.sort(Comparator.comparingDouble(entity -> player.getLocation().distance(entity.getLocation())));
        Display closestDisplay = nearbyDisplays.get(0);

        if (closestDisplay != null) {
            selectedDisplays.put(player, closestDisplay);
            player.sendMessage("Closest Display selected.");
            spawnParticleAtDisplay(closestDisplay);
        } else {
            player.sendMessage("No nearby Displays found.");
        }
    }

    /**
     * Spawns particles at the location of the selected Display.
     *
     * @param selectedDisplay The Display at which particles should be spawned.
     */
    private void spawnParticleAtDisplay(Display selectedDisplay) {
        Location displayLocation = selectedDisplay.getLocation();

        Particle particle = Particle.WATER_DROP;
        int count = 100;

        displayLocation.getWorld().spawnParticle(
                particle,
                displayLocation,
                count
        );
    }

    /**
     * selects a display for (player) given an (index).
     *
     * @param player The player performing the command.
     * @param args   Command arguments:
     *              - [index]: The index of the Display to select.
     */
    private void handleDisplaySelectCommand(Player player, String[] args) {
        if (args.length >= 2) {
            int index = Integer.parseInt(args[1]);
            List<Display> nearbyEntities = nearbyDisplays.get(player);

            if (nearbyEntities != null && index >= 1 && index <= nearbyEntities.size()) {
                Display selectedDisplay = (Display) nearbyEntities.get(index - 1);
                selectedDisplays.put(player, selectedDisplay);
                player.sendMessage("You selected Display #" + index);
                spawnParticleAtDisplay(selectedDisplay);
            } else {
                player.sendMessage("Invalid selection.");
            }
        } else {
            player.sendMessage("Usage: /advdisplay select [index]");
        }
    }

    /**
     * rotates the selected display for a (player). Allows changing (+=) or setting (=) the rotation with optional offsets.
     *
     * @param player The player performing the command.
     * @param args   Command arguments:
     *              - For changing the rotation: /display changerotation <yawOffset> <pitchOffset> [rollOffset]
     *              - For setting the rotation: /display setrotation <yawOffset> <pitchOffset> [rollOffset]
     */
    private void handleDisplayRotationCommand(Player player, String[] args) {
        boolean isChange = args.length > 0 && "changerotation".equalsIgnoreCase(args[0]);

        if (args.length >= 3) {
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

            yawOffset = (float) Math.toRadians(yawOffset);
            pitchOffset = (float) Math.toRadians(pitchOffset);
            rollOffset = (float) Math.toRadians(rollOffset);

            Display selectedDisplay = selectedDisplays.get(player);

            if (selectedDisplay != null) {
                Transformation newTransformation = new Transformation(
                        selectedDisplay.getTransformation().getTranslation(),
                        selectedDisplay.getTransformation().getLeftRotation(),
                        selectedDisplay.getTransformation().getScale(),
                        selectedDisplay.getTransformation().getRightRotation()
                );

                newTransformation.getLeftRotation().rotateYXZ(rollOffset, pitchOffset, yawOffset);

                selectedDisplay.setTransformation(newTransformation);
                //player.sendMessage(isChange ? "Rotation changed for selected Display." : "Rotation set for selected Display.");
            } else {
                player.sendMessage("You must first select a Display");
            }
        } else {
            player.sendMessage("Usage: /display " + (isChange ? "changeposition" : "setposition") + " <pitchOffset> <yawOffset> [rollOffset]");
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


    /**
     * Handles the positioning of a selected Display for a player.
     * Allows changing or setting the position with optional offsets.
     *
     * @param player The player performing the command.
     * @param args   Command arguments:
     *              - For changing the position: /display changeposition <xOffset> <yOffset> <zOffset>
     *              - For setting the position: /display setposition <x> <y> <z>
     */
    private void handleDisplayPositionCommand(Player player, String[] args) {
        boolean isChange = args.length > 0 && "changeposition".equalsIgnoreCase(args[0]);
        if (args.length == 4) {
            double x, y, z;
            try {
                x = Double.parseDouble(args[1]);
                y = Double.parseDouble(args[2]);
                z = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid coordinates. Please provide valid numbers for X, Y, and Z.");
                return;
            }

            Display selectedDisplay = selectedDisplays.get(player);

            if (selectedDisplay != null) {
                Location currentLocation = selectedDisplay.getLocation();
                double newX = isChange ? currentLocation.getX() + x : x;
                double newY = isChange ? currentLocation.getY() + y : y;
                double newZ = isChange ? currentLocation.getZ() + z : z;

                // Store the current rotation
                float currentYaw = selectedDisplay.getLocation().getYaw();
                float currentPitch = selectedDisplay.getLocation().getPitch();

                // Teleport to the new position
                selectedDisplay.teleport(new Location(player.getWorld(), newX, newY, newZ));

                // Apply the previous rotation
                selectedDisplay.setRotation(currentYaw, currentPitch);

                //player.sendMessage(isChange ? "Position changed for selected Display." : "Position set for selected Display.");
            } else {
                player.sendMessage("You must first select a Display");
            }
        } else {
            player.sendMessage(isChange ? "Usage: /display changeposition <xOffset> <yOffset> <zOffset>"
                    : "Usage: /display setposition <x> <y> <z>");
        }
    }

    /**
     * Creates an ItemStack with the specified (material) and (displayName) at the given (x, y) coordinates in the (inventory).
     *
     * @param inventory   The inventory to place the button in.
     * @param material    The material for the button.
     * @param displayName The display name for the button.
     * @param x           The X-coordinate for the button.
     * @param y           The Y-coordinate for the button.
     */
    private void createButtonAtXY(Inventory inventory, Material material, String displayName, int x, int y) {
        ItemStack button = new ItemStack(material);
        button = im.itemWithName(button, displayName);
        im.setInventoryItemXY(inventory, button, x, y);
    }

    /**
     * Creates the display-editing inventory-GUI for a (player) with buttons for adjusting position, rotation, and size.
     *
     * @param player The player for whom the GUI is created.
     */
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

    /**
     * Rounds a double (num) to a specified number of decimal (places).
     *
     * @param num    The number to be rounded.
     * @param places The number of decimal places to round to.
     * @return The rounded double value.
     */
    private double roundTo(double num, int places){
        double mult = Math.pow(10, places);
        return Math.round(num * mult)/mult;
    }

    /**
     * rounds a (location)'s position to (places)
     *
     * @param location The original location.
     * @param places   The number of decimal places to round to.
     * @return A new location with rounded coordinates.
     */
    private Location locationRoundedTo(Location location, int places) {
        double x = roundTo((float) location.getX(), places);
        double y = roundTo((float) location.getY(), places);
        double z = roundTo((float) location.getZ(), places);

        return new Location(location.getWorld(), x, y, z, location.getYaw(), location.getPitch());
    }

    /**
     * sends (player) a hyperlink to select a (display) with a given (index)
     *
     * @param player  The player who will receive the hyperlink.
     * @param display The display to be selected via the hyperlink.
     * @param index   The index associated with the display.
     */
    // todo: use UUID here, not index, so you don't need to have ran /nearby for this select to work
    //  for maintainability..
    private void createHyperlink(Player player, Display display, int index) {
        assert display != null;

        Location location = display.getLocation();
        Location playerLocation = player.getLocation();

        // get distance rounded to 2 places
        double distance = roundTo(location.distance(playerLocation), 2);

        String name = nbtm.getStringNBT(display, "VivDisplayName");
        if(name == null){
            name = "";
        }

        Material displayMaterial;
        String displayTypeStr;

        // get material & type of display
        if (display instanceof BlockDisplay) {
            displayMaterial = ((BlockDisplay) display).getBlock().getMaterial();
            displayTypeStr = "[BlockDisplay]";
        } else if (display instanceof ItemDisplay) {
            ItemStack itemStack = ((ItemDisplay) display).getItemStack();
            assert itemStack != null;
            displayMaterial = itemStack.getType();
            displayTypeStr = "[ItemDisplay]";
        } else {
            displayMaterial = Material.AIR;
            displayTypeStr = "[???]";
            System.out.println("createHyperlink(): This boy ain't right. Was I passed a text display?");
            return; // Exit early if display is borked
        }

        // create message to select this display, if it's not borked
        TextComponent message = new TextComponent(ChatColor.BLUE + "Click to select " + displayTypeStr + " '" + name + "' holding " + displayMaterial.toString() + ", " + distance + " blocks away");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/advdisplay select " + index));
        // send it
        player.spigot().sendMessage(message);
    }
}
