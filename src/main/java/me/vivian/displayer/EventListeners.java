package me.vivian.displayer;

import me.vivian.displayerutils.ItemManipulation;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector2i;

import java.util.HashMap;

/**
 * Handles startup and events relevant to displays/display GUIs.
 */
public final class EventListeners extends JavaPlugin implements Listener {
    /**
     * constructor
     */
    public EventListeners() {}

    // todo: move scale values to config
    double positionScale = 0.01;
    double rotationScale = 1;
    double sizeScale = 0.01;
    double brightnessScale = 0.01;
    double multiplierFastValue = 10.0;

    ItemManipulation im = new ItemManipulation();

    public void registerCommand(CommandExecutor commandExecutor, SubCommandExecutor subCommandExecutor, String commandName) {
        getCommand(commandName).setExecutor(commandExecutor);
        getCommand(commandName).setTabCompleter(subCommandExecutor);
    }

    @Override
    public void onEnable() {
        // todo: un-gpt comments lmao
        // Plugin startup logic
        System.out.println("THIS IS VERY WIP AND SHOULD NOT BE ON A PUBLIC SERVER");

        CommandExecutor mainCommandExecutor = new DisplayCommands(this);
        SubCommandExecutor subCommandExecutor = new SubCommandExecutor(getDescription());

        registerCommand(mainCommandExecutor, subCommandExecutor, "display");
        registerCommand(mainCommandExecutor, subCommandExecutor, "advdisplay");
        registerCommand(mainCommandExecutor, subCommandExecutor, "displaygroup");

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
        // ensure this is the display gui
        if (!event.getView().getTitle().equals("display GUI")) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        int slot = event.getRawSlot(); // Get the raw slot number

        // ensure the clicked slot is within the inventory gui (not player inv)
        if (slot < 0 || slot >= event.getInventory().getSize()) {
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

        HashMap<String, String> commandMap = getCommandMap(multiplier, positionScale, rotationScale, sizeScale, brightnessScale);

        String command = commandMap.getOrDefault(selectedSlot.x + "," + selectedSlot.y, null);
        if (command == null) {return;}

        // Execute the command
        player.performCommand(command);
    }

    private static HashMap<String, String> getCommandMap(double multiplier, double positionScale, double rotationScale, double sizeScale, double brightnessScale) {
        // it was this or a switch statement
        HashMap<String, String> commandMap = new HashMap<>();
        commandMap.put("1,1", "advdisplay changeposition " + (positionScale * multiplier) + " 0 0");
        commandMap.put("1,2", "advdisplay changeposition " + (-positionScale * multiplier) + " 0 0");
        commandMap.put("2,1", "advdisplay changeposition 0 " + (positionScale * multiplier) + " 0");
        commandMap.put("2,2", "advdisplay changeposition 0 " + (-positionScale * multiplier) + " 0");
        commandMap.put("3,1", "advdisplay changeposition 0 0 " + (positionScale * multiplier));
        commandMap.put("3,2", "advdisplay changeposition 0 0 " + (-positionScale * multiplier));
        commandMap.put("4,1", "advdisplay changerotation " + (rotationScale * multiplier) + " 0 0");
        commandMap.put("4,2", "advdisplay changerotation " + (-rotationScale * multiplier) + " 0 0");
        commandMap.put("5,1", "advdisplay changerotation 0 " + (rotationScale * multiplier) + " 0");
        commandMap.put("5,2", "advdisplay changerotation 0 " + (-rotationScale * multiplier) + " 0");
        commandMap.put("6,1", "advdisplay changerotation 0 0 " + (rotationScale * multiplier));
        commandMap.put("6,2", "advdisplay changerotation 0 0 " + (-rotationScale * multiplier));
        commandMap.put("7,1", "advdisplay changesize " + (sizeScale * multiplier));
        commandMap.put("7,2", "advdisplay changesize " + (-sizeScale * multiplier));
        return commandMap;
    }
}