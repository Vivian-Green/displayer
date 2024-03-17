package me.vivian.displayer;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Config;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayGroupHandler;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.ItemManipulation;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// todo: EventListeners should exist and shouldn't be taped to plugin-

/**
 * Handles startup and events relevant to displays/display GUIs.
 */
public final class EventListeners implements Listener {

    public EventListeners() {}

    Plugin plugin;
    double positionScale = 0.01;
    double rotationScale = 1;
    double sizeScale = 0.01;
    double brightnessScale = 0.01;
    double multiplierFastSpeed = 10.0;
    double multiplierSlowSpeed = 0.1;

    String displayGUITitle;
    String displayNearbyGUITitle;
    String displayGroupShowGUITitle;
    ArrayList<String> guiTitles = new ArrayList<>();

    Vector2d switchPosition = new Vector2d(0, 5);
    int switchStartSlot = (int) (switchPosition.y * 9 + switchPosition.x);


    public EventListeners(DisplayPlugin thisPlugin) {
        plugin = thisPlugin;

        displayGUITitle = Texts.getText("displayGUITitle").toLowerCase();
        displayNearbyGUITitle = Texts.getText("displayNearbyGUITitle").toLowerCase();
        displayGroupShowGUITitle = Texts.getText("displayGroupShowGUITitle").toLowerCase();

        guiTitles.addAll(List.of(new String[]{displayGUITitle, displayNearbyGUITitle, displayGroupShowGUITitle}));

        positionScale = (float) Config.config.getDouble("positionScale");
        rotationScale = (float) Config.config.getDouble("rotationScale");
        sizeScale = (float) Config.config.getDouble("sizeScale");
        brightnessScale = (float) Config.config.getDouble("brightnessScale");
        multiplierFastSpeed = (float) Config.config.getDouble("multiplierFastSpeed");
        multiplierSlowSpeed = (float) Config.config.getDouble("multiplierSlowSpeed");
    }

    public void onDisplayGUIClick(InventoryClickEvent event){
        System.out.println("display gui click");
        Player player = (Player) event.getWhoClicked();
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
        if (selectedVivDisplay == null || selectedVivDisplay.display == null) return; // player doesn't have a display selected, so don't do anything on gui click-

        System.out.println(selectedVivDisplay.getClass());

        if (selectedVivDisplay.display instanceof ItemDisplay || selectedVivDisplay.display instanceof BlockDisplay) {
            System.out.println("standard display gui click");
            onStandardDisplayGUIClick(event, selectedVivDisplay);
        } else if (selectedVivDisplay.display instanceof TextDisplay){
            System.out.println("text display gui click");
            onTextDisplayGUIClick(event, selectedVivDisplay);
        } else {
            System.out.println("invalid display on gui click?");
            event.setCancelled(true); // don't let the players take the gui items just bc they're in a weird state (michigan :(  )
            player.closeInventory(); // close the inventory
            player.sendMessage("FUCK!"); // todo: debug, remove
            // todo: write err for this, warn player, do not yell "FUCK!" at them
        }
    }

    private boolean onDisplayGUIClickCommon(InventoryClickEvent event, VivDisplay vivDisplay){
        int slot = event.getRawSlot();
        if (slot > 53) return false;

        Player player = (Player) event.getWhoClicked();
        switch (slot) {
            case 0:
                System.out.println("close button click!");
                player.closeInventory();
                player.performCommand("display nearby");
                event.setCancelled(true);
                return true;
            case 52:
                System.out.println("rename button click!");
                player.closeInventory();
                String command = "/display rename ";
                String json = String.format("{\"text\":\"Click to rename this display\",\"color\":\"green\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"%s\"}}", command);
                player.performCommand("tellraw " + player.getName() + " " + json);
                event.setCancelled(true);
                return true;
        }
        // Regular click: 1x
        // Shift click: 10x
        // Right click OR shift right click: 0.1x
        double multiplier = 1;
        if (event.isShiftClick()){
            multiplier = multiplierFastSpeed;
        }
        if (event.isRightClick()) {
            multiplier = multiplierSlowSpeed;
        }

        System.out.println("checking translation buttons");
        return handleTranslationButtonClick(multiplier, slot, player, vivDisplay);
    }


