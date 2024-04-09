package me.vivian.displayerutils;

import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class TMath {
    public static Location locInWAtP(World world, Vector3d pointOnLine) {
        return locInWAtP(world, new Vector(pointOnLine.x, pointOnLine.y, pointOnLine.z));
    }

    public static Location locInWAtP(World world, Vector pos) {
        return new Location(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static Location locInWAtPandYP(World world, Vector pos, float yaw, float pitch) {
        return new Location(world, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
    }

    public static Location locInWAtPandYP(World world, Vector pos, double yaw, double pitch) {
        return locInWAtPandYP(world, pos, (float) yaw, (float) pitch);
    }


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

    public static Quaternionf eulerToQuaternion(float yaw, float pitch, float roll) {
        // Convert the Euler angles to radians
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double rollRad = Math.toRadians(roll);

        // quaternion components
        double cy = Math.cos(yawRad * 0.5);
        double sy = Math.sin(yawRad * 0.5);
        double cp = Math.cos(pitchRad * 0.5);
        double sp = Math.sin(pitchRad * 0.5);
        double cr = Math.cos(rollRad * 0.5);
        double sr = Math.sin(rollRad * 0.5);

        double w = cr * cp * cy + sr * sp * sy;
        double x = sr * cp * cy - cr * sp * sy;
        double y = cr * sp * cy + sr * cp * sy;
        double z = cr * cp * sy - sr * sp * cy;

        // Create the quaternion
        return new Quaternionf((float)x, (float)y, (float)z, (float)w);
    }


    public static float[] quaternionToEuler(Quaternionf quaternion) {
        float[] euler = new float[3];

        // big math go brrrrrrrrrr
        euler[0] = (float) Math.atan2(2.0*(quaternion.w*quaternion.x + quaternion.y*quaternion.z), 1.0 - 2.0*(quaternion.x*quaternion.x + quaternion.y*quaternion.y));
        euler[1] = (float) Math.asin(2.0*(quaternion.w*quaternion.y - quaternion.z*quaternion.x));
        euler[2] = (float) Math.atan2(2.0*(quaternion.w*quaternion.z + quaternion.x*quaternion.y), 1.0 - 2.0*(quaternion.y*quaternion.y + quaternion.z*quaternion.z));

        euler[0] = (float) Math.toDegrees(euler[0]);
        euler[1] = (float) Math.toDegrees(euler[1]);
        euler[2] = (float) Math.toDegrees(euler[2]);

        return euler;
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
