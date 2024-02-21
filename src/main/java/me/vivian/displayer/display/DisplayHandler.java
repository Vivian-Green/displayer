package me.vivian.displayer.display;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.commands.Parsing;
import me.vivian.displayer.config.Texts;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DisplayHandler {

    static Map<String, String> errMap = Texts.getErrors();
    private static final Plugin plugin = CommandHandler.getPlugin();
    public static void createBlockDisplay(Player player, String[] args) {
        if (!player.getInventory().getItemInMainHand().getType().isBlock()) {
            player.sendMessage(errMap.get("invalidBlock"));
            return;
        }
        BlockData blockData = player.getInventory().getItemInMainHand().getType().createBlockData();
        VivDisplay vivDisplay = new VivDisplay(plugin, player.getWorld(), player.getEyeLocation(), EntityType.BLOCK_DISPLAY, blockData);
        updateDisplay(player, vivDisplay, args);
    }

    public static void createItemDisplay(Player player, String[] args) {
        ItemStack displayItem = player.getInventory().getItemInMainHand();
        displayItem.setAmount(1); // forgor this lmao
        VivDisplay vivDisplay = new VivDisplay(plugin, player.getWorld(), player.getEyeLocation(), EntityType.ITEM_DISPLAY, displayItem);
        updateDisplay(player, vivDisplay, args);
    }

    public static void updateDisplay(Player player, VivDisplay vivDisplay, String[] args) {
        boolean atSelected = (args.length >= 3 && args[2].equalsIgnoreCase("atselected"));
        if (atSelected && CommandHandler.selectedVivDisplays.get(player) != null) {
            // todo: should the location be set directly?
            vivDisplay.display.setTransformation(CommandHandler.selectedVivDisplays.get(player).display.getTransformation());
        } else {
            vivDisplay.display.setRotation(player.getEyeLocation().getYaw(), player.getEyeLocation().getPitch());
        }

        CommandHandler.vivDisplays.put(vivDisplay.display.getUniqueId().toString(), vivDisplay);
        CommandHandler.selectedVivDisplays.put(player, vivDisplay);
    }

    public static void destroyNearbyDisplays(Player player, String[] args) {
        // todo: MAX RADIUS
        int maxCount = (int) Parsing.parseNumberFromArgs(args, 2, 0, 1, player, "Invalid max count"); // default max count to 1
        double radius = Parsing.parseNumberFromArgs(args, 3, 0.0, 5.0, player, "Invalid radius"); // default radius to 5

        if (maxCount <= 0 || radius <= 0.0) {
            return; // Invalid max count or radius, error message already sent in parsing functions
        }

        List<VivDisplay> nearbyVivDisplays = getNearbyVivDisplays(player, (int) radius);

        // Destroy nearby displays up to the specified max count
        int destroyedCount = 0;
        for (VivDisplay vivDisplay: nearbyVivDisplays) {
            vivDisplay.destroy(player, CommandHandler.vivDisplays, CommandHandler.selectedVivDisplays);
            destroyedCount++;

            if (destroyedCount >= maxCount) {
                return;
            }
        }
    }

    // Gets Displays near a (player) within in a given (radius)
    public static List<Display> getNearbyDisplays(Player player, double radius) {
        // todo: MAX RADIUS
        double maxTaxicabDistance = Math.sqrt(3) * radius; // maximum taxicab distance
        Location playerLocation = player.getLocation();

        List<Display> allDisplays = (List<Display>) player.getWorld().getEntitiesByClass(Display.class);
        List<Display> nearbyDisplays = new ArrayList<>();
        for (Display display: allDisplays) {
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
    public static List<VivDisplay> getNearbyVivDisplays(Player player, int radius) {
        // todo: MAX RADIUS
        List<VivDisplay> nearbyVivDisplays = new ArrayList<>();
        List<Display> nearbyDisplays = getNearbyDisplays(player, radius);

        for (Display display: nearbyDisplays) {
            // Get the UUID of the display
            String displayUUID = String.valueOf(display.getUniqueId());

            // Check if it exists in the VivDisplays map
            if (!CommandHandler.vivDisplays.containsKey(displayUUID)) {
                // Instantiate a new VivDisplay and add it to the list
                VivDisplay vivDisplay = new VivDisplay(plugin, display);
                nearbyVivDisplays.add(vivDisplay);

                // Add the newly created VivDisplay to the map with its UUID as the key
                CommandHandler.vivDisplays.put(displayUUID, vivDisplay);
            } else {
                nearbyVivDisplays.add(CommandHandler.vivDisplays.get(displayUUID));
            }
        }
        nearbyVivDisplays.sort(Comparator.comparingDouble(vivDisplay -> vivDisplay.display.getLocation().distance(player.getLocation())));

        if (nearbyVivDisplays.isEmpty()) {
            player.sendMessage(errMap.get("displayNearbyNotFound_Begin") + radius + errMap.get("displayNearbyNotFound_End"));
        }

        return nearbyVivDisplays;
    }

    public static void destroySelectedDisplay(Player player) {
        VivDisplay selectedVivDisplay = CommandHandler.selectedVivDisplays.get(player);
        if (selectedVivDisplay == null) {
            player.sendMessage(errMap.get("noSelectedDisplay"));
        } else {
            selectedVivDisplay.destroy(player, CommandHandler.vivDisplays, CommandHandler.selectedVivDisplays);
        }
    }

    //self-explanatory
    public static VivDisplay getSelectedVivDisplay(Player player) {
        VivDisplay selectedVivDisplay = CommandHandler.selectedVivDisplays.get(player);
        if (selectedVivDisplay == null) {
            player.sendMessage(errMap.get("noSelectedDisplay"));
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
    public static Display getVivDisplayByName(Player player, String displayName) { //todo: is this supposed to return a display & not a VivDisplay?
        // Get the nearby displays within a radius of 5 blocks
        List<Display> nearbyDisplays = getNearbyDisplays(player, 5);

        // Find the first display with the specified "VivDisplayName" NBT tag equal to displayName
        for (Display display: nearbyDisplays) {
            String currentDisplayName = CommandHandler.nbtm.getNBT(display, "VivDisplayName", String.class);
            if (currentDisplayName != null && currentDisplayName.equals(displayName)) {
                return display; // Found the display, return it
            }
        }

        return null; // No display found with the given name
    }

    public static VivDisplay getSelectedDisplayIfExists(Player player) {
        VivDisplay selectedVivDisplay = CommandHandler.selectedVivDisplays.get(player);

        if (selectedVivDisplay == null) {
            player.sendMessage(errMap.get("noSelectedDisplay"));
            return null;
        }

        return selectedVivDisplay;
    }
}