    private void onStandardDisplayGUIClick(InventoryClickEvent event, VivDisplay selectedVivDisplay) {
        if (onDisplayGUIClickCommon(event, selectedVivDisplay)) {
            event.setCancelled(true);
            return;
        }
        System.out.println("onStandardDisplayGUIClick");

        int slot = event.getRawSlot();

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        switch (slot) {
            case 53:
                System.out.println("material button click!");
                // mise en place
                ItemStack cursorItem = player.getItemOnCursor();
                if (cursorItem.getType() == Material.AIR || cursorItem.getAmount() <= 0) return;

                // player has clicked on the replaceitem button in the display gui while holding an item

                // if BlockDisplay, return if cursorItem Material can't be placed in a BlockDisplay
                if (selectedVivDisplay.display instanceof BlockDisplay) {
                    Material material = cursorItem.getType();
                    try {
                        BlockData blockData = material.createBlockData(); // just a test
                    } catch (Exception IllegalArgumentException){
                        // failed to create a BlockData
                        String errStr = Texts.errors.get("invalidBlockDisplayItem").replace("$itemName", cursorItem.getType().name());
                        CommandHandler.sendPlayerMsgIfMsg(player, errStr);
                        return;
                    }
                }

                // change item slot in gui
                ItemStack newItemStack = cursorItem.clone();
                ItemMeta itemMeta = newItemStack.getItemMeta();
                itemMeta.setDisplayName(Texts.getText("displayGUIReplaceItemButtonDisplayName"));
                newItemStack.setAmount(1);
                newItemStack.setItemMeta(itemMeta);
                inventory.setItem(slot, newItemStack);

                // replace item in display & drop old one
                cursorItem.setAmount(cursorItem.getAmount() - 1);
                player.setItemOnCursor(cursorItem);
                selectedVivDisplay.replaceItem(newItemStack);
                event.setCancelled(true);
                ItemManipulation.takeFromHeldItem(player);
                return;
        }
        if (slot >= inventory.getSize()) return; // only cancel event in standard display gui when in the gui, for replacing item
        event.setCancelled(true);
    }

    private void onTextDisplayGUIClick(InventoryClickEvent event, VivDisplay selectedVivDisplay) {
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot > 53) return;

        if (onDisplayGUIClickCommon(event, selectedVivDisplay)) return;

        TextDisplay textDisplay = (TextDisplay) selectedVivDisplay.display;
        Inventory inventory = event.getInventory();

