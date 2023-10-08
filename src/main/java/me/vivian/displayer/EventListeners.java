package me.vivian.displayer;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector2i;

/**
 * Handles startup and events relevant to displays/display GUIs.
 */
public final class EventListeners extends JavaPlugin implements Listener {
    /**
     * constructor
     */
    public EventListeners(){}

    // todo: move scale values to config
    double positionScale = 0.01;
    double rotationScale = 1;
    double sizeScale = 0.01;
    double brightnessScale = 0.01;
    double multiplierFastValue = 10.0;

    ItemManipulation im = new ItemManipulation();

    public void registerCommand(CommandExecutor commandExecutor, SubCommandExecutor subCommandExecutor, String commandName){
        getCommand(commandName).setExecutor(commandExecutor);
        getCommand("display").setTabCompleter(subCommandExecutor);
    }

    @Override
    public void onEnable() {
        // todo: un-gpt comments lmao
        // Plugin startup logic
        System.out.println("THIS IS VERY WIP AND SHOULD NOT BE ON A PUBLIC SERVER");

        CommandExecutor mainCommandExecutor = new DisplayCommands(getDescription());
        SubCommandExecutor subCommandExecutor = new SubCommandExecutor(getDescription());

        registerCommand(mainCommandExecutor, subCommandExecutor, "display");
        registerCommand(mainCommandExecutor, subCommandExecutor, "advdisplay");

        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Handles inventory click events, for display GUIs
     *
     * @param event The InventoryClickEvent triggered when a player clicks an inventory.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if inventory is not the display GUI
        if (!event.getView().getTitle().equals("display GUI")) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        int slot = event.getRawSlot(); // Get the raw slot number

        // Check if the clicked slot is not valid
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        ItemStack clickedItem = event.getInventory().getItem(slot);

        // Check if the clicked slot is empty (does not contain an item)
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        int column = slot % 9; // Calculate the column (zero-based)
        int row = (slot - column) / 9; // Calculate the row (zero-based)

        Vector2i selectedSlot = new Vector2i(column, row);

        // Determine the multiplier based on shift-click & right click
        // Regular click: 1
        // Shift click: 10
        // Right click OR shift right click: 0.1
        double multiplier = event.isShiftClick() ? multiplierFastValue : 1.0;
        multiplier = multiplier / (event.isRightClick() ? (multiplierFastValue * multiplier) : 1.0);

        // Generate the command string with the multiplier for the /advdisplay command
        String command;
        switch (selectedSlot.x + "," + selectedSlot.y) {
            case "1,1":
                command = "advdisplay changeposition " + (positionScale * multiplier) + " 0 0";
                break;
            case "1,2":
                command = "advdisplay changeposition " + (-positionScale * multiplier) + " 0 0";
                break;
            case "2,1":
                command = "advdisplay changeposition 0 " + (positionScale * multiplier) + " 0";
                break;
            case "2,2":
                command = "advdisplay changeposition 0 " + (-positionScale * multiplier) + " 0";
                break;
            case "3,1":
                command = "advdisplay changeposition 0 0 " + (positionScale * multiplier);
                break;
            case "3,2":
                command = "advdisplay changeposition 0 0 " + (-positionScale * multiplier);
                break;
            case "4,1":
                command = "advdisplay changerotation " + (rotationScale * multiplier) + " 0 0";
                break;
            case "4,2":
                command = "advdisplay changerotation " + (-rotationScale * multiplier) + " 0 0";
                break;
            case "5,1":
                command = "advdisplay changerotation 0 " + (rotationScale * multiplier) + " 0";
                break;
            case "5,2":
                command = "advdisplay changerotation 0 " + (-rotationScale * multiplier) + " 0";
                break;
            case "6,1":
                command = "advdisplay changerotation 0 0 " + (rotationScale * multiplier);
                break;
            case "6,2":
                command = "advdisplay changerotation 0 0 " + (-rotationScale * multiplier);
                break;
            case "7,1":
                command = "advdisplay changesize " + (sizeScale * multiplier);
                break;
            case "7,2":
                command = "advdisplay changesize " + (-sizeScale * multiplier);
                break;
            case "8,1":
                command = "advdisplay changebrightness " + (brightnessScale * multiplier);
                break;
            case "8,2":
                command = "advdisplay changebrightness " + (-brightnessScale * multiplier);
                break;
            default:
                return; // No action for other slots
        }

        // Execute the command
        player.performCommand(command);
    }

}

