package me.vivian.displayerutils;

import me.vivian.displayer.DisplayPlugin;
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
import java.util.List;
import java.util.UUID;

public class ItemBuilder {
    private static DisplayPlugin plugin;

    public static void init(DisplayPlugin thisPlugin){
        plugin = thisPlugin;
    }
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
        // get material & name
        Material material = Material.BARRIER;
        String displayName = vivDisplay.getItemName();

        if (vivDisplay.display instanceof BlockDisplay || vivDisplay.display instanceof ItemDisplay) {
            material = vivDisplay.getMaterial();
        } else if (vivDisplay.display instanceof TextDisplay) {
            material = Material.NAME_TAG;
        }

        // return button with material & name
        return buildSelectButtonItem(vivDisplay, material, displayName);
    }

    private static ItemStack buildSelectButtonItem(VivDisplay vivDisplay, Material material, String displayName) {
        //System.out.println("building select button...");
        // build button
        ItemStack button = new ItemStack(material);
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(displayName);
        if (vivDisplay.display instanceof TextDisplay){
            // set lore to TextDisplay text
            ArrayList<String> lore = new ArrayList<>(List.of(((TextDisplay) vivDisplay.display).getText().split("\n")));
            buttonMeta.setLore(lore);
        }

        // bind UUID to button nbt
        PersistentDataContainer dataContainer = buttonMeta.getPersistentDataContainer();

        UUID displayUUID = vivDisplay.display.getUniqueId();
        dataContainer.set(new NamespacedKey(plugin, "displayUUID"), PersistentDataType.STRING, displayUUID.toString());

        button.setItemMeta(buttonMeta);

        // add enchantment glint to named Displays
        if (vivDisplay.displayName.isEmpty()){
            return button;
        }
        return ItemManipulation.addEnchantmentGlint(button);
    }

    public static ItemStack createDetailsButton(VivDisplay vivDisplay) {
        ItemStack detailsButton = ItemManipulation.itemWithName(new ItemStack(Material.PAPER), "details: ");
        ItemMeta detailsButtonMeta = detailsButton.getItemMeta();
        assert detailsButtonMeta != null;

        ArrayList<String> details = new ArrayList<>();

        details.add("name: " + vivDisplay.displayName);
        details.add("type: " + vivDisplay.display.getType());

        int alignState = -1;

        if (vivDisplay.display instanceof TextDisplay){
            TextDisplay textDisplay = (TextDisplay) vivDisplay.display;

            String alignmentStr = "";
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
                String paddedText = MiscUtils.padTextToLength(line, longestLen, alignState);

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
        return detailsButton;
    }
}
