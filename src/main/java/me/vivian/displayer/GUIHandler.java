package me.vivian.displayer;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Texts;
import me.vivian.displayerutils.ItemManipulation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class GUIHandler {
    public static ItemManipulation itemManipulation;
    public static ItemStack makeGUIBook() {
        // Create a new book
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        // Set the title and author
        meta.setTitle(Texts.getText("displayGUIBookTitle"));
        meta.setAuthor(Texts.getText("displayGUIBookAuthor"));



        /*        // Determine the multiplier based on shift-click & right click
        // Regular click: 1
        // Shift click: 10
        // Right click OR shift right click: 0.1*/

        meta.setLore(Texts.getTexts("displayGUIBookLore"));

        // Apply the book meta to the book
        book.setItemMeta(meta);
        return book;
    }

    // Creates an ItemStack in the (inventory) with the specified (material) and (displayName) at the given (x, y) coordinates.
    public static void createButtonAtXY(Inventory inventory, Material material, String displayName, int x, int y) {
        if (itemManipulation == null) itemManipulation = new ItemManipulation();

        ItemStack button = new ItemStack(material);
        button = itemManipulation.itemWithName(button, displayName);
        itemManipulation.setInventoryItemXY(inventory, button, x, y);
    }

    public static void createPlusMinusButtonsAtXY(Inventory inventory, Material material, String displayName, int x, int y) {
        createButtonAtXY(inventory, material, "+" + displayName, x, y);
        createButtonAtXY(inventory, material, "-" + displayName, x, y + 1);
    }

    public static Inventory displayGUIBuilder() {
        if (itemManipulation == null) itemManipulation = new ItemManipulation();
        // todo: move materials & names to config

        Inventory inventory = Bukkit.createInventory(null, 54, "display GUI");

        Material posButtonMaterial = Material.ORANGE_CONCRETE;
        Material rotButtonMaterial = Material.LIME_CONCRETE;
        Material sizeButtonMaterial = Material.LIGHT_BLUE_CONCRETE;

        // buttons
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "x", 1, 1); // pos
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "y", 2, 1);
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "z", 3, 1);

        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "yaw", 4, 1); // rot
        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "pitch", 5, 1);
        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "roll", 6, 1);

        createPlusMinusButtonsAtXY(inventory, sizeButtonMaterial, "size", 7, 1); // size

        // tool displays
        createButtonAtXY(inventory, Material.LEAD, "move tool", 2, 3);
        createButtonAtXY(inventory, Material.SPECTRAL_ARROW, "rotate tool", 5, 3);
        createButtonAtXY(inventory, Material.BLAZE_ROD, "resize tool", 7, 3);

        // book
        itemManipulation.setInventoryItemXY(inventory, makeGUIBook(), 0, 5);

        return inventory;
    }
}
