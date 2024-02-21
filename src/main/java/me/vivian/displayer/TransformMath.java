package me.vivian.displayer;

import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

public class TransformMath {
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

    // rounds a double (num)'s position to (places)
    public static double roundTo(double num, int places) {
        double mult = Math.pow(10, places);
        return Math.round(num * mult) / mult;
    }

    public static float roundTo(float num, int places) {
        float mult = (float) Math.pow(10, places);
        return Math.round(num * mult) / mult;
    }

    // rounds a (location)'s position to (places)
    public static Location locationRoundedTo(Location location, int places) {
        double x = roundTo((float) location.getX(), places);
        double y = roundTo((float) location.getY(), places);
        double z = roundTo((float) location.getZ(), places);

        return new Location(location.getWorld(), x, y, z, location.getYaw(), location.getPitch());
    }
}
