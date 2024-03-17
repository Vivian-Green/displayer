package me.vivian.displayer.display;

import me.vivian.displayer.DisplayPlugin;
import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Config;
import me.vivian.displayerutils.CommandParsing;
import me.vivian.displayer.config.Texts;
import me.vivian.displayerutils.NBTMagic;
import me.vivian.displayerutils.WorldGuardIntegrationWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DisplayHandler {
    public static final Map<UUID, VivDisplay> selectedVivDisplays = new HashMap<>();
    public static final Map<UUID, Long> lastUpdateTimes = new HashMap<>();
    private static DisplayPlugin plugin;
    public static int playerStaleTime; // seconds
    public static int playerCleanupFrequency; // seconds

    public static void init(DisplayPlugin thisPlugin){
        plugin = thisPlugin;
        playerStaleTime = Config.config.getInt("playerStaleTime");
        playerCleanupFrequency = Config.config.getInt("playerCleanupFrequency");

        int periodTicks = 20 * playerCleanupFrequency;
        plugin.getServer().getScheduler().runTaskTimer(plugin, DisplayHandler::cleanupStalePlayers, 0, periodTicks);
    }

    public static void cleanupStalePlayers() {
        long currentTime = System.currentTimeMillis();
        long staleTime = currentTime - (playerStaleTime * 1000L); // time that's old enough to be stale

        Iterator<Map.Entry<UUID, Long>> iterator = lastUpdateTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            UUID playerUUID = entry.getKey();
            long lastUpdateTime = entry.getValue();

            // for each stale player
            if (lastUpdateTime < staleTime) {
                iterator.remove();
                selectedVivDisplays.remove(playerUUID);
                continue;
            }

            // for each not stale player
            long delayTicks = (playerStaleTime * 20L) + 20L; // Convert seconds to ticks and add 20 ticks
            Bukkit.getScheduler().runTaskLater(plugin, removePlayerVivDisplaysIfStale(playerUUID), delayTicks);
        }
    }

    public static Runnable removePlayerVivDisplaysIfStale(UUID playerUUID) {
        long staleTime2 = System.currentTimeMillis() - (playerStaleTime * 1000L);
        if (lastUpdateTimes.get(playerUUID) < staleTime2) {
            selectedVivDisplays.remove(playerUUID);
            lastUpdateTimes.remove(playerUUID);
        }
        return null;
    }

    public static void removePlayerVivDisplays(UUID playerUUID){
        selectedVivDisplays.remove(playerUUID);
        lastUpdateTimes.remove(playerUUID);
    }



    public static void createBlockDisplay(Player player, String[] args) {
        if (!player.getInventory().getItemInMainHand().getType().isBlock()) {
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("invalidBlock"));
            return;
        }
        BlockData blockData = player.getInventory().getItemInMainHand().getType().createBlockData();
        VivDisplay vivDisplay = new VivDisplay(plugin, player.getWorld(), player.getEyeLocation(), EntityType.BLOCK_DISPLAY, blockData);
        updateDisplay(player, vivDisplay, args);
    }

    public static void createItemDisplay(Player player, String[] args) {
        ItemStack displayItem = player.getInventory().getItemInMainHand().clone();
        displayItem.setAmount(1); // forgor this lmao
        VivDisplay vivDisplay = new VivDisplay(plugin, player.getWorld(), player.getEyeLocation(), EntityType.ITEM_DISPLAY, displayItem);
        updateDisplay(player, vivDisplay, args);
    }

    public static void createTextDisplay(Player player, String[] args) { // todo: filter
        String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();

        if (text.isEmpty()) { // case text is only whitespace, which is trimmed
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("displayCreateTextNoText"));
            return;
        }

        CommandHandler.sendPlayerMsgIfMsg(player, Texts.messages.get("displayCreateText") + text);

        VivDisplay vivDisplay = new VivDisplay(plugin, player.getWorld(), player.getEyeLocation(), EntityType.TEXT_DISPLAY, text);

        ((TextDisplay) (vivDisplay.display)).setSeeThrough(false);

        updateDisplay(player, vivDisplay, args);
    }



    public static void updateDisplay(Player player, VivDisplay vivDisplay, String[] args) {
        boolean atSelected = (args.length >= 3 && args[2].equalsIgnoreCase("atselected"));
        VivDisplay selectedDisplay = selectedVivDisplays.get(player.getUniqueId());

        if (atSelected && selectedDisplay != null) {
            // todo: should the location be set directly?
            vivDisplay.display.setTransformation(selectedDisplay.display.getTransformation());
        } else {
            vivDisplay.display.setRotation(player.getEyeLocation().getYaw(), player.getEyeLocation().getPitch());
        }

        selectedVivDisplays.put(player.getUniqueId(), vivDisplay);
    }

    public static void destroyNearbyDisplays(Player player, String[] args) {
        // todo: MAX RADIUS
        int maxCount = (int) CommandParsing.parseNumberFromArgs(args, 2, 0, 1, player, "Invalid max count"); // default max count to 1
        double radius = CommandParsing.parseNumberFromArgs(args, 3, 0.0, 5.0, player, "Invalid radius"); // default radius to 5

        if (maxCount < 1 || radius < 0.01) return; // Invalid max count or radius, error message already sent in parsing functions

        List<VivDisplay> nearbyVivDisplays = getNearbyVivDisplays(player.getLocation(), (int) radius, player);
        if (nearbyVivDisplays == null) return;

        // Destroy nearby displays up to the specified max count
        int destroyedCount = 0;
        for (VivDisplay vivDisplay: nearbyVivDisplays) {
            if(!WorldGuardIntegrationWrapper.canEditThisDisplay(player, vivDisplay)) {
                CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("cantEditDisplayHere"));
                continue;
            }

            vivDisplay.destroy(player);
            destroyedCount++;

            if (destroyedCount >= maxCount) {
                return;
            }
        }
    }

    // Gets Displays near a (player) within in a given (radius)
    public static List<Display> getNearbyDisplays(Location location, double radius) {
        // todo: MAX RADIUS
        double maxTaxicabDistance = Math.sqrt(3) * radius; // maximum taxicab distance

        List<Display> allDisplays = (List<Display>) location.getWorld().getEntitiesByClass(Display.class);
        List<Display> nearbyDisplays = new ArrayList<>();
        for (Display display: allDisplays) {
            Location displayLocation = display.getLocation();

            double xDistance = Math.abs(location.getX() - displayLocation.getX());
            double yDistance = Math.abs(location.getY() - displayLocation.getY());
            double zDistance = Math.abs(location.getZ() - displayLocation.getZ());

            double totalDistance = xDistance + yDistance + zDistance;

            // do pythagorean after passing taxicab
            if (totalDistance <= maxTaxicabDistance && location.distance(displayLocation) <= radius) {
                nearbyDisplays.add(display);
            }
        }
        return nearbyDisplays;
    }

    // Gets VivDisplay objects near the (player) within a given (radius), sorted by distance
    public static List<VivDisplay> getNearbyVivDisplays(Location location, int radius, Player player) {
        radius = Math.min(radius, Config.config.getInt("maxSearchRadius")); // todo: warn?

        List<Display> nearbyDisplays = getNearbyDisplays(location, radius);

        if (nearbyDisplays.isEmpty()) {
            if (player == null) return null;

            if (!Texts.errors.get("displayNearbyNotFound").isEmpty()){
                CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("displayNearbyNotFound").replace("$radius", ""+radius));
            }
            return null;
        }

        nearbyDisplays.sort(Comparator.comparingDouble(display -> display.getLocation().distance(location)));
        List<VivDisplay> nearbyVivDisplays = new ArrayList<>();
        for (Display display: nearbyDisplays) {
            nearbyVivDisplays.add(new VivDisplay(plugin, display));
        }

        return nearbyVivDisplays;
    }

    public static void destroySelectedDisplay(Player player) {
        VivDisplay selectedVivDisplay = selectedVivDisplays.get(player.getUniqueId());
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("noSelectedDisplay"));
        } else {
            if(!WorldGuardIntegrationWrapper.canEditThisDisplay(player, selectedVivDisplay)) {
                CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("cantEditDisplayHere"));
                return;
            }

            selectedVivDisplay.destroy(player);
        }
    }

    /**
     * Searches for a display with a specific name within a radius of 5 blocks around the player.
     *
     * @param player The player around whom to search for displays.
     * @param displayName The name of the display to search for.
     * @return The found Display object, or null if no display with the given name is found.
     */
    public static Display getDisplayByName(Player player, String displayName) {
        List<Display> nearbyDisplays = getNearbyDisplays(player.getLocation(), Config.config.getInt("maxSearchRadius"));

        // Find the first display with the specified "VivDisplayName" NBT tag equal to displayName
        for (Display display: nearbyDisplays) {
            String currentDisplayName = NBTMagic.getNBT(display, "VivDisplayName", String.class);
            if (currentDisplayName != null && currentDisplayName.equals(displayName)) {
                return display; // Found the display, return it
            }
        }

        return null; // No display found with the given name
    }

    /**
     * Retrieves an ItemStack from a (display).
     *
     * @param display The Display to get the ItemStack from.
     * @return The ItemStack representing the Display, or null if unsupported.
     */
    public static ItemStack getItemStackFromDisplay(Display display) {
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
}
