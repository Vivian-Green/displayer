package me.vivian.displayer.display;

import me.vivian.displayer.config.Texts;
import me.vivian.displayerutils.CommandParsing;
import me.vivian.displayerutils.NBTMagic;
import me.vivian.displayerutils.TMath;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.bukkit.Location;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.UUID;


// todo: return strings instead of needing player as arg
// todo: handle case display is null

// todo: add errs & texts to texts_en.yml
/**
 * Wrapper class for a display entity in the game world, used to hold display values
 * Provides methods to interact with and manage displays, including creation, destruction, and retrieval
 * of display information.
 */
public class VivDisplay{
    public Display display;
    public static Plugin plugin = null;
    public String displayName;
    public String parentUUID;
    public boolean isChild;
    public boolean isParent;

    public VivDisplay(Plugin thisPlugin, Display thisDisplay) {
        display = thisDisplay;
        init(thisPlugin);
    }

    public VivDisplay(Plugin thisPlugin, World world, Location location, EntityType entityType, Object displayData) {
        display = createDisplay(world, location, entityType, displayData);
        init(thisPlugin);
    }

    public void init(Plugin thisPlugin){
        if (plugin == null) {
            plugin = thisPlugin;
        }
        // todo: try catch these?
        //      Cannot invoke "java.lang.Boolean.booleanValue()" because the return value of "me.vivian.displayerutils.NBTMagic.getNBT(org.bukkit.entity.Entity, String, java.lang.Class)" is null
        //      default values: "", "",
        displayName = NBTMagic.getNBT(display, "VivDisplayName", String.class) == null ? "" : NBTMagic.getNBT(display, "VivDisplayName", String.class);
        parentUUID = NBTMagic.getNBT(display, "VivDisplayParentUUID", String.class) == null ? "" : NBTMagic.getNBT(display, "VivDisplayParentUUID", String.class);
        isChild = NBTMagic.getNBT(display, "VivDisplayIsChild", Boolean.class) != null && NBTMagic.getNBT(display, "VivDisplayIsChild", Boolean.class);
        isParent = NBTMagic.getNBT(display, "VivDisplayIsParent", Boolean.class) != null && NBTMagic.getNBT(display, "VivDisplayIsParent", Boolean.class);
    }

    public String getItemName(){
        //System.out.println("getItemName");
        if (displayName == null) {
            //System.out.println("A");
            Material material = getMaterial();
            String materialName = material == null ? "" : material.name();
            return CommandParsing.toTitleCase(materialName.replace("_", " "));
        }
        //System.out.println("B");
        //System.out.println(displayName);
        return displayName;
    }

    public Boolean isParentDisplay() {
        isParent = NBTMagic.getNBT(display, "VivDisplayIsParent", Boolean.class) != null && NBTMagic.getNBT(display, "VivDisplayIsParent", Boolean.class);
        return isParent;
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
        } else if (display instanceof TextDisplay && displayData instanceof String) {
            String text = (String) displayData;
            text = text.replace("&", "§");
            ((TextDisplay) display).setText(text);
        } else {
            System.out.println("createDisplay: Unhandled display type or display data mismatch.");
        }

