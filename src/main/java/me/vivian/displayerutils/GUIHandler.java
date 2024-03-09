package me.vivian.displayerutils;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GUIHandler {

    // Creates an ItemStack in the (inventory) with the specified (material) and (displayName) at the given (x, y) coordinates.
    public static void createButtonAtXY(Inventory inventory, Material material, String displayName, int x, int y) {
        ItemStack button = new ItemStack(material);
        button = ItemManipulation.itemWithName(button, displayName);
        ItemManipulation.setInventoryItemXY(inventory, button, x, y);
    }

    public static void createPlusMinusButtonsAtXY(Inventory inventory, Material material, String displayName, int x, int y) {
        createButtonAtXY(inventory, material, "+" + displayName, x, y);
        createButtonAtXY(inventory, material, "-" + displayName, x, y + 1);
    }

    public static Inventory displayGUIBuilder() {
        // todo: move materials & names to config

        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText("displayGUITitle"));

        Material posButtonMaterial = Material.ORANGE_CONCRETE;
        Material rotButtonMaterial = Material.LIME_CONCRETE;
        Material sizeButtonMaterial = Material.LIGHT_BLUE_CONCRETE;
        Material backButtonMaterial = Material.REDSTONE_BLOCK;

        // buttons
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "x", 1, 1); // pos // todo: config this?
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "y", 2, 1);
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "z", 3, 1);

        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "yaw", 4, 1); // rot
        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "pitch", 5, 1);
        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "roll", 6, 1);

        createPlusMinusButtonsAtXY(inventory, sizeButtonMaterial, "size", 7, 1); // size

        // tool displays
        createButtonAtXY(inventory, Material.LEAD, Texts.getText("displayGUIMovePanelDisplayName"), 2, 3);
        createButtonAtXY(inventory, Material.SPECTRAL_ARROW, Texts.getText("displayGUIRotatePanelDisplayName"), 5, 3);
        createButtonAtXY(inventory, Material.BLAZE_ROD, Texts.getText("displayGUIResizePanelDisplayName"), 7, 3);

        createButtonAtXY(inventory, Material.WRITABLE_BOOK, Texts.getText("displayGUIRenameButtonDisplayName"), 7, 5);

        createButtonAtXY(inventory, backButtonMaterial, Texts.getText("displayGUIBackButtonDisplayName"), 0, 0);

        // book
        ItemManipulation.setInventoryItemXY(inventory, ItemBuilder.makeGUIBook(), 0, 5);

        return inventory;
    }


    public static Inventory displayNearbyGUIBuilder(List<VivDisplay> nearbyVivDisplays) {
        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText("displayNearbyGUITitle"));

        int maxDisplaysToShow = 10;
        int renamedCount = 0;
        for (int index = 0; index < maxDisplaysToShow && index < nearbyVivDisplays.size(); index++) {
            VivDisplay vivDisplay = nearbyVivDisplays.get(index);

            ItemStack button = ItemBuilder.makeDisplaySelectButton(vivDisplay);

            if (!Objects.requireNonNull(button.getItemMeta()).getDisplayName().isEmpty() || vivDisplay.isParent) { // add renamed or parented displays at end, otherwise at begin
                // todo: sort these alphabetically by name, left to right, and give them enough room to show them top to bottom-
                //      or just add them like this but from z to a
                inventory.setItem(53-renamedCount, button);
                renamedCount++;
            } else {
                inventory.setItem(index-renamedCount, button);
            }
        }
        return inventory;
    }

    public static Inventory displayGroupShowGUIBuilder(List<VivDisplay> hierarchy) {
        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText("displayGroupShowGUITitle"));
        int parentCount = 0;
        for (int i = 0; i < hierarchy.size(); i++) {
            VivDisplay thisVivDisplay = hierarchy.get(i);
            ItemStack button = ItemBuilder.makeDisplaySelectButton(thisVivDisplay);
            if (thisVivDisplay.isParent) { // add parented displays at end, otherwise at begin
                inventory.setItem(9+parentCount, button);
                parentCount++;
            } else {
                inventory.setItem(18+i-parentCount, button);
            }
        }
        return inventory;
    }
}
