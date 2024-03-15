package me.vivian.displayerutils;

import me.vivian.displayer.DisplayPlugin;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GUIBuilder {
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

    public static void createButtonsAndTools(Inventory inventory, VivDisplay vivDisplay) {
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

        createButtonAtXY(inventory, Material.PAPER,"details: ", 6,  5);
        ItemStack detailsButton = ItemManipulation.itemWithName(new ItemStack(Material.PAPER), "details: ");
        ItemMeta detailsButtonMeta = detailsButton.getItemMeta();
        assert detailsButtonMeta != null;

        ArrayList<String> details = new ArrayList<>();

        details.add("name: " + vivDisplay.displayName);
        details.add("type: " + vivDisplay.display.getType());

        if (vivDisplay.display instanceof TextDisplay){
            TextDisplay textDisplay = (TextDisplay) vivDisplay.display;

            String alignmentStr = "| | | |";
            switch (textDisplay.getAlignment()){
                case LEFT:
                    alignmentStr = "|<| | |";
                    break;
                case CENTER:
                    alignmentStr = "| |=| |";
                    break;
                case RIGHT:
                    alignmentStr = "| | |>|";
                    break;
            }

            details.add("text properties: align: " + alignmentStr);
            details.add("    text: " + textDisplay.getText());
            details.add("    background color: " + textDisplay.getBackgroundColor());
            details.add("    opacity: " + textDisplay.getTextOpacity());

        } else if (vivDisplay instanceof ItemDisplay || vivDisplay instanceof BlockDisplay) {
            // leave here in case details need to be added for these in the future
        }

        // todo: lore text as config with placeholders

        detailsButtonMeta.setLore(details);
        detailsButton.setItemMeta(detailsButtonMeta);

        ItemManipulation.setInventoryItemXY(inventory, detailsButton, 6, 5);
    }


    public static Inventory displayGUIBuilder(Player player) {
        System.out.println("displayGUIBuilder called");
        VivDisplay selectedVivDisplay = DisplayPlugin.selectedVivDisplays.get(player);
        if (selectedVivDisplay == null || selectedVivDisplay.display == null) return null; // player doesn't have a display selected, so ya can't make a gui for it-

        System.out.println("displayGUIBuilder called");
        if (selectedVivDisplay.display instanceof ItemDisplay || selectedVivDisplay.display instanceof BlockDisplay) {
            return standardDisplayGUIBuilder(selectedVivDisplay);
        } else if (selectedVivDisplay.display instanceof TextDisplay) {
            return textDisplayGUIBuilder(selectedVivDisplay);
        }
        return null;
    }

    public static Inventory standardDisplayGUIBuilder(VivDisplay vivDisplay) {
        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText("displayGUITitle"));
        createButtonsAndTools(inventory, vivDisplay);

        createButtonAtXY(inventory, Material.WRITABLE_BOOK, Texts.getText("displayGUIRenameButtonDisplayName"), 7, 5);
        ItemManipulation.setInventoryItemXY(inventory, ItemBuilder.makeGUIBook(), 0, 5);

        return inventory;
    }

    public static Inventory textDisplayGUIBuilder(VivDisplay vivDisplay) {
        TextDisplay textDisplay = (TextDisplay) vivDisplay.display;
        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText("displayGUITitle"));
        createButtonsAndTools(inventory, vivDisplay);

        ItemStack off = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack on = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

        TextDisplay.TextAlignment[] alignments = TextDisplay.TextAlignment.values();
        for (int i = 0; i < 3; i++) {
            TextDisplay.TextAlignment alignment = alignments[i];
            ItemStack pane = (textDisplay.getAlignment() == alignment) ? on : off;
            ItemManipulation.setInventoryItemXY(inventory, pane, i, 5);
        }

        return inventory;
    }


    public static Inventory displaySelectorGUIBuilder(List<VivDisplay> vivDisplays, String title, boolean isNearby) {
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int maxDisplaysToShow = 10;
        int specialCount = 0;

        vivDisplays.sort(Comparator.comparing(vivDisplay -> vivDisplay.displayName));

        for (int i = 0; i < maxDisplaysToShow && i < vivDisplays.size(); i++) {
            VivDisplay vivDisplay = vivDisplays.get(i);
            ItemStack button = ItemBuilder.buildDisplaySelectButton(vivDisplay);

            if (isNearby) {
                specialCount = placeButtonAndGetRenamedCount(button, vivDisplay, inventory, specialCount, i);
            } else {
                specialCount = placeButtonAndGetParentCount(button, vivDisplay, inventory, specialCount, i);
            }
        }
        return inventory;
    }

    private static int placeButtonAndGetParentCount(ItemStack button, VivDisplay vivDisplay, Inventory inventory, int parentCount, int i) {
        if (vivDisplay.isParent) { // add parented displays at begin, otherwise towards end
            inventory.setItem(9+ parentCount, button); // left to right starting at second row
            parentCount++;
        } else {
            inventory.setItem(27+ i - parentCount, button); // left to right starting at fourth row
        }
        return parentCount;
    }

    private static int placeButtonAndGetRenamedCount(ItemStack button, VivDisplay vivDisplay, Inventory inventory, int renamedCount, int i) {
        if (!vivDisplay.displayName.isEmpty() || vivDisplay.isParent) { // add renamed or parented displays at end, otherwise at begin
            inventory.setItem(53 - renamedCount, button);
            renamedCount++;
        } else {
            inventory.setItem(i - renamedCount, button);
        }
        return renamedCount;
    }
}
