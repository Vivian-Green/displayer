package me.vivian.displayer.commands;

import me.vivian.displayer.DisplayPlugin;
import me.vivian.displayer.config.Config;
import me.vivian.displayer.display.DisplayGroupHandler;
import me.vivian.displayerutils.*;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DisplayCommands {

    static FileConfiguration config = Config.config;
    static double GUIYawOffsetTarget;
    static int maxSearchRadius;
    static boolean autoFocusDisplayOnGUI;

    static String displayNearbyGuiTitle; // todo: refactor into map
    static String displayClosestSuccessMsg;
    static String cantEditDisplayHereErr;
    static String displayEmptyHandErr;
    static String noSelectedDisplayErr;
    static String displayReplaceItemTextDisplayErr;
    static String displayCreateHyperLinkErr;
    static String displayGUIReplaceItemButtonDisplayNameErr;
    static String advDisplayDestroyNoYesFlagErr;
    static String advDisplayDestroyUsageErr;

    static String displayGUIReplaceItemButtonDisplayName;
    static String displayCreateHyperLink;
    static String displayRenameUsageErr;
    static String displayCreateTextNoTextErr;
    static String displayCreateEmptyHandErr;



    private static DisplayPlugin plugin;

    public static void init(DisplayPlugin thisPlugin){
        plugin = thisPlugin;
        GUIYawOffsetTarget = config.getInt("GUIYawOffsetTarget");
        maxSearchRadius = config.getInt("maxSearchRadius");
        autoFocusDisplayOnGUI = config.getBoolean("autoFocusDisplayOnGUI");

        displayNearbyGuiTitle = Texts.getText("displayNearbyGUITitle");
        displayClosestSuccessMsg = Texts.messages.get("displayClosestSuccess");

        cantEditDisplayHereErr = Texts.getError("cantEditDisplayHere");
        displayEmptyHandErr = Texts.getError("displayEmptyHand");
        noSelectedDisplayErr = Texts.getError("noSelectedDisplay");
        displayReplaceItemTextDisplayErr = Texts.getError("displayReplaceItemTextDisplay");
        displayGUIReplaceItemButtonDisplayNameErr = Texts.getText("displayGUIReplaceItemButtonDisplayName");
        displayCreateHyperLinkErr = Texts.getText("displayCreateHyperLink");
        advDisplayDestroyNoYesFlagErr = Texts.getError("advDisplayDestroyNoYesFlag");
        advDisplayDestroyUsageErr = Texts.getError("advDisplayDestroyUsage");

        displayGUIReplaceItemButtonDisplayName = Texts.getText("displayGUIReplaceItemButtonDisplayName");
        displayCreateHyperLink = Texts.getText("displayCreateHyperLink");
        displayRenameUsageErr = Texts.getError("displayRenameUsage");
        displayCreateTextNoTextErr = Texts.getError("displayCreateTextNoText");
        displayCreateEmptyHandErr = Texts.getError("displayCreateEmptyHand");
    }
    /**
     * writes an awful, technical, /help message
     *
     * @param player the player to send the help messages to
     */
    static void handleDisplayHelpCommand(Player player) { // todo: EW AAAAA GROSS EW NO
        player.sendMessage("Displayer Help:");

        Map<String, Map<String, Object>> commands = plugin.pluginDesc.getCommands();

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
     * /display destroy [nearby]
     * Destroys (player)'s selected display, or nearby displays in range if "nearby" (args[1]) specified
     *
     * @param player The player executing the command.
     * @param args   Additional arguments provided with the command.
     */
    static void handleDisplayDestroyCommand(Player player, String[] args) {
        Boolean yesFlag = args.length == 2 && Objects.equals(args[1], "-y");
        if (args.length < 2 || yesFlag) {
            // /display destroy or /display destroy -y

            VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
            if (selectedVivDisplay == null) return;

            if (selectedVivDisplay.isParent) {
                if (!yesFlag) { // tried to destroy parent without -y
                    String errMsg = advDisplayDestroyNoYesFlagErr;
                    errMsg = errMsg.replace("$displayname", selectedVivDisplay.displayName);
                    CommandHandler.sendPlayerMsgIfMsg(player, errMsg);
                    return;
                }
                // destroy children
                List<VivDisplay> queuedDisplays = DisplayGroupHandler.getAllDescendants(selectedVivDisplay);
                for (VivDisplay vivDisplay : queuedDisplays) {
                    vivDisplay.destroy();
                }
                // is now not parent
            }
            // destroy not parent
            DisplayHandler.destroySelectedDisplay(player);
            return;
        }

        if (!args[1].equalsIgnoreCase("nearby")) {
            CommandHandler.sendPlayerMsgIfMsg(player, advDisplayDestroyUsageErr);
            return;
        }

        DisplayHandler.destroyNearbyDisplays(player, args);
    }

    /**
     * If [block] (args[1]) specified, creates a block display; otherwise, creates an item display.
     * If ([atselected] (args[2]) specified AND no selected display) OR (player)'s held item is invalid, warn (player)
     *
     * @param player The player executing the command.
     * @param args   Command arguments:
     *               - /display create <name> [block/item] [atselected]
     */
    static void handleDisplayCreateCommand(Player player, String[] args) {
        if(WorldGuardIntegrationWrapper.worldGuardExists && !WorldGuardIntegrationWrapper.canEditDisplay(player)) {
            CommandHandler.sendPlayerMsgIfMsg(player, cantEditDisplayHereErr);
            return;
        }

        boolean isText = args.length >= 2 && args[1].equalsIgnoreCase("text");
        if (isText) {
            if (args.length < 3) {
                CommandHandler.sendPlayerMsgIfMsg(player, displayCreateTextNoTextErr);
                return;
            }
            DisplayHandler.createTextDisplay(player, args);
            return;
        }

        if (!ItemManipulation.isHeldItemValid(player)) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayCreateEmptyHandErr);
            return;
        }
        boolean atSelected = args.length >= 3 && args[2].equalsIgnoreCase("atselected");
        if (atSelected && DisplayHandler.selectedVivDisplays.get(player.getUniqueId()) == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }

        boolean isBlock = args.length >= 2 && Objects.equals(args[1], "block");
        if (isBlock) {
            DisplayHandler.createBlockDisplay(player, args);
        } else {
            DisplayHandler.createItemDisplay(player, args);
        }

        // todo: check for creative mode before taking shit, also whatever perms are good enough for that idk luckperms permission permissions
        // todo: dry? since this check is needed twice now? ctrl+f

        ItemManipulation.takeFromHeldItem(player);
    }

    /**
     * Handles the renaming of a (player)'s selected display by adding a custom NBT tag with a given arg name
     *
     * @param player The player who issued the command.
     * @param args   Command arguments:
     *               - /display rename <name>
     */
    static void handleDisplayRenameCommand(Player player, String[] args) {
        if (args.length < 2) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayRenameUsageErr);
            return;
        }

        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }

        String name = args[1];
        CommandHandler.sendPlayerMsgIfMsg(player, selectedVivDisplay.rename(name));
    }

    /**
     * /display replaceitem
     * If (player)'s held item is invalid or no selected display, warn (player)
     * Drops the display's old item at itself, taking 1 from the (player)'s held item
     *
     * @param player The player executing the command.
     */
    static void handleDisplayReplaceItemCommand(Player player) {
        if (!ItemManipulation.isHeldItemValid(player)) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayEmptyHandErr);
            return;
        }

        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());

        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }
        if (selectedVivDisplay instanceof TextDisplay) {
            CommandHandler.sendPlayerMsgIfMsg(player, displayReplaceItemTextDisplayErr);
            return;
        }
        if(!WorldGuardIntegrationWrapper.canEditThisDisplay(player, selectedVivDisplay)) {
            CommandHandler.sendPlayerMsgIfMsg(player, cantEditDisplayHereErr);
            return;
        }

        ItemStack newItem = player.getInventory().getItemInMainHand().clone();
        newItem.setAmount(1);
        selectedVivDisplay.replaceItem(newItem);

        // todo: check for creative mode before taking shit, also whatever perms are good enough for that idk luckperms permission permissions
        // todo: dry? since this check is needed twice now? ctrl+f

        ItemManipulation.takeFromHeldItem(player);
    }

    /**
     * finds nearby VivDisplay objects within a given radius,
     * opens nearby gui
     *
     * @param player The player who issued the command.
     * @param args   Command arguments:
     *               - args[1]: (Optional) The radius  to search for displays. Defaults to 5
     */
    static void handleDisplayNearbyCommand(Player player, String[] args) {
        double radius = CommandParsing.parseNumberFromArgs(args, 1, 1, 5, player, "Invalid radius specified."); // todo: config this

        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(player.getLocation(), (int) radius, player);

        if (nearbyVivDisplays == null || nearbyVivDisplays.isEmpty()) return; // errs in func

        Inventory inventory = GUIBuilder.displaySelectorGUIBuilder(nearbyVivDisplays, displayNearbyGuiTitle);
        player.openInventory(inventory);
    }

    /**
     * Selects the closest VivDisplay to the player's location within a specified radius.
     *
     * @param player The player executing the command.
     */
    static void handleDisplayClosestCommand(Player player) {
        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(player.getLocation(), maxSearchRadius, player);

        if (nearbyVivDisplays.isEmpty()) return; // errs in func

        VivDisplay closestVivDisplay = nearbyVivDisplays.get(0);

        if(!WorldGuardIntegrationWrapper.canEditThisDisplay(player, closestVivDisplay)) {
            // return on closest display can't be edited
            CommandHandler.sendPlayerMsgIfMsg(player, cantEditDisplayHereErr);
            return;
        }

        DisplayHandler.selectedVivDisplays.put(player.getUniqueId(), closestVivDisplay);
        CommandHandler.sendPlayerMsgIfMsg(player, displayClosestSuccessMsg);
        player.performCommand("display locate");
    }

    /**
     * Creates and opens the display-editing inventory-GUI for a (player) with buttons for adjusting position, rotation, and size.
     *
     * @param player The player performing the command.
     */
    static void handleDisplayGUICommand(Player player) {
        VivDisplay selectedDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
        if (selectedDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);

            MiscUtils.sendHyperlink(displayCreateHyperLink, "/display create", player);
            return;
        }

        Inventory inventory = GUIBuilder.displayGUIBuilder(player);
        if (selectedDisplay.display instanceof ItemDisplay || selectedDisplay.display instanceof BlockDisplay) {
            ItemStack itemStack = selectedDisplay.getItemStack();
            itemStack.setAmount(1);
            ItemMeta itemMeta = itemStack.getItemMeta();
            String name = displayGUIReplaceItemButtonDisplayName;
            if (name.isEmpty()) {
                name = "displayGUIReplaceItemButtonDisplayName";
            }
            itemMeta.setDisplayName(name);
            itemStack.setItemMeta(itemMeta);

            inventory.setItem(53, itemStack);
        }

        if(autoFocusDisplayOnGUI) {
            // Get yaw difference
            Vector lookVector = player.getLocation().getDirection();
            Vector toDisplayVector = selectedDisplay.display.getLocation().subtract(player.getLocation()).toVector();
            lookVector.setY(0);
            toDisplayVector.setY(0);
            lookVector.normalize();
            toDisplayVector.normalize();

            // Calculate cross product to determine relative direction
            Vector crossProduct = lookVector.clone().crossProduct(toDisplayVector);
            double direction = crossProduct.getY(); // Get the y-component to determine direction

            double dotProduct = lookVector.dot(toDisplayVector);
            double angleInRadians = Math.acos(dotProduct);
            double angleInDegrees = Math.toDegrees(angleInRadians);

            // Check if the player is looking towards the display
            if (angleInDegrees <= GUIYawOffsetTarget) {
                // Turn the player away from the display based on relative direction
                float newYaw;
                if (direction > 0) {
                    System.out.println("right!");
                    // facing to the right, turn right
                    newYaw = (float) (player.getLocation().getYaw() + (GUIYawOffsetTarget-angleInDegrees));
                } else {
                    // facing to the left, turn left
                    newYaw = (float) (player.getLocation().getYaw() - (GUIYawOffsetTarget-angleInDegrees));
                }

                Location newLocation = player.getLocation().clone();
                newLocation.setYaw(newYaw);
                player.teleport(newLocation);
            }
        }


        player.openInventory(inventory);
    }

    public static void handleDisplayLocateCommand(Player player) {
        VivDisplay selectedDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
        if (selectedDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }

        ParticleHandler.drawParticleLine(player.getLocation(), selectedDisplay.display.getLocation(), Particle.REDSTONE, 100, new Particle.DustOptions(Color.PURPLE, 5));
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> ParticleHandler.spawnParticle(selectedDisplay.display, Particle.SONIC_BOOM, 5),
                20
        );
    }

    public static void handleDisplayUnselectCommand(Player player) {
        DisplayHandler.removePlayerVivDisplays(player.getUniqueId());
    }
}
