package me.vivian.displayer.commands;

import com.sk89q.worldedit.command.util.CreatureButcher;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import me.vivian.displayer.config.Config;
import me.vivian.displayerutils.*;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DisplayCommands {

    static Map<String, String> errMap = Texts.getErrors();
    static Map<String, String> msgMap = Texts.getMessages();

    static FileConfiguration config = Config.getConfig();

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
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("advDisplayDestroyUsage"));
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
        WorldGuardIntegration worldGuardIntegration = new WorldGuardIntegration();

        if(!WorldGuardIntegration.canEditDisplay(player)) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("cantEditDisplayHere"));
            return;
        }

        boolean isBlock = args.length >= 2 && Objects.equals(args[1], "block");
        boolean atSelected = args.length >= 3 && args[2].equalsIgnoreCase("atselected");
        boolean isText = args.length >= 3 && args[2].equalsIgnoreCase("text");

        if (isText) {
            if (args.length < 4) {
                CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayCreateTextNoText"));
                return;
            }

            DisplayHandler.createTextDisplay(player, args);
        }

        // todo: where put this when it just kinda figured itself out on errs? CommandHandler.sendPlayerMessageIfExists(player, )(errMap.get("displayCreateUsage"));

        if (!ItemManipulation.isHeldItemValid(player)) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayCreateEmptyHand"));
            return;
        }
        if (atSelected && CommandHandler.selectedVivDisplays.get(player) == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
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
     * Handles the renaming of a (player)'s selected display by adding a custom NBT tag with a given arg name
     *
     * @param player The player who issued the command.
     * @param args   Command arguments:
     *               - /display rename <name>
     */
    static void handleDisplayRenameCommand(Player player, String[] args) {
        if (args.length < 2) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("advDisplayRenameUsage"));
            return;
        }

        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedDisplayIfExists(player);
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
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
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayEmptyHand")); // todo: generic this name or use different label
            return;
        }

        VivDisplay selectedVivDisplay = CommandHandler.selectedVivDisplays.get(player);
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
            return;
        }
        if (selectedVivDisplay instanceof TextDisplay) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayReplaceItemTextDisplay"));
            return;
        }

        if(!WorldGuardIntegration.canEditThisDisplay(player, selectedVivDisplay)) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("cantEditDisplayHere"));
            return;
        }

        ItemStack newItem = player.getInventory().getItemInMainHand();

        selectedVivDisplay.replaceItem(newItem);

        // todo: check for creative mode before taking shit, also whatever perms are good enough for that idk
        // todo: dry? since this check is needed twice now
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
        double radius = CommandParsing.parseNumberFromArgs(args, 1, 1, 5, player, "Invalid radius specified.");

        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(player.getLocation(), (int) radius, player);

        if (nearbyVivDisplays.isEmpty()) return; // errs in func

        Inventory inventory = GUIHandler.displaySelectorGUIBuilder(nearbyVivDisplays, Texts.getText("displayNearbyGUITitle"), true);
        player.openInventory(inventory);
    }

    /**
     * Selects the closest VivDisplay to the player's location within a specified radius.
     *
     * @param player The player executing the command.
     */
    static void handleDisplayClosestCommand(Player player) {
        int radius = config.getInt("maxSearchRadius");
        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(player.getLocation(), radius, player);

        if (nearbyVivDisplays.isEmpty()) return; // errs in func

        VivDisplay closestVivDisplay = nearbyVivDisplays.get(0);

        if(!WorldGuardIntegration.canEditThisDisplay(player, closestVivDisplay)) {
            // return on closest display can't be edited
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("cantEditDisplayHere"));
            return;
        }

        CommandHandler.selectedVivDisplays.put(player, closestVivDisplay);
        ParticleHandler.spawnParticle(closestVivDisplay.display, null, null);
        CommandHandler.sendPlayerMsgIfMsg(player, msgMap.get("displayClosestSuccess"));
    }

    /**
     * Creates and opens the display-editing inventory-GUI for a (player) with buttons for adjusting position, rotation, and size.
     *
     * @param player The player performing the command.
     */
    static void handleDisplayGUICommand(Player player) {
        Inventory inventory = GUIHandler.displayGUIBuilder(player);

        VivDisplay selectedDisplay = CommandHandler.selectedVivDisplays.get(player);
        if (selectedDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
            // todo: 'try "/display create"'
            //       'or "/display nearby"' hyperlinks? also, config that-
            return;
        }

        ItemStack itemStack = selectedDisplay.getItemStack();
        itemStack.setAmount(1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        String name = Texts.getText("displayGUIReplaceItemButtonDisplayName");
        if(name.isEmpty()){
            name = "displayGUIReplaceItemButtonDisplayName";
        }
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);

        inventory.setItem(53, itemStack);

        player.openInventory(inventory);
    }
}
