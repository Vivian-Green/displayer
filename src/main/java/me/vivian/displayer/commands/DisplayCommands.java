package me.vivian.displayer.commands;

import me.vivian.displayerutils.*;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DisplayCommands {

    static Map<String, String> errMap = Texts.getErrors();

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
     * /display destroy [nearby]
     * Destroys (player)'s selected display, or nearby displays in range if "nearby" (args[1]) specified
     *
     * @param player The player executing the command.
     * @param args   Additional arguments provided with the command.
     */
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

    /**
     * If [block] (args[1]) specified, creates a block display; otherwise, creates an item display.
     * If ([atselected] (args[2]) specified AND no selected display) OR (player)'s held item is invalid, warn (player)
     *
     * @param player The player executing the command.
     * @param args   Command arguments:
     *               - /display create <name> [block/item] [atselected]
     */
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
     * /display replaceitem
     * If (player)'s held item is invalid or no selected display, warn (player)
     * Drops the display's old item at itself, taking 1 from the (player)'s held item
     *
     * @param player The player executing the command.
     */
    static void handleDisplayReplaceItemCommand(Player player) {
        if (!ItemManipulation.isHeldItemValid(player)) {
            player.sendMessage(errMap.get("displayCreateEmptyHand")); // todo: generic this name or use different label
            return;
        }

        VivDisplay selectedDisplay = CommandHandler.selectedVivDisplays.get(player);
        if (selectedDisplay == null) {
            player.sendMessage(errMap.get("noSelectedDisplay"));
            return;
        }

        ItemStack newItem = player.getInventory().getItemInMainHand();
        newItem.setAmount(1); // ensure no item duping-

        ItemStack oldItem = selectedDisplay.replaceItem(newItem);
        player.getWorld().dropItem(selectedDisplay.display.getLocation(), oldItem);

        // todo: check for creative mode before taking shit, also whatever perms are good enough for that idk
        // todo: dry? since this check is needed twice now
        ItemManipulation.takeFromHeldItem(player);
    }

    /**
     * finds nearby VivDisplay objects within a given radius,
     * sends messages to the (player) with hyperlinks to each
     *
     * @param player The player who issued the command.
     * @param args   Command arguments:
     *               - args[1]: (Optional) The radius  to search for displays. Defaults to 5
     */
    static void handleDisplayNearbyCommand(Player player, String[] args) {
        double radius = CommandParsing.parseNumberFromArgs(args, 1, 1, 5, player, "Invalid radius specified.");

        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(player, (int) radius);

        if (nearbyVivDisplays.isEmpty()) {
            // errs in func
            return;
        }

        player.sendMessage(errMap.get("displayNearbyTitle"));
        int maxDisplaysToShow = 10;
        for (int index = 0; index < maxDisplaysToShow && index < nearbyVivDisplays.size(); index++) {
            createHyperlink(player, nearbyVivDisplays.get(index));
        }
    }

    /**
     * Selects the closest VivDisplay to the player's location within a specified radius.
     *
     * @param player The player executing the command.
     */
    static void handleDisplayClosestCommand(Player player) {
        int radius = 5; // todo: config this? lmao
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

    /**
     * Creates and opens the display-editing inventory-GUI for a (player) with buttons for adjusting position, rotation, and size.
     *
     * @param player The player performing the command.
     */
    static void handleDisplayGUICommand(Player player) {
        Inventory inventory = GUIHandler.displayGUIBuilder();
        player.openInventory(inventory);
    }

    /**
     * Sends a hyperlink to the player for selecting a VivDisplay with a given UUID.
     *
     * @param player    The player to send the hyperlink to.
     * @param vivDisplay The VivDisplay to create a hyperlink for.
     */
    public static void createHyperlink(Player player, VivDisplay vivDisplay) {
        assert vivDisplay != null;

        Location location = vivDisplay.display.getLocation();
        Location playerLocation = player.getLocation();

        // Get distance rounded to 2 places
        double distance = TransformMath.roundTo(location.distance(playerLocation), 2);

        String name = CommandHandler.nbtm.getNBT(vivDisplay.display, "VivDisplayName", String.class);
        if (name == null) name = "";

        Material displayMaterial;
        String displayTypeStr;

        // Get material & type of display
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
            return; // Exit early if the display is borked
        }

        // Create & send message to select this display, if it's not borked
        String hyperLinkText = errMap.get("displayNearbyHyperlinkText");
        hyperLinkText = hyperLinkText.replace("$DisplayTypeDisplayText", displayTypeStr);
        hyperLinkText = hyperLinkText.replace("$DisplayName", name);
        hyperLinkText = hyperLinkText.replace("$DisplayMaterial", displayMaterial.toString());
        hyperLinkText = hyperLinkText.replace("$Distance", distance + "");

        TextComponent message = new TextComponent(hyperLinkText);

        // Set click event to run command for selecting the display using its UUID
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/advdisplay select " + vivDisplay.display.getUniqueId()));

        player.spigot().sendMessage(message);
    }
}
