package me.vivian.displayer;

import me.vivian.displayerutils.NBTMagic;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.bukkit.Location;
import org.joml.Vector3d;

import java.util.Map;


// todo: return strings instead of needing player as arg
// todo: handle case display is null
/**
 * Wrapper class for a display entity in the game world, used to hold display values
 * Provides methods to interact with and manage displays, including creation, destruction, and retrieval
 * of display information.
 */
public class VivDisplay{
    Display display;
    Plugin plugin;
    NBTMagic nbtm;

    public String displayName;
    public String parentUUID;
    public boolean isChild;
    public boolean isParent;
    public VivDisplay(Plugin thisPlugin, Display thisDisplay) {
        // todo: handle null
        plugin = thisPlugin;
        display = thisDisplay;
        nbtm = new NBTMagic(plugin);

        displayName = nbtm.getNBT(display, "VivDisplayName", String.class);
        parentUUID = nbtm.getNBT(display, "VivDisplayParentUUID", String.class);
        isChild = nbtm.getNBT(display, "VivDisplayIsChild", Boolean.class);
        isParent = nbtm.getNBT(display, "VivDisplayIsParent", Boolean.class);
    }

    public Boolean isThisParent() {
        isParent = nbtm.getNBT(display, "VivDisplayIsParent", Boolean.class);
        return isParent;
    }

    public VivDisplay(Plugin thisPlugin, World world, Location location, EntityType entityType, Object displayData) {
        // todo: handle null
        plugin = thisPlugin;
        createDisplay(world, location, entityType, displayData);
        nbtm = new NBTMagic(plugin);

        displayName = nbtm.getNBT(display, "VivDisplayName", String.class);
        parentUUID = nbtm.getNBT(display, "VivDisplayParentUUID", String.class);
        isChild = nbtm.getNBT(display, "VivDisplayIsChild", Boolean.class);
        isParent = nbtm.getNBT(display, "VivDisplayIsParent", Boolean.class);
    }

    /**
     * Creates and spawns a Display entity in the specified (world) at a given (location).
     *
     * @param world        The world in which the Display is created.
     * @param location     The location at which the Display is spawned.
     * @param entityType   The EntityType of the Display (e.g., BLOCK_DISPLAY or ITEM_DISPLAY).
     * @param displayData  The data associated with the Display (BlockData for BLOCK_DISPLAY or ItemStack for ITEM_DISPLAY).
     * @return The newly created Display entity.
     */
    private Display createDisplay(World world, Location location, EntityType entityType, Object displayData) {
        display = (Display) world.spawnEntity(location, entityType);

        if (display instanceof BlockDisplay && displayData instanceof BlockData) {
            ((BlockDisplay) display).setBlock((BlockData) displayData);
        } else if (display instanceof ItemDisplay && displayData instanceof ItemStack) {
            ((ItemDisplay) display).setItemStack((ItemStack) displayData);
        } else {
            System.out.println("createDisplay: Unhandled display type or display data mismatch.");
        }

        return display;
    }

    /**
     * Destroys and removes the specified VivDisplay for the given player.
     *
     * @param player      The player initiating the destruction.
     * @param vivDisplays The map of VivDisplays where the VivDisplay should be removed from.
     */
    public void destroy(Player player, Map<String, VivDisplay> vivDisplays, Map<Player, VivDisplay> selectedVivDisplays) {
        // todo: remove from selectedVivDisplays too
        if (display != null) {
            try {
                player.getWorld().dropItemNaturally(display.getLocation(), getItemStackFromDisplay(display));
            } catch (Exception e) {
                System.out.println("destroyDisplay(): Failed to spawn item on display destroy. Is this an unsupported display type?");
                System.out.println("destroyDisplay(): Destroying anyway after this stack trace:");
                e.printStackTrace();
            } finally {
                // Get the UUID of the display
                String displayUUID = display.getUniqueId().toString();

                display.remove();
                player.sendMessage("Display destroyed.");

                if (vivDisplays.containsKey(displayUUID)) {
                    vivDisplays.remove(displayUUID);
                } else {
                    System.out.println("destroyDisplay(): Display not found in vivDisplays map.");
                }
                if (selectedVivDisplays.containsKey(player)) {
                    selectedVivDisplays.remove(player);
                } else {
                    System.out.println("destroyDisplay(): Display not found in selectedVivDisplays map.");
                }
            }
        } else {
            System.out.println("destroyDisplay(): tried to destroy a display that was null");
            player.sendMessage("You must first select a Display");
        }
    }
    public ItemStack getItemStack() {
        return getItemStackFromDisplay(display);
    }

