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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GUIBuilder {
    static Material posButtonMaterial = Material.ORANGE_CONCRETE;
    static Material rotButtonMaterial = Material.LIME_CONCRETE;
    static Material sizeButtonMaterial = Material.LIGHT_BLUE_CONCRETE;
    static Material backButtonMaterial = Material.REDSTONE_BLOCK;

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

    public static void createButtonsAndTools(Inventory inventory, VivDisplay vivDisplay) {
        // buttons
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "x", 0, 1); // pos
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "y", 1, 1);
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "z", 2, 1);

        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "yaw", 3, 1); // rot
        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "pitch", 4, 1);
        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "roll", 5, 1);

        createPlusMinusButtonsAtXY(inventory, sizeButtonMaterial, "size x", 6, 1); // size
        createPlusMinusButtonsAtXY(inventory, sizeButtonMaterial, "size y", 7, 1);
        createPlusMinusButtonsAtXY(inventory, sizeButtonMaterial, "size z", 8, 1);

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

        createButtonAtXY(inventory, Material.PAPER,"details: ", 6,  5);
        ItemStack detailsButton = ItemManipulation.itemWithName(new ItemStack(Material.PAPER), "details: ");
        ItemMeta detailsButtonMeta = detailsButton.getItemMeta();
        assert detailsButtonMeta != null;

        ArrayList<String> details = new ArrayList<>();

        details.add("name: " + vivDisplay.displayName);
        details.add("type: " + vivDisplay.display.getType());

        int alignState = -1;

        if (vivDisplay.display instanceof TextDisplay){
            TextDisplay textDisplay = (TextDisplay) vivDisplay.display;

            String alignmentStr = "| | | |";
            switch (textDisplay.getAlignment()){
                case LEFT:
                    alignmentStr = "|<| | |";
                    alignState = -1;
                    break;
                case CENTER:
                    alignmentStr = "| |=| |";
                    alignState = 0;
                    break;
                case RIGHT:
                    alignmentStr = "| | |>|";
                    alignState = 1;
                    break;
            }

            String[] texts = textDisplay.getText().split("\n");
            String[] rawTexts = texts;
            int longestLen = 0;
            for (int i = 0; i < rawTexts.length; i++) {
                rawTexts[i] = rawTexts[i].replaceAll("§.", "");
                if (rawTexts[i].length() > longestLen) longestLen = rawTexts[i].length();
            }
            for (String line : texts) {
                String paddedText = padTextToLength(line, longestLen, alignState);

                details.add(paddedText);
            }
            //details.addAll(Arrays.asList(texts));

            details.add("text properties: align: " + alignmentStr);
            details.add("    background color: " + textDisplay.getBackgroundColor());
            details.add("    opacity: " + textDisplay.getTextOpacity());

        } else if (vivDisplay instanceof ItemDisplay || vivDisplay instanceof BlockDisplay) {
            // leave here in case details need to be added for these in the future
        }
        // todo: lore text as config with placeholders

        detailsButtonMeta.setLore(details);
        detailsButton.setItemMeta(detailsButtonMeta);

        createButtonAtXY(inventory, Material.WRITABLE_BOOK, Texts.getText("displayGUIRenameButtonDisplayName"), 7, 5);
        ItemManipulation.setInventoryItemXY(inventory, ItemBuilder.makeGUIBook(), 0, 5);

        ItemManipulation.setInventoryItemXY(inventory, detailsButton, 6, 5);
    }

    private static String padTextToLength(String text, int maxLen, int alignState) {
        int halfLength = (maxLen - text.length()) / 2;
        StringBuilder halfPaddingBuilder = new StringBuilder(halfLength);
        for (int j = 0; j < halfLength; j++) {
            halfPaddingBuilder.append("_");
        }
        String halfPadding = halfPaddingBuilder.toString();
        String paddingIfCenterOrFull = alignState == 0 ? halfPadding : halfPadding + halfPadding;
        String left = alignState == -1 ? "" : paddingIfCenterOrFull;
        String right = alignState == 1 ? "" : paddingIfCenterOrFull;
        return left + text + right;
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
        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText("displayGUITitle"));
        createButtonsAndTools(inventory, vivDisplay);
        return inventory;
    }

    public static Inventory textDisplayGUIBuilder(VivDisplay vivDisplay) {
        TextDisplay textDisplay = (TextDisplay) vivDisplay.display;
        Inventory inventory = Bukkit.createInventory(null, 54, Texts.getText("displayGUITitle"));
        createButtonsAndTools(inventory, vivDisplay);

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

        return inventory;
    }


    public static Inventory displaySelectorGUIBuilder(List<VivDisplay> vivDisplays, String title, boolean isNearby) {
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int maxDisplaysToShow = 36;
        int specialCount = 0;

        //vivDisplays.sort(Comparator.comparing(VivDisplay::getItemName));

        for (int i = 0; i < maxDisplaysToShow && i < vivDisplays.size(); i++) {
            System.out.println("b");
            VivDisplay vivDisplay = vivDisplays.get(i);
            ItemStack button = ItemBuilder.buildDisplaySelectButton(vivDisplay);

            System.out.println(button.getItemMeta().getDisplayName());

            if (vivDisplay.isParent) { // add parented displays at end, otherwise at begin
                System.out.println("p");
                inventory.setItem(36 + specialCount, button); // left to right starting at second row
                specialCount++;
            } else {
                System.out.println("!p");
                inventory.setItem(i - specialCount, button); // left to right starting at fourth row
            }
        }

        return inventory;
    }
}