        if (switchStartSlot <= slot && slot <= switchStartSlot + 2) { // bottom left 3 slots 3-state switch
            System.out.println("is switch click");
            int column = slot % 9; // zero-based

            // todo: when dragging over one of these slots it needs a tactile click this is necessary
            Material off = Material.BLACK_STAINED_GLASS_PANE;
            Material on = Material.WHITE_STAINED_GLASS_PANE;
            Material[] slotMaterials = {off, off, off}; // can define a length of 3 and then fill for an arbitrary length switch but FUCK you I am not writing a goddamn multi-state switch class and binding functions and and and and
            slotMaterials[column - (int) switchPosition.x] = on;

            inventory.setItem(switchStartSlot + 0, new ItemStack(slotMaterials[0]));
            inventory.setItem(switchStartSlot + 1, new ItemStack(slotMaterials[1]));
            inventory.setItem(switchStartSlot + 2, new ItemStack(slotMaterials[2]));

            switch (column) {
                case 0:
                    textDisplay.setAlignment(TextDisplay.TextAlignment.LEFT);
                    return;
                case 1:
                    textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
                    return;
                case 2:
                    textDisplay.setAlignment(TextDisplay.TextAlignment.RIGHT);
                    return;
            }
        }
    }

    private boolean handleTranslationButtonClick(double multiplier, int slot, Player player, VivDisplay selectedVivDisplay) {
        System.out.println("translation buttons check called");
        int column = slot % 9; // zero-based
        int row = (slot - column) / 9;

        double scaledPosition = positionScale * multiplier;
        double scaledRotation = rotationScale * multiplier;

        Map<String, Vector3d> slotToTranslation = Map.of(
                "1,1", new Vector3d(scaledPosition, 0, 0),
                "1,2", new Vector3d(-scaledPosition, 0, 0),
                "2,1", new Vector3d(0, scaledPosition, 0),
                "2,2", new Vector3d(0, -scaledPosition, 0),
                "3,1", new Vector3d(0, 0, scaledPosition),
                "3,2", new Vector3d(0, 0, -scaledPosition)
        );

        Map<String, Vector3d> slotToRotation = Map.of(
                "4,1", new Vector3d(scaledRotation, 0, 0),
                "4,2", new Vector3d(-scaledRotation, 0, 0),
                "5,1", new Vector3d(0, scaledRotation, 0),
                "5,2", new Vector3d(0, -scaledRotation, 0),
                "6,1", new Vector3d(0, 0, scaledRotation),
                "6,2", new Vector3d(0, 0, -scaledRotation)
        );

        String slotKey = column + "," + row;
        System.out.println("slot: "+slotKey);
        Vector3d translation = slotToTranslation.get(slotKey);
        if (translation != null) {
            System.out.println("translation button!");
            System.out.println("    : " + translation);
            selectedVivDisplay.changePosition(translation.x, translation.y, translation.z);
            return true;
        }

        Vector3d rotation = slotToRotation.get(slotKey);
        if (rotation != null) {
            System.out.println("rotation button!");
            selectedVivDisplay.changeRotation((float) rotation.x, (float) rotation.y, (float) rotation.z);
            return true;
        }

        switch (slotKey) {
            case "7,1":
                selectedVivDisplay.changeSize(sizeScale * multiplier, player);
                System.out.println("size button!");
                return true;
            case "7,2":
                selectedVivDisplay.changeSize(-sizeScale * multiplier, player);
                System.out.println("size button!");
                return true;
        }
        return false;
    }

    public void onDisplayNearbyGUIClick(InventoryClickEvent event) {
        //System.out.println("display nearby gui click");

        // mise en place
        int slot = event.getRawSlot();
        if (slot > 53) return; // OOB slot

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return; // empty slot

        ItemMeta itemMeta = clickedItem.getItemMeta();
        if (itemMeta == null) return; // no metadata?

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        // Check for display UUID nbt
        if (!dataContainer.has(new NamespacedKey(plugin, "displayUUID"), PersistentDataType.STRING)) {
            System.out.println("no UUID?"); // todo: config this
            return; // no UUID (should not happen-)
        }

        // actually do the thing if everything is good
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();

        String UUIDStr = dataContainer.get(new NamespacedKey(plugin, "displayUUID"), PersistentDataType.STRING);

        player.performCommand("advdisplay select " + UUIDStr);
    }

    public void onDisplayGroupGUIClick(InventoryClickEvent event){
        onDisplayNearbyGUIClick(event);
    }

    /**
     * Handles inventory click events, for display GUIs
     *
     * @param event The InventoryClickEvent triggered when a player clicks an inventory.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String thisTitle = event.getView().getTitle().toLowerCase();
        if (!guiTitles.contains(thisTitle)) return;

        if (thisTitle.equals(displayGUITitle)) {
            onDisplayGUIClick(event);
            return;
        }
        if (thisTitle.equals(displayNearbyGUITitle) || thisTitle.equals(displayGroupShowGUITitle)) {
            event.setCancelled(true);
            onDisplayNearbyGUIClick(event);
        }
    }


    @EventHandler
    public void onPlayerRotate(PlayerMoveEvent event) {
        // todo: toggle this with a perm or command? performance go brrrrrrr
        Player player = event.getPlayer();

        Material heldItemMaterial = player.getInventory().getItemInMainHand().getType();
        if (!(heldItemMaterial == Material.LEAD || heldItemMaterial == Material.SPECTRAL_ARROW)) return;

        // ensure selected display
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
        if (selectedVivDisplay == null) return;

        float deltaYaw = (event.getTo().getYaw() - event.getFrom().getYaw()) % 360;
        float deltaPitch = (event.getTo().getPitch() - event.getFrom().getPitch()) % 360;

        if(player.isSneaking()){
            deltaPitch *= 0.1F;
            deltaYaw *= 0.1F;
        }

        if (selectedVivDisplay.isParentDisplay()) {
            if (heldItemMaterial == Material.LEAD){
                System.out.println("-deltaYaw: " + -deltaYaw);
                DisplayGroupHandler.rotateHierarchy(selectedVivDisplay, new Vector3d(0, 0, -deltaYaw));
            }
            if (heldItemMaterial == Material.SPECTRAL_ARROW){
                DisplayGroupHandler.rotateHierarchy(selectedVivDisplay, new Vector3d(deltaYaw, deltaPitch, 0));
            }
        }else{
            if (heldItemMaterial == Material.LEAD){
                selectedVivDisplay.changeRotation(0, 0, -deltaYaw);
            }
            if (heldItemMaterial == Material.SPECTRAL_ARROW){
                selectedVivDisplay.changeRotation(deltaYaw, deltaPitch, 0);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // todo: toggle this with a perm or command? performance go brrrrrrr
        Player player = event.getPlayer();

        Material heldItemMaterial = player.getInventory().getItemInMainHand().getType();
        if (!(heldItemMaterial == Material.LEAD || heldItemMaterial == Material.BLAZE_ROD)) return;

        // ensure selectedVivDisplay
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
        if (selectedVivDisplay == null) return;

        Vector3d from = new Vector3d(event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ());
        Vector3d to = new Vector3d(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
        Vector3d delta = new Vector3d(to.x - from.x, to.y - from.y, to.z - from.z); // can't subtract Vector3d's lmao

        if (selectedVivDisplay.isParentDisplay()) {
            if (heldItemMaterial == Material.LEAD) {
                DisplayGroupHandler.translateHierarchy(selectedVivDisplay, delta);
            }
            if (heldItemMaterial == Material.BLAZE_ROD) {
                DisplayGroupHandler.resizeHierarchy(selectedVivDisplay, (float) ((delta.x+delta.y+delta.z)*0.1+1));
            }
        }else{
            if (heldItemMaterial == Material.LEAD) {
                selectedVivDisplay.changePosition(delta);
            }
            if (heldItemMaterial == Material.BLAZE_ROD) {
                selectedVivDisplay.changeSize((delta.x+delta.y+delta.z)*0.1, null);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            if (Config.config.getBoolean("doArmorStandConversion")) {
                ArmorStandClickHandler.onInteractWithArmorStand(event);
            }
        }
    }
}