    /**
     * Retrieves an ItemStack from a (display).
     *
     * @param display The Display to get the ItemStack from.
     * @return The ItemStack representing the Display, or null if unsupported.
     */
    public static ItemStack getItemStackFromDisplay(Display display) {
        // todo: consider switch statement when adding TextDisplay
        if (display instanceof ItemDisplay) {
            // If ItemDisplay, return its ItemStack directly
            ItemDisplay itemDisplay = (ItemDisplay) display;
            return itemDisplay.getItemStack();
        } else if (display instanceof BlockDisplay) {
            // If BlockDisplay, create an ItemStack based on the block material
            BlockDisplay blockDisplay = (BlockDisplay) display;
            Material material = blockDisplay.getBlock().getMaterial();
            return new ItemStack(material, 1);
        } else {
            System.out.println("getItemStackFromDisplay(): Unsupported display type");
            return null;
        }
    }

    /**
     * Renames this VivDisplay.
     *
     * @param newName The new name to set for the display.
     * @return True if the rename operation was successful, false otherwise.
     */
    public String rename(String newName) {
        try {
            nbtm.setNBT(display, "VivDisplayName", newName);
            displayName = newName;
            return "The display is now called " + newName;
        } catch (Exception e) {
            System.out.println("renameDisplay(): Failed to rename display. Error: " + e.getMessage());
            return "Failed to rename the display. Please try again later.";
        }
    }

    /**
     * Sets the parent of this VivDisplay.
     *
     * @param parentDisplay The parent display to set.
     * @return True if the operation was successful, false otherwise.
     */
    public String  setParent(Display parentDisplay) {
        if (parentDisplay == null) {
            return("Parent display not found");
        }

        // Get the UUID of the parent display
        parentUUID = String.valueOf(parentDisplay.getUniqueId());
        isChild = true;

        nbtm.setNBT(display, "VivDisplayParentUUID", parentUUID);
        nbtm.setNBT(parentDisplay, "VivDisplayIsParent", true);
        nbtm.setNBT(display, "VivDisplayIsChild", true);
        return "";
    }

    /**
     * Unsets the parent of this VivDisplay.
     *
     * @return err string, empty if ok
     */
    public String unsetParent() {
        // Get the UUID of the parent display from the display's NBT
        String parentUUIDStr = nbtm.getNBT(display, "VivDisplayParentUUID", String.class);

        if (!isChild || parentUUIDStr == null || parentUUIDStr.isEmpty()) {
            return "The display is not a child display and does not have a parent.";
        }

        parentUUID = "";
        isChild = false;

        // Clear the parent-related NBT tags
        nbtm.setNBT(display, "VivDisplayParentUUID", "");
        nbtm.setNBT(display, "VivDisplayIsChild", false);

        // todo: Update the IsParent property of the parent display and handle other related tasks.

        return "";
    }



    /**
     * Changes the scale of this VivDisplay by the specified offset.
     *
     * @param sizeOffset The offset to add to the current scale.
     * @param player     The player performing the scale change.
     * @return True if the scale change was successful, false otherwise.
     */
    public boolean changeScale(double sizeOffset, Player player) {
        Transformation transformation = display.getTransformation();
        double currentScale = transformation.getScale().x;
        double newScale = currentScale + sizeOffset;

        if (newScale > 0.0) {
            transformation.getScale().set(newScale);
            display.setTransformation(transformation);
            return true;
        } else {
            if (player != null) {
                player.sendMessage("Invalid scale value. Scale must be greater than 0.0.");
            }
            return false;
        }
    }

    /**
     * Sets the scale of this VivDisplay to the specified value.
     *
     * @param newSize The new scale value to set.
     * @param player  The player performing the scale setting.
     * @return True if the scale setting was successful, false otherwise.
     */
    public boolean setScale(double newSize, Player player) {
        if (newSize > 0.0) {
            Transformation transformation = display.getTransformation();
            transformation.getScale().set(newSize);
            display.setTransformation(transformation);
            if (player != null) {
                player.sendMessage("Display scale set to " + newSize);
            }
            return true;
        } else {
            if (player != null) {
                player.sendMessage("Invalid scale value. Scale must be greater than 0.0.");
            }
            return false;
        }
    }

    public float getScale() {
        return (display.getTransformation().getScale().x+display.getTransformation().getScale().y+display.getTransformation().getScale().z)/3;
    }

