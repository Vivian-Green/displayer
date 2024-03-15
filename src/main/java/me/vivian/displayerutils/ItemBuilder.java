package me.vivian.displayerutils;

import me.vivian.displayer.commands.Main;
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

import java.util.ArrayList;
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

    public static ItemStack buildDisplaySelectButton(VivDisplay vivDisplay) {
        // get mateiral & name
        Material material = Material.BARRIER;
        String displayName = vivDisplay.displayName;

        if (vivDisplay.display instanceof BlockDisplay || vivDisplay.display instanceof ItemDisplay) {
            material = vivDisplay.getMaterial();
        } else if (vivDisplay.display instanceof TextDisplay) {
            material = Material.NAME_TAG;
        }

        if (displayName.isEmpty()) { // unnamed vivDisplay, use default name
            displayName = material == Material.NAME_TAG ? "Text" : material + " Display";
        }

        // return button with material & name
        return buildSelectButtonItem(vivDisplay, material, displayName);
    }

    private static ItemStack buildSelectButtonItem(VivDisplay vivDisplay, Material material, String displayName) {
        // build button
        ItemStack button = new ItemStack(material);
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(displayName);
        if (vivDisplay.display instanceof TextDisplay){
            // set lore to TextDisplay text
            ArrayList<String> lore = new ArrayList<>();
            lore.add(((TextDisplay) vivDisplay.display).getText());
            buttonMeta.setLore(lore);
        }
        button.setItemMeta(buttonMeta);

        // bind UUID to button nbt
        PersistentDataContainer dataContainer = buttonMeta.getPersistentDataContainer();
        UUID displayUUID = vivDisplay.display.getUniqueId();
        dataContainer.set(new NamespacedKey(Main.getPlugin(), "displayUUID"), PersistentDataType.STRING, displayUUID.toString());

        // add enchantment glint to named Displays
        if (vivDisplay.displayName.isEmpty()){
            return button;
        }
        return ItemManipulation.addEnchantmentGlint(button);
    }
}
