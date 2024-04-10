package me.vivian.displayerutils;

import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GUIBuilder {
    static Material posButtonMaterial = Material.ORANGE_CONCRETE;
    static Material rotButtonMaterial = Material.LIME_CONCRETE;
    static Material sizeButtonMaterial = Material.LIGHT_BLUE_CONCRETE;
    static Material backButtonMaterial = Material.REDSTONE_BLOCK;

    static Inventory baseStandardDisplayGUIInventory = null;

    public static void init() {
        baseStandardDisplayGUIInventory = createButtonsAndToolsPrefab();
    }

    // Creates an ItemStack in the (inventory) with the specified (material) and (displayName) at the given (x, y) coordinates.
    public static void createButtonAtXY(Inventory inventory, Material material, String displayName, int x, int y) {
        if (displayName == null) displayName = "";
        ItemStack button = new ItemStack(material);
        button = ItemManipulation.itemWithName(button, displayName);
        ItemManipulation.setInventoryItemXY(inventory, button, x, y);
    }

    public static void createPlusMinusButtonsAtXY(Inventory inventory, Material material, String displayName, int x, int y) {
        createButtonAtXY(inventory, material, "+" + displayName, x, y);
        createButtonAtXY(inventory, material, "-" + displayName, x, y + 1);
    }

    public static void createVectorPanel(Inventory inventory, Material material, List<String> names, int topLeftX, int topLeftY) {
        if (names.size() != 3) {
          System.out.println("created vector panel with invalid number of names??");
          return;
        }

        for (int i = 0; i < 3; i++) {
            createPlusMinusButtonsAtXY(inventory, material, names.get(i), topLeftX+i, topLeftY);
        }

    }

    public static Inventory createButtonsAndToolsPrefab() { // runs once & is then copied-
        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText("displayGUITitle"));
        // buttons
        createVectorPanel(inventory, posButtonMaterial, List.of("x", "y", "z"), 0, 1);
        createVectorPanel(inventory, rotButtonMaterial, List.of("yaw", "pitch", "roll"), 3, 1);
        createVectorPanel(inventory, sizeButtonMaterial, List.of("size x", "size y", "size z"), 6, 1);

        // tool displays
        createButtonAtXY(inventory, Material.LEAD, Texts.getText("displayGUIMovePanelDisplayName"), 2, 3);
        createButtonAtXY(inventory, Material.SPECTRAL_ARROW, Texts.getText("displayGUIRotatePanelDisplayName"), 5, 3);
        createButtonAtXY(inventory, Material.BLAZE_ROD, Texts.getText("displayGUIResizePanelDisplayName"), 7, 3);

        // arrow keys
        createButtonAtXY(inventory, Material.BRICK, Texts.getText("displayGUILookUpName"), 3, 3);
        createButtonAtXY(inventory, Material.BRICK, Texts.getText("displayGUILookLeftName"), 2, 4);
        createButtonAtXY(inventory, Material.BRICK, Texts.getText("displayGUILookDownName"), 3, 4);
        createButtonAtXY(inventory, Material.BRICK, Texts.getText("displayGUILookRightName"), 4, 4);

        // back button
        createButtonAtXY(inventory, backButtonMaterial, Texts.getText("displayGUIBackButtonDisplayName"), 0, 0);
        createButtonAtXY(inventory, Material.BOOK, Texts.getText("displayGUIGroupButtonDisplayName"), 0, 3);

        createButtonAtXY(inventory, Material.WRITABLE_BOOK, Texts.getText("displayGUIRenameButtonDisplayName"), 7, 5);
        ItemManipulation.setInventoryItemXY(inventory, ItemBuilder.makeGUIBook(), 0, 5);

        return inventory;
    }

    public static Inventory createButtonsAndTools(VivDisplay vivDisplay) {
        if (baseStandardDisplayGUIInventory == null) baseStandardDisplayGUIInventory = createButtonsAndToolsPrefab();
        Inventory inventory = baseStandardDisplayGUIInventory;
        ItemManipulation.setInventoryItemXY(inventory, ItemBuilder.createDetailsButton(vivDisplay), 6, 5);
        return inventory;
    }

    public static Inventory displayGUIBuilder(Player player) {
        System.out.println("displayGUIBuilder called");
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
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
        return createButtonsAndTools(vivDisplay);
    }

    public static void buildTextAlignmentSelector(Inventory inventory, TextDisplay textDisplay) {
        ItemStack off = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack on = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

        TextDisplay.TextAlignment[] alignments = TextDisplay.TextAlignment.values();

        if (alignments[0] == TextDisplay.TextAlignment.CENTER) {
            TextDisplay.TextAlignment temp = alignments[0];
            alignments[0] = alignments[1];
            alignments[1] = temp; // swap - why tf is it center left right??
        }

        for (int i = 0; i < 3; i++) {
            TextDisplay.TextAlignment alignment = alignments[i];

            ItemStack pane = (textDisplay.getAlignment() == alignment) ? on : off;
            ItemManipulation.setInventoryItemXY(inventory, pane, i, 5);
        }
    }

    public static Inventory textDisplayGUIBuilder(VivDisplay vivDisplay) {
        TextDisplay textDisplay = (TextDisplay) vivDisplay.display;
        Inventory inventory = createButtonsAndTools(vivDisplay);

        buildTextAlignmentSelector(inventory, textDisplay);
        return inventory;
    }


    public static Inventory displaySelectorGUIBuilder(List<VivDisplay> vivDisplays, String title) {
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int maxDisplaysToShow = 36;
        int specialCount = 0;

        for (int i = 0; i < maxDisplaysToShow && i < vivDisplays.size(); i++) {
            VivDisplay vivDisplay = vivDisplays.get(i);
            ItemStack button = ItemBuilder.buildDisplaySelectButton(vivDisplay);

            if (vivDisplay.isParent) { // add parented displays at end, otherwise at begin
                inventory.setItem(36 + specialCount, button); // left to right starting at fifth row
                specialCount++;
            } else {
                inventory.setItem(i - specialCount, button); // left to right starting at first row
            }
        }

        return inventory;
    }
}
