package me.vivian.displayerutils;

import me.vivian.displayer.config.Texts;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides utility methods for manipulating inventory items.
 */
public class ItemManipulation { // todo: make static
    public ItemManipulation() {}

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
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            player.sendMessage(Texts.getError("displayEmptyHand"));
            return false;
        }
        return true;
    }

    /**
     * Sets a specific slot (X, Y) of the given (inventory) to (itemStack).
     *
     * @param inventory  The target inventory.
     * @param itemStack  The item to set.
     * @param X          The X coordinate of the slot.
     * @param Y          The Y coordinate of the slot.
     */
    public static void setInventoryItemXY(Inventory inventory, ItemStack itemStack, Integer X, Integer Y) {
        // TODO URGENT: Ensure slot is empty first! This can delete shit!

        int slot = Y * 9 + X;
        if (slot >= inventory.getSize()) return;

        inventory.setItem(slot, itemStack);
    }

    /**
     * Sets the display (name) of an (itemStack).
     *
     * @param itemStack  The ItemStack to modify.
     * @param name  The new display name.
     * @return      The modified ItemStack.
     */
    public static ItemStack itemWithName(ItemStack itemStack, String name) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (!name.isEmpty()) itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static ItemStack addEnchantmentGlint(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.LURE, 1, true); // todo: there's definitely a better way to do this - least useful thing I could think of in case of somehow accidentally whoopsie daisy
        item.setItemMeta(meta);
        return item;
    }
}


