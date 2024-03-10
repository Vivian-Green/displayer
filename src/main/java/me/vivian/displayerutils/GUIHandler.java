package me.vivian.displayerutils;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GUIHandler {
    static Material posButtonMaterial = Material.ORANGE_CONCRETE;
    static Material rotButtonMaterial = Material.LIME_CONCRETE;
    static Material sizeButtonMaterial = Material.LIGHT_BLUE_CONCRETE;
    static Material backButtonMaterial = Material.REDSTONE_BLOCK;

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

    public static void createButtonsAndTools(Inventory inventory) {
        // buttons
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "x", 1, 1); // pos
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

        // back button
        createButtonAtXY(inventory, backButtonMaterial, Texts.getText("displayGUIBackButtonDisplayName"), 0, 0);
    }


    public static Inventory displayGUIBuilder(Player player) {
        VivDisplay selectedDisplay = CommandHandler.selectedVivDisplays.get(player);
        if (selectedDisplay.display instanceof ItemDisplay || selectedDisplay.display instanceof BlockDisplay) {
            return standardDisplayGUIBuilder();
        } else if (selectedDisplay.display instanceof TextDisplay) {
            return textDisplayGUIBuilder((TextDisplay) selectedDisplay);
        }
        return null;
    }

    public static Inventory standardDisplayGUIBuilder() {
        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText("displayGUITitle"));
        createButtonsAndTools(inventory);

        createButtonAtXY(inventory, Material.WRITABLE_BOOK, Texts.getText("displayGUIRenameButtonDisplayName"), 7, 5);
        ItemManipulation.setInventoryItemXY(inventory, ItemBuilder.makeGUIBook(), 0, 5);

        return inventory;
    }

    public static Inventory textDisplayGUIBuilder(TextDisplay textDisplay) {
        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText("displayGUITitle"));
        createButtonsAndTools(inventory);

        ItemStack off = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack on = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        if (textDisplay.getAlignment() == TextDisplay.TextAlignment.LEFT) {
            ItemManipulation.setInventoryItemXY(inventory, on, 0, 5);
        } else {
            ItemManipulation.setInventoryItemXY(inventory, off, 0, 5);
        }
        if (textDisplay.getAlignment() == TextDisplay.TextAlignment.CENTER) {
            ItemManipulation.setInventoryItemXY(inventory, on, 1, 5);
        } else {
            ItemManipulation.setInventoryItemXY(inventory, off, 1, 5);
        }
        if (textDisplay.getAlignment() == TextDisplay.TextAlignment.RIGHT) {
            ItemManipulation.setInventoryItemXY(inventory, on, 2, 5);
        } else {
            ItemManipulation.setInventoryItemXY(inventory, off, 2, 5);
        }

        return inventory;
    }


    public static Inventory displayNearbyGUIBuilder(List<VivDisplay> vivDisplays) {
        return displaySelectorGUIBuilder(vivDisplays, Texts.getText("displayNearbyGUITitle"), true);
    }

    public static Inventory displayGroupShowGUIBuilder(List<VivDisplay> vivDisplays) {
        return displaySelectorGUIBuilder(vivDisplays, Texts.getText("displayGroupShowGUITitle"), false);
    }

    public static Inventory displaySelectorGUIBuilder(List<VivDisplay> vivDisplays, String titleKey, boolean isNearby) {
        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText(titleKey));

        int maxDisplaysToShow = 10;
        int count = 0;

        vivDisplays.sort(Comparator.comparing(vivDisplay -> vivDisplay.displayName));

        for (int i = 0; i < maxDisplaysToShow && i < vivDisplays.size(); i++) {
            VivDisplay vivDisplay = vivDisplays.get(i);
            ItemStack button = ItemBuilder.makeDisplaySelectButton(vivDisplay);

            if (isNearby) {
                count = placeButtonAndGetRenamedCount(button, vivDisplay, inventory, count, i);
            } else {
                count = placeButtonAndGetParentCount(vivDisplay, inventory, count, button, i);
            }
        }
        return inventory;
    }

    private static int placeButtonAndGetParentCount(VivDisplay vivDisplay, Inventory inventory, int parentCount, ItemStack button, int i) {
        if (vivDisplay.isParent) { // add parented displays at begin, otherwise towards end
            inventory.setItem(9+ parentCount, button); // left to right starting at second row
            parentCount++;
        } else {
            inventory.setItem(27+ i - parentCount, button); // left to right starting at fourth row
        }
        return parentCount;
    }

    private static int placeButtonAndGetRenamedCount(ItemStack button, VivDisplay vivDisplay, Inventory inventory, int renamedCount, int i) {
        if (!button.getItemMeta().getDisplayName().isEmpty() || vivDisplay.isParent) { // add renamed or parented displays at end, otherwise at begin
            inventory.setItem(53- renamedCount, button);
            renamedCount++;
        } else {
            inventory.setItem(i - renamedCount, button);
        }
        return renamedCount;
    }
}
