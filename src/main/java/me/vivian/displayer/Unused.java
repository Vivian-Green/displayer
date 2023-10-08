package me.vivian.displayer;

import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

/**
 * Holds unused code in case I need to snag it for something else
 */
public class Unused {
    /**
     * constructor... why would you instantiate this?
     */
    public Unused() {}


    /**
     * gets the roll in degrees from a given (transformation)'s right rotation component.
     *
     * @param transformation The transformation containing rotation information.
     * @return The roll angle in degrees.
     */
    public static float getTransRoll(Transformation transformation) {
        // Get the right rotation component
        Quaternionf rollRotation = transformation.getRightRotation();

        // Calculate the roll in degrees from the quaternion
        return (float) Math.toDegrees(2.0 * Math.atan2(rollRotation.x, rollRotation.w));
    }

    /**
     * Determines whether two (locations) are within a given (radius) of each other.
     *
     * @param loc1   The first location.
     * @param loc2   The second location.
     * @param radius The radius within which to check.
     * @return True if the locations are within the specified radius, false otherwise.
     */
    private boolean isWithinRadius(Location loc1, Location loc2, int radius) {
        return loc1.distanceSquared(loc2) <= radius * radius;
    }

    /**
     * Rounds a float (num) to a specified number of decimal (places).
     *
     * @param num    The number to be rounded.
     * @param places The number of decimal places to round to.
     * @return The rounded float value.
     */
    private float roundTo(float num, int places){
        float mult = (float) Math.pow(10, places);
        return Math.round(num * mult)/mult;
    }

    /**
     * rounds a (location)'s position to (places)
     *
     * @param location The original location.
     * @param places   The number of decimal places to round to.
     * @return A new location with rounded coordinates.
     */
    private Location locationRoundedTo(Location location, int places) {
        double x = roundTo((float) location.getX(), places);
        double y = roundTo((float) location.getY(), places);
        double z = roundTo((float) location.getZ(), places);

        return new Location(location.getWorld(), x, y, z, location.getYaw(), location.getPitch());
    }
}
