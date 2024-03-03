package me.vivian.displayer;

import me.vivian.displayer.commands.AutoFill;
import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayGroupHandler;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles startup and events relevant to displays/display GUIs.
 */
public final class EventListeners extends JavaPlugin implements Listener {

    public EventListeners() {}

    // todo: move scale values to config
    double positionScale = 0.01;
    double rotationScale = 1;
    double sizeScale = 0.01;
    double brightnessScale = 0.01;
    double multiplierFastValue = 10.0;

    Map<String, String> errMap;

    public void registerCommand(CommandExecutor commandExecutor, TabCompleter subCommandExecutor, String commandName) {
        System.out.println(commandName);
        getCommand(commandName).setExecutor(commandExecutor);
        getCommand(commandName).setTabCompleter(subCommandExecutor);
    }

    @Override
    public void onEnable() {
        this.saveResource("plugin.yml", false);
        this.saveResource("config.yml", false);
        this.saveResource("texts.yml", false);

        CommandExecutor mainCommandExecutor = new CommandHandler(this);
        TabCompleter subCommandExecutor = new AutoFill();

        registerCommand(mainCommandExecutor, subCommandExecutor, "display");
        registerCommand(mainCommandExecutor, subCommandExecutor, "advdisplay");
        registerCommand(mainCommandExecutor, subCommandExecutor, "displaygroup");

        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);

        errMap = Texts.getErrors();
    }

    public void onDisplayGUIClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();

        int slot = event.getRawSlot();

        // ensure the clicked slot is gui (not player inv)
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        int column = slot % 9; // zero-based
        int row = (slot - column) / 9;

        if (row == 5 && column == 7) {
            // If the clicked slot is at row 5, column 7, close the GUI and autofill the command
            player.closeInventory();
            player.chat("/advdisplay rename ");
            return;
        }

        // Regular click: 1x
        // Shift click: 10x
        // Right click OR shift right click: 0.1x
        double multiplier = event.isShiftClick() ? multiplierFastValue : 1.0;
        multiplier = multiplier / (event.isRightClick() ? (multiplierFastValue * multiplier) : 1.0);


        HashMap<String, String> commandMap = getCommandMap(multiplier, positionScale, rotationScale, sizeScale, brightnessScale);

        String command = commandMap.getOrDefault(column + "," + row, null);
        //System.out.println("clicked button command:");
        //System.out.println(command);
        if (command == null) {return;}

        // todo: call functions directly instead of using tape I mean commands-
        player.performCommand(command);
    }

    public void onDisplayNearbyGUIClick(InventoryClickEvent event) {
        // mise en place
        int slot = event.getRawSlot();

        if (slot < 0 || slot >= event.getInventory().getSize()) return; // OOB slot

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return; // empty slot

        ItemMeta itemMeta = clickedItem.getItemMeta();
        if (itemMeta == null) return; // no metadata?


        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        Player player = (Player) event.getWhoClicked();

        // the actual code

        // Check for display UUID nbt
        if (!dataContainer.has(new NamespacedKey(CommandHandler.getPlugin(), "displayUUID"), PersistentDataType.STRING)) return;

        String UUIDStr = dataContainer.get(new NamespacedKey(CommandHandler.getPlugin(), "displayUUID"), PersistentDataType.STRING);

        // Perform your desired action with the display UUID
        player.performCommand("/advdisplay select " + UUIDStr);
    }




    /**
     * Handles inventory click events, for display GUIs
     *
     * @param event The InventoryClickEvent triggered when a player clicks an inventory.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("display GUI")) { // todo: config this, ctrl+shift+f
            event.setCancelled(true);
            onDisplayGUIClick(event);
        }
        if (event.getView().getTitle().equals("nearby displays")) { // todo: config this, ctrl+shift+f
            event.setCancelled(true);
            onDisplayNearbyGUIClick(event);
        }


    }

    @EventHandler
    public void onPlayerRotate(PlayerMoveEvent event) {
        // todo: toggle this with a perm or command? performance go brrrrrrr
        // todo: if player is sneaking, multiply any movement by 0.1

        Player player = event.getPlayer();

        Material heldItemMaterial = player.getInventory().getItemInMainHand().getType();
        if (!(heldItemMaterial == Material.LEAD || heldItemMaterial == Material.SPECTRAL_ARROW)) return;


        float deltaYaw = (event.getTo().getYaw() - event.getFrom().getYaw()) % 360;
        float deltaPitch = (event.getTo().getPitch() - event.getFrom().getPitch()) % 360;
        System.out.println("deltaYaw: " + deltaYaw);
        System.out.println("deltaPitch: " + deltaPitch);

        // ensure selected display
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
            return;
        }

        if (selectedVivDisplay.isThisParent()) {
            if (heldItemMaterial == Material.LEAD){
                System.out.println("-deltaYaw: " + -deltaYaw);
                DisplayGroupHandler.rotateHierarchy(selectedVivDisplay, new Vector3d(0, 0, -deltaYaw));
            }
            if (heldItemMaterial == Material.SPECTRAL_ARROW){
                DisplayGroupHandler.rotateHierarchy(selectedVivDisplay, new Vector3d(deltaYaw, deltaPitch, 0));
            }
        }else{
            if (heldItemMaterial == Material.LEAD){
                selectedVivDisplay.changeRotation(0, 0, -deltaYaw, null);
            }
            if (heldItemMaterial == Material.SPECTRAL_ARROW){
                selectedVivDisplay.changeRotation(deltaYaw, deltaPitch, 0, null);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // todo: toggle this with a perm or command? performance go brrrrrrr
        Player player = event.getPlayer();

        Material heldItemMaterial = player.getInventory().getItemInMainHand().getType();
        if (!(heldItemMaterial == Material.LEAD || heldItemMaterial == Material.SPECTRAL_ARROW)) return;

        Vector3d from = new Vector3d(event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ());
        Vector3d to = new Vector3d(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
        Vector3d delta = new Vector3d(to.x - from.x, to.y - from.y, to.z - from.z); // can't subtract Vector3d's lmao

        // ensure selectedVivDisplay
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
            return;
        }

        if (selectedVivDisplay.isThisParent()) {
            if (heldItemMaterial == Material.LEAD) {
                DisplayGroupHandler.translateHierarchy(selectedVivDisplay, delta);
            }
            if (heldItemMaterial == Material.SPECTRAL_ARROW) {
                DisplayGroupHandler.resizeHierarchy(selectedVivDisplay, (float) ((delta.x+delta.y+delta.z)*0.1+1));
            }

        }else{
            if (heldItemMaterial == Material.LEAD) {
                selectedVivDisplay.changePosition(delta);
            }
            if (heldItemMaterial == Material.SPECTRAL_ARROW) {
                selectedVivDisplay.changeScale((delta.x+delta.y+delta.z)*0.1, null);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStandClickHandler.onInteractWithArmorStand(event);
        }
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