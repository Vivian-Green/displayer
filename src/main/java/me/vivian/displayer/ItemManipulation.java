package me.vivian.displayer;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Provides utility methods for manipulating inventory items.
 */
public class ItemManipulation {

    /**
     * constructor
     */
    public ItemManipulation(){}

    /**
     * Checks if a slot (index) is out of bounds for the given (inventory size).
     *
     * @param index   The slot index to check.
     * @param invSize The size of the inventory.
     * @return        True if the slot index is out of bounds, otherwise false.
     */
    public boolean isSlotOOB(Integer index, Integer invSize) {
        if (index > invSize - 1) {
            System.out.println("Addressing out-of-bounds slot " + index + " of max " + (invSize - 1));
            return true;
        }
        return false;
    }

    /**
     * Sets a specific slot (X, Y) of the given (inventory) to (itemStack).
     *
     * @param inventory  The target inventory.
     * @param itemStack  The item to set.
     * @param X          The X coordinate of the slot.
     * @param Y          The Y coordinate of the slot.
     */
    public void setInventoryItemXY(Inventory inventory, ItemStack itemStack, Integer X, Integer Y) {
        // TODO: Check if the slot is empty first or empty it first if needed.
        int index = Y * 9 + X;
        if (isSlotOOB(index, inventory.getSize())) {
            return;
        }
        inventory.setItem(index, itemStack);
    }

    /**
     * gets the item from slot (X, Y) of the given (inventory).
     *
     * @param inventory  The target inventory.
     * @param X          The X coordinate of the slot.
     * @param Y          The Y coordinate of the slot.
     * @return           The ItemStack in the specified slot or null if out of bounds.
     */
    public ItemStack getInventoryItemXY(Inventory inventory, Integer X, Integer Y) {
        int index = Y * 9 + X;
        if (isSlotOOB(index, inventory.getSize())) {
            return null;
        }
        return inventory.getItem(Y * 9 + X);
    }

    /**
     * Sets the display (name) of an (itemStack).
     *
     * @param itemStack  The ItemStack to modify.
     * @param name  The new display name.
     * @return      The modified ItemStack.
     */
    public ItemStack itemWithName(ItemStack itemStack, String name) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Sets the (lore) of an (itemStack).
     *
     * @param itemStack  The ItemStack to modify.
     * @param lore  The new lore.
     * @return      The modified ItemStack.
     */
    public ItemStack itemWithLore(ItemStack itemStack, ArrayList<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}


