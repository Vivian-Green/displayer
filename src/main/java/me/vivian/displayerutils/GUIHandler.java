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
import java.util.UUID;

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

        Inventory inventory = Bukkit.createInventory(null, 54, "display GUI"); // todo: config this

        Material posButtonMaterial = Material.ORANGE_CONCRETE;
        Material rotButtonMaterial = Material.LIME_CONCRETE;
        Material sizeButtonMaterial = Material.LIGHT_BLUE_CONCRETE;

        // buttons
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "x", 1, 1); // pos // todo: config this
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "y", 2, 1);
        createPlusMinusButtonsAtXY(inventory, posButtonMaterial, "z", 3, 1);

        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "yaw", 4, 1); // rot
        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "pitch", 5, 1);
        createPlusMinusButtonsAtXY(inventory, rotButtonMaterial, "roll", 6, 1);

        createPlusMinusButtonsAtXY(inventory, sizeButtonMaterial, "size", 7, 1); // size

        // tool displays
        createButtonAtXY(inventory, Material.LEAD, "move tool", 2, 3); // todo: config this
        createButtonAtXY(inventory, Material.SPECTRAL_ARROW, "rotate tool", 5, 3);
        createButtonAtXY(inventory, Material.BLAZE_ROD, "resize tool", 7, 3);

        createButtonAtXY(inventory, Material.WRITABLE_BOOK, "rename", 7, 5);

        // book
        itemManipulation.setInventoryItemXY(inventory, makeGUIBook(), 0, 5);

        return inventory;
    }

    public static Inventory displayNearbyGUIBuilder(List<VivDisplay> nearbyVivDisplays) {
        if (itemManipulation == null) itemManipulation = new ItemManipulation();

        Inventory inventory = Bukkit.createInventory(null, 54, "nearby displays"); // todo: config this

        int maxDisplaysToShow = 10;
        for (int index = 0; index < maxDisplaysToShow && index < nearbyVivDisplays.size(); index++) {
            Material material;
            VivDisplay vivDisplay = nearbyVivDisplays.get(index);
            if (vivDisplay.display instanceof BlockDisplay) {
                material = ((BlockDisplay) vivDisplay.display).getBlock().getMaterial();
            } else if (vivDisplay.display instanceof ItemDisplay) {
                ItemStack itemStack = ((ItemDisplay) vivDisplay.display).getItemStack();
                assert itemStack != null;
                material = itemStack.getType();
            } else {
                material = Material.AIR;
            }

            ItemStack button = new ItemStack(material);
            String name = vivDisplay.display.getName();
            button = itemManipulation.itemWithName(button, name);

            ItemMeta buttonMeta = button.getItemMeta();
            if (buttonMeta == null) continue; // shouldn't happen but makes the yellow squiggle go away & feels more explicit than an assertion-

            PersistentDataContainer dataContainer = buttonMeta.getPersistentDataContainer();
            UUID displayUUID = vivDisplay.display.getUniqueId();
            dataContainer.set(new NamespacedKey(CommandHandler.getPlugin(), "displayUUID"), PersistentDataType.STRING, displayUUID.toString());

            button.setItemMeta(buttonMeta);

            // todo: put display UUID in item nbt

            inventory.setItem(index, button);
        }

        return inventory;
    }
}
