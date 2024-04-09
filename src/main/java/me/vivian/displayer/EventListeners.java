package me.vivian.displayer;

import java.util.*;

import me.vivian.displayerutils.TMath;
import org.bukkit.util.Vector;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Config;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayGroupHandler;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.ItemManipulation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
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
import org.joml.Vector2i;
import org.joml.Vector3d;

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
            case 27:
                System.out.println("displaygroup button click!");
                player.closeInventory();
                player.performCommand("displaygroup show");
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
                System.out.println("material button click!"); // todo: make this a function
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
                        String errStr = Texts.getError("invalidBlockDisplayItem").replace("$itemName", cursorItem.getType().name());
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

        if (switchStartSlot <= slot && slot <= switchStartSlot + 2) { // bottom left 3 slots 3-state switch // todo: extract this to a function
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

    private static Vector3d handleVectorPanel(double multiplier, Vector2i slot2i, Vector2i startSlot) {
        int column = slot2i.x - startSlot.x;
        int row = slot2i.y - startSlot.y;

        if (column >= 0 && column < 3 && row >= 0 && row < 2) {
            if (row == 1) {
                multiplier *= -1; // Adjust multiplier for second row
            }
            double[] resultVec = new double[3]; // new {0, 0, 0}
            resultVec[column] = multiplier; // set right direction for column selected to right multiplier for row selected
            return new Vector3d(resultVec);
        }
        return null;
    }


    private boolean handleTranslationButtonClick(double multiplier, int slot, Player player, VivDisplay selectedVivDisplay) {
        System.out.println("translation buttons check called");
        int column = slot % 9; // zero-based
        int row = (slot - column) / 9;
        Vector2i slot2i = new Vector2i(column, row);

        Vector3d translation = handleVectorPanel(positionScale * multiplier, slot2i, new Vector2i(0, 1));
        if (translation != null) {
            selectedVivDisplay.changePosition(translation);
            return true;
        }
        Vector3d rotation = handleVectorPanel(rotationScale * multiplier, slot2i, new Vector2i(3, 1));
        if (rotation != null) {
            selectedVivDisplay.changeRotation(rotation);
            return true;
        }
        Vector3d scale = handleVectorPanel(sizeScale * multiplier, slot2i, new Vector2i(6, 1));
        if (scale != null) {
            selectedVivDisplay.changeSize(scale);
            return true;
        }

        if (handleArrowKeyButtonClicks(player, slot)) return true;

        return false;
    }

    private static boolean handleArrowKeyButtonClicks(Player player, int slot) {
        int column = slot % 9; // zero-based
        int row = (slot - column) / 9;

        boolean isArrowKey = (column >= 2 && column <= 4 && row == 4) || (row == 3 && column == 3);
        if (isArrowKey){ // match any arrow key
            Location location = player.getLocation();

            if (column == 3) { // match middle keys to pitch
                System.out.println("||v||");
                int dir = row == 3 ? -1 : 1;
                location.setPitch(location.getPitch() + dir * 10);
            } else { // match not middle keys to yaw
                System.out.println("--h--");
                int dir = column == 4 ? 1 : -1;
                location.setYaw(location.getYaw() + dir * 10);
            }

            Inventory oldGUI = player.getOpenInventory().getInventory(50); // guarantee 54 slot inv of some OBVIOUS weird stuff-
            player.teleport(location);
            player.openInventory(oldGUI);
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

        if (selectedVivDisplay.isParentDisplay() && Config.config.getBoolean("doDisplayGroupRotation")) {
            if (heldItemMaterial == Material.LEAD){
                //System.out.println("-deltaYaw: " + -deltaYaw);
                //DisplayGroupHandler.rotateHierarchy(selectedVivDisplay, new Vector3d(0, 0, -deltaYaw));
            }
            if (heldItemMaterial == Material.SPECTRAL_ARROW){
                DisplayGroupHandler.rotateHierarchy(selectedVivDisplay, -deltaYaw);
            }
        } else if (!selectedVivDisplay.isParentDisplay()){
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

        Vector delta = event.getTo().toVector().subtract(event.getFrom().toVector());

        if (selectedVivDisplay.isParentDisplay()) {
            if (heldItemMaterial == Material.LEAD) {
                DisplayGroupHandler.translateHierarchy(selectedVivDisplay, delta.toVector3d());
            }
            /*if (heldItemMaterial == Material.BLAZE_ROD) {
                DisplayGroupHandler.resizeHierarchy(selectedVivDisplay, (float) ((delta.x+delta.y+delta.z)*0.1+1));
            }*/
        }else{
            if (heldItemMaterial == Material.LEAD) {
                selectedVivDisplay.changePosition(delta);
            }
            if (heldItemMaterial == Material.BLAZE_ROD) {
                selectedVivDisplay.changeSize(new Vector3d(delta.length()*sizeScale));
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