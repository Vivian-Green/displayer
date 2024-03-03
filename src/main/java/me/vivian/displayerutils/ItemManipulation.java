package me.vivian.displayerutils;

import me.vivian.displayer.config.Texts;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides utility methods for manipulating inventory items.
 */
public class ItemManipulation {
    public ItemManipulation() {}

    static Map<String, String> errMap = Texts.getErrors();

    // Reduces the count of the (player)'s held item by 1. If the new count <= 0, the remove it.
    public static void takeFromHeldItem(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        heldItem.setAmount(heldItem.getAmount() - 1);
        if (heldItem.getAmount() <= 0) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            player.getInventory().setItemInMainHand(heldItem);
        }
    }

    // Checks if a (player) is holding a displayable item
    public static boolean isHeldItemValid(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType() == Material.AIR) {
            player.sendMessage();
            return false;
        }
        return true;
    }

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
        // TODO URGENT: Ensure slot is empty first! This can delete shit!

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

        if (!name.isEmpty()) itemMeta.setDisplayName(name);
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


