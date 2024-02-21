package me.vivian.displayer.display;

import me.vivian.displayer.ParticleHandler;
import me.vivian.displayer.commands.CommandHandler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.*;

public class DisplayGroupHandler {
    // Function to translate all displays in a hierarchy
    public static void translateHierarchy(VivDisplay vivDisplay, Vector3d translation) {
        // Get all displays in the hierarchy
        if (vivDisplay == null) {
            System.out.println("translateHierarchy: vivDisplay is null");
        } else {
            System.out.println(vivDisplay.displayName);
        }
        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);

        // Translate all VivDisplays in the hierarchy
        for (VivDisplay vivDisplayToTranslate : hierarchy) {
            vivDisplayToTranslate.changePosition(translation.x, translation.y, translation.z);
        }
    }

    // Function to resize all displays in a hierarchy
    public static void resizeHierarchy(VivDisplay vivDisplay, float size) {
        // Get all displays in the hierarchy
        if (vivDisplay == null) {
            System.out.println("resizeHierarchy: vivDisplay is null");
        } else {
            System.out.println(vivDisplay.displayName);
        }
        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);

        // Get the position of the highest parent
        Vector3d parentPosition = hierarchy.get(0).getPosition();

        // Resize all VivDisplays in the hierarchy
        for (VivDisplay vivDisplayToResize : hierarchy) {
            // Scale the position of the VivDisplay relative to the highest parent
            Vector3d position = vivDisplayToResize.getPosition();
            Vector3d scaledPosition = new Vector3d(
                    parentPosition.x + (position.x - parentPosition.x) * size,
                    parentPosition.y + (position.y - parentPosition.y) * size,
                    parentPosition.z + (position.z - parentPosition.z) * size
            );
            vivDisplayToResize.setPosition(scaledPosition.x, scaledPosition.y, scaledPosition.z, null);

            // Scale the size of the VivDisplay
            vivDisplayToResize.setScale(vivDisplayToResize.getScale() * size, null);
        }
    }

    // Function to copy and paste all displays in a hierarchy
    public static void copyAndPasteHierarchy(VivDisplay vivDisplay, Player player, Location newLocation) {
        // Record the player's selected display before copying
        VivDisplay originalSelectedDisplay = CommandHandler.selectedVivDisplays.get(player);

        // Get all displays in the hierarchy
        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);

        // Create a map to store the copies of each VivDisplay
        Map<String, VivDisplay> copies = new HashMap<>();

        // Copy all VivDisplays in the hierarchy
        for (VivDisplay original: hierarchy) {
            VivDisplay copy = copyVivDisplay(original);
            copies.put(original.display.getUniqueId().toString(), copy);
        }

        // Update the parentUUIDs of the copies
        for (VivDisplay original: hierarchy) {
            VivDisplay copy = copies.get(original.display.getUniqueId().toString());
            if (original.parentUUID != null) {
                VivDisplay parentCopy = copies.get(original.parentUUID);
                copy.parentUUID = parentCopy.display.getUniqueId().toString();
            }
        }

        // Calculate the translation vector
        VivDisplay topmostParent = getHighestVivDisplay(vivDisplay);
        Vector3d translation = new Vector3d(newLocation.getX() - topmostParent.display.getLocation().getX(),
                newLocation.getY() - topmostParent.display.getLocation().getY(),
                newLocation.getZ() - topmostParent.display.getLocation().getZ());

        // Paste all copies at the new location
        for (VivDisplay copy: copies.values()) {
            Vector3d newPosition = new Vector3d(
                copy.display.getLocation().getX() + translation.x,
                copy.display.getLocation().getY() + translation.y,
                copy.display.getLocation().getZ() + translation.z
            );

            copy.setPosition(newPosition.x, newPosition.y, newPosition.z, null);
            // copy.setRotation(newLocation.getYaw(), newLocation.getPitch());
            // Add the copy to the vivDisplays map
            CommandHandler.vivDisplays.put(copy.display.getUniqueId().toString(), copy);
        }

        // Set the player's selected display back to what it was before copying
        CommandHandler.selectedVivDisplays.put(player, originalSelectedDisplay);
    }

    // Function to get all descendants of a VivDisplay
    private static List<VivDisplay> getAllDescendants(VivDisplay parentVivDisplay) {
        List<VivDisplay> descendants = new ArrayList<>();
        for (VivDisplay vivDisplay: CommandHandler.vivDisplays.values()) {
            if (vivDisplay.parentUUID != null && vivDisplay.parentUUID.equals(parentVivDisplay.display.getUniqueId().toString())) {
                descendants.add(vivDisplay);
                descendants.addAll(getAllDescendants(vivDisplay)); // Recursive call
                System.out.println(vivDisplay.displayName);
            }
        }
        return descendants;
    }

    // Function to get all displays in a hierarchy
    public static List<VivDisplay> getAllDisplaysInHierarchy(VivDisplay vivDisplay) {
        if (vivDisplay == null) {
            System.out.println("translateHierarchy: vivDisplay is null");
        } else {
            System.out.println(vivDisplay.displayName);
        }
        VivDisplay topmostParent = getHighestVivDisplay(vivDisplay);
        if (topmostParent == null) {
            System.out.println("translateHierarchy: topmostParent is null");
        } else {
            System.out.println(topmostParent.displayName);
        }
        List<VivDisplay> hierarchy = getAllDescendants(topmostParent);
        hierarchy.add(vivDisplay);
        System.out.println(vivDisplay.displayName);
        return hierarchy;
    }

    public static VivDisplay getHighestVivDisplay(VivDisplay vivDisplay) {
        if (vivDisplay == null) {
            System.out.println("getHighestVivDisplay: vivDisplay is null");
        } else {
            System.out.println(vivDisplay.displayName);
        }

        int maxDepth = 25;
        while (maxDepth > 0 && vivDisplay != null && vivDisplay.parentUUID != null && CommandHandler.vivDisplays.get(vivDisplay.parentUUID) != null) {
            // todo: check for recursive trees
            vivDisplay = CommandHandler.vivDisplays.get(vivDisplay.parentUUID);
            maxDepth--;
        }
        if (maxDepth < 1) {
            // handle recursive group
            return vivDisplay;
        }

        return vivDisplay;
    }


    // Function to copy a VivDisplay
    public static VivDisplay copyVivDisplay(VivDisplay original) {
        // Create a new VivDisplay with the same properties as the original
        VivDisplay copy = new VivDisplay(original.plugin, original.display);
        copy.displayName = original.displayName;
        copy.isChild = original.isChild;
        copy.isParent = original.isThisParent();
        // Don't copy the parentUUID, because we'll set it when we paste the copy
        return copy;
    }

    // Function to rotate a VivDisplay around a point using degrees
    public static void rotateVivDisplayAroundPoint(VivDisplay vivDisplay, Vector3d point, Vector3d rotationDegrees) {
        // Convert degrees to radians
        //roll, yaw, pitch
        double xRotation = Math.toRadians(rotationDegrees.x);
        double yRotation = Math.toRadians(rotationDegrees.y);
        double zRotation = Math.toRadians(rotationDegrees.z);

        // Translate the VivDisplay's position so that the rotation point is at the origin
        Vector3d translatedPosition = new Vector3d(vivDisplay.display.getLocation().getX() - point.x,
                vivDisplay.display.getLocation().getY() - point.y,
                vivDisplay.display.getLocation().getZ() - point.z);

        // Calculate the rotation matrix
        // Matrix3d rotationMatrix = new Matrix3d().rotateXYZ(xRotation, yRotation, zRotation);
        Matrix3d rotationMatrix = new Matrix3d().rotateXYZ(xRotation, zRotation, -yRotation);

        // Apply the rotation matrix to the translated position
        Vector3d rotatedPosition = rotationMatrix.transform(translatedPosition);

        // Translate the rotated position back
        Vector3d newPosition = new Vector3d(rotatedPosition.x + point.x,
                rotatedPosition.y + point.y,
                rotatedPosition.z + point.z);

        // Set the VivDisplay's position and rotation
        vivDisplay.setPosition(newPosition.x, newPosition.y, newPosition.z, null);
        vivDisplay.changeRotation((float) rotationDegrees.x, (float) rotationDegrees.y, (float) rotationDegrees.z, null);
    }

    // Function to rotate all displays in a hierarchy
    public static void rotateHierarchy(VivDisplay vivDisplay, Vector3d rotation) {
        if (vivDisplay == null) {
            System.out.println("translateHierarchy: vivDisplay is null");
        } else {
            System.out.println(vivDisplay.displayName);
        }
        // Get all displays in the hierarchy
        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);

        // Get the highest VivDisplay in the hierarchy
        VivDisplay highestVivDisplay = getHighestVivDisplay(vivDisplay);

        Location rotationCenter = highestVivDisplay.display.getLocation();
        Vector3d rotationCenterPos = new Vector3d(rotationCenter.getX(), rotationCenter.getY(), rotationCenter.getZ());
        // Rotate all VivDisplays in the hierarchy
        for (VivDisplay vivDisplayToRotate: hierarchy) {
            rotateVivDisplayAroundPoint(vivDisplayToRotate, rotationCenterPos, rotation);
        }
    }
}