        return display;
    }

    /**
     * Destroys this display by dropping its item stack and removing it from the cache.
     *
     * @param player              The player performing the display destruction.
     */
    public void destroy(Player player) { // todo: handle case player is null
        Map<UUID, VivDisplay> selectedVivDisplays = DisplayHandler.selectedVivDisplays;

        if (display!= null) {
            try {
                if (display instanceof BlockDisplay || display instanceof ItemDisplay) {
                    display.getWorld().dropItemNaturally(display.getLocation(), DisplayHandler.getItemStackFromDisplay(display));
                }
            } catch (Exception e) {
                System.out.println("Failed to drop item on display destruction: " + e.getMessage());
            }
            if (player != null) {
                selectedVivDisplays.remove(player);
            }
        } else {
            /* this REALLY should not be an accessible path
             considering it requires this, the VivDisplay calling it, to contain a null Display
             and creating a VivDisplay requires a display entity
             and destroying a display is impossible without a plugin (EG THIS METHOD) or commands*/

            // Print a warning if the display was null
            System.out.println("Tried to destroy a null display");
        }

        display.remove();
    }

    public void destroy(){
        destroy(null);
    }

    public ItemStack getItemStack() {
        return DisplayHandler.getItemStackFromDisplay(display);
    }
    public Material getMaterial() {
        if (getItemStack() == null) return null;
        return getItemStack().getType();
    }

    public void replaceItem(ItemStack newItem){
        if (display instanceof TextDisplay) { // todo: warn
            System.out.println("Tried to replace a TextDisplay's item... glwt?");
            return;
        }

        ItemStack oldItem = getItemStack();
        ItemStack newItemCopy = newItem.clone();
        newItemCopy.setAmount(1);

        if (display instanceof ItemDisplay) {
            ItemDisplay itemDisplay = (ItemDisplay) display;
            itemDisplay.setItemStack(newItemCopy);
        } else if (display instanceof BlockDisplay) {
            BlockDisplay blockDisplay = (BlockDisplay) display;
            BlockData blockData = newItemCopy.getType().createBlockData();
            blockDisplay.setBlock(blockData);
        }

        display.getWorld().dropItem(display.getLocation(), oldItem);
    }

    /**
     * Renames this VivDisplay.
     *
     * @param newName The new name to set for the display.
     * @return True if the rename operation was successful, false otherwise.
     */
    public String rename(String newName) {
        try {
            NBTMagic.setNBT(display, "VivDisplayName", newName);
            String oldName = displayName;
            displayName = newName;

            return Texts.getError("renameSuccess").replace("$newname", newName).replace("$oldname", oldName);
        } catch (Exception e) {
            System.out.println("renameDisplay(): Failed to rename display. Error: " + e.getMessage());
            return Texts.getError("renameFailed");
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

        NBTMagic.setNBT(display, "VivDisplayParentUUID", parentUUID);
        NBTMagic.setNBT(parentDisplay, "VivDisplayIsParent", true);
        NBTMagic.setNBT(display, "VivDisplayIsChild", true);
        return "";
    }

    /**
     * Unsets the parent of this VivDisplay.
     *
     * @return err string, empty if ok
     */
    public String unsetParent() {
        // Get the UUID of the parent display from the display's NBT
        String parentUUIDStr = NBTMagic.getNBT(display, "VivDisplayParentUUID", String.class);

        if (!isChild || parentUUIDStr == null || parentUUIDStr.isEmpty()) {
            return "The display is not a child display and does not have a parent.";
        }

        parentUUID = "";
        isChild = false;

        // Clear the parent-related NBT tags
        NBTMagic.setNBT(display, "VivDisplayParentUUID", "");
        NBTMagic.setNBT(display, "VivDisplayIsChild", false);

        // todo: Update the IsParent property of the parent display and handle other related tasks.

        return "";
    }



    /**
     * Changes the scale of this VivDisplay by the specified offset.
     *
     * @param sizeOffset The offset to add to the current scale.
     * @return True if the scale change was successful, false otherwise.
     */
    public boolean changeSize(Vector3f sizeOffset) {
        Transformation transformation = display.getTransformation();
        Vector3f currentScale = transformation.getScale();
        Vector3f newScale = currentScale.add(sizeOffset);

        if (Math.min(newScale.x, Math.min(newScale.y, newScale.z)) > 0.0) {
            transformation.getScale().set(newScale);
            display.setTransformation(transformation);
            return true;
        } else {
            return false;
        }
    }

    public boolean changeSize(Vector3d sizeOffset) {
        return changeSize(new Vector3f((float) sizeOffset.x, (float) sizeOffset.y, (float) sizeOffset.z));
    }

    /**
     * Sets the scale of this VivDisplay to the specified value.
     *
     * @param newSize The new scale value to set.
     * @param player  The player performing the scale setting.
     * @return True if the scale setting was successful, false otherwise.
     */
    public boolean setScale(double newSize, Player player) { // todo: remove usages
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
     * @return True if the rotation change was successful, false otherwise.
     */
    public void changeRotation(float yawOffset, float pitchOffset, float rollOffset) {
        Transformation transformation = display.getTransformation();

        yawOffset = (float) Math.toRadians(yawOffset);
        pitchOffset = (float) Math.toRadians(pitchOffset);
        rollOffset = (float) Math.toRadians(rollOffset);
        transformation.getLeftRotation().rotateYXZ(rollOffset, pitchOffset, yawOffset);

        display.setTransformation(transformation);
    }

    public void changeRotation(Vector3d rotationOffset) {
        changeRotation((float) rotationOffset.x, (float) rotationOffset.y, (float) rotationOffset.z);
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

    public void changePosition(Vector offset) {
        Location currentLocation = display.getLocation();
        Vector newPos = currentLocation.toVector().add(offset);

        // Teleport to the new position
        display.teleport(TMath.locInWAtPandYP(currentLocation.getWorld(), newPos, currentLocation.getYaw(), currentLocation.getPitch()));
    }

    public void changePosition(double xOffset, double yOffset, double zOffset) {
        changePosition(new Vector(xOffset, yOffset, zOffset));
    }

    public void changePosition(Vector3d offset) {
        changePosition(offset.x, offset.y, offset.z);
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

    public boolean setPosition(Vector pos){
        return setPosition(pos.getX(), pos.getY(), pos.getZ(), null);
    }

    public Vector3d getPosition() {
        Location location = display.getLocation();
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }

    public List<VivDisplay> getDescendants(){
        return DisplayGroupHandler.getAllDescendants(this);
    }
}