    /**
     * Changes the rotation of this VivDisplay by the specified offsets.
     *
     * @param yawOffset   The yaw offset to add to the current rotation (in degrees).
     * @param pitchOffset The pitch offset to add to the current rotation (in degrees).
     * @param rollOffset  The roll offset to add to the current rotation (in degrees).
     * @param player      The player performing the rotation change.
     * @return True if the rotation change was successful, false otherwise.
     */
    public boolean changeRotation(float yawOffset, float pitchOffset, float rollOffset, Player player) {
        // Convert offsets from degrees to radians
        yawOffset = (float) Math.toRadians(yawOffset);
        pitchOffset = (float) Math.toRadians(pitchOffset);
        rollOffset = (float) Math.toRadians(rollOffset);

        Transformation transformation = display.getTransformation();

        // Create a new transformation with updated rotation
        Transformation newTransformation = new Transformation(
                transformation.getTranslation(),
                transformation.getLeftRotation(),
                transformation.getScale(),
                transformation.getRightRotation()
        );

        // Apply the rotation offsets
        newTransformation.getLeftRotation().rotateYXZ(rollOffset, pitchOffset, yawOffset);

        display.setTransformation(newTransformation);
        return true;
    }

    /**
     * Sets the rotation of this VivDisplay to the specified values.
     *
     * @param yawOffset   The yaw offset to set (in degrees).
     * @param pitchOffset The pitch offset to set (in degrees).
     * @param rollOffset  The roll offset to set (in degrees).
     * @param player      The player performing the rotation setting.
     * @return True if the rotation setting was successful, false otherwise.
     */
    public boolean setRotation(float yawOffset, float pitchOffset, float rollOffset, Player player) {
        // Convert offsets from degrees to radians
        yawOffset = (float) Math.toRadians(yawOffset);
        pitchOffset = (float) Math.toRadians(pitchOffset);
        rollOffset = (float) Math.toRadians(rollOffset);

        Transformation transformation = display.getTransformation();

        // Create a new quaternion for the specified rotation
        Quaternionf rotation = new Quaternionf().rotationYXZ(rollOffset, pitchOffset, yawOffset);

        // Set the rotation directly
        transformation.getLeftRotation().set(rotation);

        display.setTransformation(transformation);

        // Only send the message if player is not null
        if (player != null) {
            player.sendMessage("Rotation set for the selected Display.");
        }
        return true;
    }

    /**
     * Changes the position of this VivDisplay by the specified offsets.
     *
     * @param xOffset The X-axis offset to add to the current position.
     * @param yOffset The Y-axis offset to add to the current position.
     * @param zOffset The Z-axis offset to add to the current position.
     * @param player  The player performing the position change.
     * @return True if the position change was successful, false otherwise.
     */
    public boolean changePosition(double xOffset, double yOffset, double zOffset) {
        Location currentLocation = display.getLocation();
        double newX = currentLocation.getX() + xOffset;
        double newY = currentLocation.getY() + yOffset;
        double newZ = currentLocation.getZ() + zOffset;

        // Store the current rotation
        float currentYaw = currentLocation.getYaw();
        float currentPitch = currentLocation.getPitch();

        // Teleport to the new position
        display.teleport(new Location(currentLocation.getWorld(), newX, newY, newZ, currentYaw, currentPitch));

        return true;
    }

    public boolean changePosition(Vector3d offset) {
        return changePosition(offset.x, offset.y, offset.z);
    }

    /**
     * Sets the position of this VivDisplay to the specified values.
     *
     * @param x      The X-coordinate to set.
     * @param y      The Y-coordinate to set.
     * @param z      The Z-coordinate to set.
     * @param player The player performing the position setting.
     * @return True if the position setting was successful, false otherwise.
     */
    public boolean setPosition(double x, double y, double z, Player player) {
        // Store the current rotation
        float currentYaw = display.getLocation().getYaw();
        float currentPitch = display.getLocation().getPitch();

        // Teleport to the new position with the current rotation
        display.teleport(new Location(display.getLocation().getWorld(), x, y, z, currentYaw, currentPitch));

        // Only send the message if player is not null
        if (player != null) {
            player.sendMessage("Position set for the selected Display.");
        }

        return true;
    }

    public Vector3d getPosition() {
        Location location = display.getLocation();
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }


    /**
     * spawns particles at this display
     */
    public void spawnParticle(Particle particle, Integer count) {
        Location displayLocation = display.getLocation();

        if (particle == null) {
            particle = Particle.ENCHANTMENT_TABLE;
        }
        if (count == null) {
            count = 100;
        }

        displayLocation.getWorld().spawnParticle(
                particle,
                displayLocation,
                count
        );
    }
}