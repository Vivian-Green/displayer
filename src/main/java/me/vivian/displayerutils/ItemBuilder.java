package me.vivian.displayerutils;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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
        meta.setLore(Texts.getTexts("displayGUIBookLore"));

        // Apply the book meta to the book
        book.setItemMeta(meta);
        return book;
    }

    public static ItemStack makeDisplaySelectButton(VivDisplay vivDisplay) {
        Material material = Material.BARRIER;
        String displayName = "unexpected/invalid display type named: " + vivDisplay.displayName;

        if (vivDisplay.display instanceof BlockDisplay) {
            material = ((BlockDisplay) vivDisplay.display).getBlock().getMaterial();
            displayName = vivDisplay.displayName;
        } else if (vivDisplay.display instanceof ItemDisplay) {
            material = ((ItemDisplay) vivDisplay.display).getItemStack().getType();
            displayName = vivDisplay.displayName;
        } else if (vivDisplay.display instanceof TextDisplay) {
            material = Material.NAME_TAG;
            displayName = ((TextDisplay) vivDisplay.display).getText();
        }

        ItemStack button = new ItemStack(material);
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(displayName);

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
