package me.vivian.displayer.display;

import me.vivian.displayer.config.Config;
import me.vivian.displayerutils.TransformMath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.*;

import static me.vivian.displayer.display.VivDisplay.plugin;

public class DisplayGroupHandler {
    static FileConfiguration config = Config.config;

    // Function to translate all displays in a hierarchy
    public static void translateHierarchy(VivDisplay vivDisplay, Vector3d translation) {
        // Get all displays in the hierarchy
        if (vivDisplay == null) {
            System.out.println("translateHierarchy: vivDisplay is null");
            return;
        }
        System.out.println(vivDisplay.displayName);

        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);
        if (hierarchy == null) {
            System.out.println("translateHierarchy: hierarchy is null"); // todo: warn
            return;
        }

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
            return;
        }
        System.out.println(vivDisplay.displayName);

        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);
        if (hierarchy == null) {
            System.out.println("resizeHierarchy: hierarchy is null"); // todo: warn
            return;
        }

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
    public static void copyAndPasteHierarchy(VivDisplay vivDisplay, Player player, Location newLocation) { // todo: parenting just doesn't apply right here
        // Record the player's selected display before copying
        VivDisplay originalSelectedDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());

        // Get all displays in the hierarchy
        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);
        if (hierarchy == null) return; /* either this vivDisplay is null, or its topmost parent is, which is a weird case
            todo: handle by getting topmost not-null parent?
        */

        // Create a map to store the copies of each VivDisplay
        Map<String, VivDisplay> copies = new HashMap<>();

        // Copy all VivDisplays in the hierarchy
        for (VivDisplay original: hierarchy) {
            VivDisplay copy = copyVivDisplay(original);
            copies.put(original.display.getUniqueId().toString(), copy);

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
        }

        // Set the player's selected display back to what it was before copying
        DisplayHandler.selectedVivDisplays.put(player.getUniqueId(), originalSelectedDisplay);
    }


    public static List<VivDisplay> getAllDescendants(VivDisplay parentVivDisplay){
        return getAllDescendants(parentVivDisplay, 0);
    }

    // Function to get all descendants of a VivDisplay
    private static List<VivDisplay> getAllDescendants(VivDisplay parentVivDisplay, int depth) {
        if (depth >= config.getInt("maxSearchDepth")) return null; // too far along in tree
        if (!parentVivDisplay.isParent) return null; // no use getting nearby displays when this isn't a parent

        String parentUUID = parentVivDisplay.display.getUniqueId().toString();
        List<VivDisplay> descendants = new ArrayList<>();
        List<VivDisplay> nearbyVivDisplays = DisplayHandler.getNearbyVivDisplays(parentVivDisplay.display.getLocation(), config.getInt("maxSearchRadius"), null);

        for (VivDisplay vivDisplay: nearbyVivDisplays) {
            if (vivDisplay.parentUUID != null && vivDisplay.parentUUID.equals(parentUUID)) {
                // descendants.add(vivDisplay); // unnecessary yea?

                List<VivDisplay> thisDescendants = getAllDescendants(vivDisplay, depth+1);
                if (thisDescendants == null) continue;

                descendants.addAll(thisDescendants); // Recursive call
                //System.out.println(vivDisplay.displayName);
            }
        }
        return descendants;
    }

    // Function to get all displays in a hierarchy
    public static List<VivDisplay> getAllDisplaysInHierarchy(VivDisplay vivDisplay) {
        if (vivDisplay == null) {
            System.out.println("vivDisplay is null"); // todo: warn
            return null;
        } else {
            System.out.println(vivDisplay.displayName);
        }

        VivDisplay topmostParent = getHighestVivDisplay(vivDisplay);
        if (topmostParent == null) {
            System.out.println("topmostParent is null"); // todo: warn
            return null;
        } else {
            System.out.println(topmostParent.displayName);
        }

        List<VivDisplay> hierarchy = getAllDescendants(topmostParent);
        if (hierarchy == null) {
            System.out.println("hierarchy is null"); // todo: warn
            return null;
        }

        hierarchy.add(topmostParent); // re-add topmost parent

        System.out.println(vivDisplay.displayName);
        return hierarchy;
    }

    public static VivDisplay getHighestVivDisplay(VivDisplay vivDisplay) {
        if (vivDisplay == null) {
            System.out.println("getHighestVivDisplay: vivDisplay is null");
            return null;
        }

        ArrayList<String> parentChainUUIDs = new ArrayList<>();

        int depthLeft = config.getInt("maxSearchDepth");
        List<Display> nearbyDisplays = DisplayHandler.getNearbyDisplays(vivDisplay.display.getLocation(), config.getInt("maxSearchRadius"));

        while (depthLeft > 0 && vivDisplay.parentUUID != null && !vivDisplay.parentUUID.isEmpty()) {
            // you have a dad somewhere
            if (parentChainUUIDs.contains(vivDisplay.parentUUID)) return vivDisplay; // you're your dad's dad, so you have to come back with the milk

            Entity entity = Bukkit.getServer().getEntity(UUID.fromString(vivDisplay.parentUUID));
            if (!(entity instanceof Display)) return vivDisplay; // your dad isn't real

            // you didn't make your dad, and he is real!
            parentChainUUIDs.add(vivDisplay.parentUUID);
            vivDisplay = new VivDisplay(plugin, (Display) entity); // but now you are your dad... maybe his dad has the milk?

            depthLeft--;
        }

        // fatherless
        return vivDisplay;
    }


    public static VivDisplay copyVivDisplay(VivDisplay original) {
        System.out.println("this shouldn't be running yet! copyVivDisplay untested!");
        if (true) return null;

        // Create a new VivDisplay with the same properties as the original
        VivDisplay copy = new VivDisplay(plugin, (Display) original.display.copy()); // todo: .copy() probably doesn't work on its own-
        copy.displayName = original.displayName;
        copy.isChild = original.isChild;
        copy.isParent = original.isParentDisplay();
        // Don't copy the parentUUID, because we'll set it when we paste the copy
        return copy;
    }


    public static void rotateVivDisplayAroundPoint(VivDisplay vivDisplay, Vector3d point, Vector3d rotationDegrees) {
        if (!Config.config.getBoolean("doDisplayGroupRotation")) return;

        // todo: hella borked, needs debugging- changeRotation should NOT be the call here.
        System.out.println("rotationDegrees (euler): " + rotationDegrees.x + ", " + rotationDegrees.y + ", " + rotationDegrees.z);

        // Get the Transformation of the VivDisplay
        Transformation oldTransform = vivDisplay.display.getTransformation();
        Location oldLocation = vivDisplay.display.getLocation();

        Quaternionf quaternionRight = oldTransform.getRightRotation();

        // Convert quaternion to Euler angles
        double sinr_cosp = 2 * (quaternionRight.w * quaternionRight.x + quaternionRight.y * quaternionRight.z);
        double cosr_cosp = 1 - 2 * (quaternionRight.x * quaternionRight.x + quaternionRight.y * quaternionRight.y);
        double roll = Math.atan2(sinr_cosp, cosr_cosp);

        double sinp = 2 * (quaternionRight.w * quaternionRight.y - quaternionRight.z * quaternionRight.x);
        double pitch;
        if (Math.abs(sinp) >= 1)
            pitch = Math.copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
        else
            pitch = Math.asin(sinp);

        double siny_cosp = 2 * (quaternionRight.w * quaternionRight.z + quaternionRight.x * quaternionRight.y);
        double cosy_cosp = 1 - 2 * (quaternionRight.y * quaternionRight.y + quaternionRight.z * quaternionRight.z);
        double yaw = Math.atan2(siny_cosp, cosy_cosp);

        yaw = Math.toDegrees(yaw);
        pitch = Math.toDegrees(pitch);
        roll = Math.toDegrees(roll);

        System.out.println("old (euler) rotation: " + yaw + ", " + pitch + ", " + roll);

        // display quaternion
        Quaternionf currentRotation = TransformMath.eulerToQuaternion((float) yaw, (float) pitch, (float) roll);
        System.out.println("old (quaternion) rotation: " + currentRotation.x + ", " + currentRotation.y + ", " + currentRotation.z + ", " + currentRotation.w);

        // rotation offset quaternion
        Quaternionf rotationQuaternion = TransformMath.eulerToQuaternion((float) rotationDegrees.x, (float) rotationDegrees.y, (float) rotationDegrees.z);

        System.out.println("rotationQuaternion: " + rotationQuaternion.x + ", " + rotationQuaternion.y + ", " + rotationQuaternion.z + ", " + rotationQuaternion.w);

        // Multiply the quaternions (this is probably the part where I am bad - tf is local space)
        Quaternionf newRotation = rotationQuaternion.mul(currentRotation);
        System.out.println("new (quaternion) rotation: " + newRotation.x + ", " + newRotation.y + ", " + newRotation.z + ", " + newRotation.w);

        // Convert the quaternion back to Euler angles
        float[] euler = TransformMath.quaternionToEuler(newRotation);

        // Set the new rotation of the VivDisplay
        System.out.println("out (euler) rotation: " + euler[0] + ", " + euler[1] + ", " + euler[2]);
        vivDisplay.changeRotation(/*euler[1] ??*/0 , euler[2], 0/*euler[0] ??*/);

        // Translate the VivDisplay's position so that the rotation point is at the origin
        Vector3d relativePosition = new Vector3d(oldLocation.getX() - point.x, oldLocation.getY() - point.y, oldLocation.getZ() - point.z);

        // rotate relative position by rotationMatrix
        Matrix3d rotationMatrix = new Matrix3d().rotateXYZ(Math.toRadians(rotationDegrees.x), Math.toRadians(rotationDegrees.z), -Math.toRadians(rotationDegrees.x)); // todo: does yRot need to be negative? ignore for now
        Vector3d rotatedPosition = rotationMatrix.transform(relativePosition);

        // absolute position from rotated relative position
        Vector3d newPosition = new Vector3d(rotatedPosition.x + point.x, rotatedPosition.y + point.y, rotatedPosition.z + point.z);

        // Set the VivDisplay's position and rotation
        vivDisplay.setPosition(newPosition.x, newPosition.y, newPosition.z, null);
    }


    // Function to rotate all displays in a hierarchy
    public static void rotateHierarchy(VivDisplay vivDisplay, Vector3d rotation) {
        if (!Config.config.getBoolean("doDisplayGroupRotation")) return;

        // mise en place
        if (vivDisplay == null) {
            System.out.println("rotateHierarchy: vivDisplay is null");
        } else {
            System.out.println(vivDisplay.displayName);
            System.out.println("rotateHierarchy rot (euler): " + rotation.x + ", " + rotation.y + ", " + rotation.z);
        }

        List<VivDisplay> hierarchy = getAllDisplaysInHierarchy(vivDisplay);
        if (hierarchy == null) {
            System.out.println("rotateHierarchy: hierarchy is null"); // todo: warn
            return;
        }

        VivDisplay highestVivDisplay = getHighestVivDisplay(vivDisplay);
        if (highestVivDisplay == null) {
            System.out.println("rotateHierarchy: highestVivDisplay is null"); // todo: warn
            return;
        }

        Location rotationCenter = highestVivDisplay.display.getLocation();
        Vector3d rotationCenterPos = new Vector3d(rotationCenter.getX(), rotationCenter.getY(), rotationCenter.getZ());

        for (VivDisplay vivDisplayToRotate: hierarchy) {
            rotateVivDisplayAroundPoint(vivDisplayToRotate, rotationCenterPos, rotation);
        }
    }
}
