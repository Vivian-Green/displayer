package me.vivian.displayerutils;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ItemBuilder {
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

    public static ItemStack makeDisplaySelectButton(VivDisplay vivDisplay) {
        Material material;
        ItemStack button;
        ItemMeta buttonMeta = null;
        if (vivDisplay.display instanceof BlockDisplay) {
            material = ((BlockDisplay) vivDisplay.display).getBlock().getMaterial();
            button = new ItemStack(material);
            buttonMeta = button.getItemMeta();
            buttonMeta.setDisplayName(vivDisplay.displayName);
        } else if (vivDisplay.display instanceof ItemDisplay) {
            button = ((ItemDisplay) vivDisplay.display).getItemStack();
            buttonMeta = button.getItemMeta();
            buttonMeta.setDisplayName(vivDisplay.displayName);
        } else if(vivDisplay instanceof TextDisplay) {
            TextDisplay textDisplay = (TextDisplay) vivDisplay.display;
            button = new ItemStack(Material.NAME_TAG);

            buttonMeta = button.getItemMeta();
            buttonMeta.setDisplayName(textDisplay.getText());
        } else {
            button = new ItemStack(Material.BARRIER);
            buttonMeta = button.getItemMeta();
            buttonMeta.setDisplayName("unexpected/invalid display type named: " + vivDisplay.displayName);
        }

        PersistentDataContainer dataContainer = buttonMeta.getPersistentDataContainer();
        UUID displayUUID = vivDisplay.display.getUniqueId();
        dataContainer.set(new NamespacedKey(CommandHandler.getPlugin(), "displayUUID"), PersistentDataType.STRING, displayUUID.toString());

        button.setItemMeta(buttonMeta);

        if (!vivDisplay.displayName.isEmpty()){
            return ItemManipulation.addEnchantmentGlint(button);
        }
        return button;
    }
}
