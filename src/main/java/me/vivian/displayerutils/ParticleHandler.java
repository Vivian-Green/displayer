package me.vivian.displayerutils;

import me.vivian.displayer.display.DisplayGroupHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.List;

public class ParticleHandler {
    /**
     * spawns particles at this display
     */
    public static void spawnParticle(Display display, Particle particle, Integer count) {
        Location displayLocation = display.getLocation();

        Vector offset = new Vector(0, 1, 0);

        if (particle == null) {
            particle = Particle.ENCHANTMENT_TABLE;
        }
        if (count == null) {
            count = 100;
        }

        Location offsetLocation = new Location(displayLocation.getWorld(), displayLocation.getX() + offset.getX(), displayLocation.getY() + offset.getY(), displayLocation.getZ() + offset.getZ(), displayLocation.getYaw(), displayLocation.getPitch());

        displayLocation.getWorld().spawnParticle(
                particle,
                offsetLocation,
                count
        );
    }

    public static void spawnParticlesAtHierarchy(VivDisplay vivDisplay, Particle particle, int particleCount) {
        // Get all displays in the hierarchy
        List<VivDisplay> hierarchy = DisplayGroupHandler.getAllDisplaysInHierarchy(vivDisplay);

        // Spawn particles at each display
        for (VivDisplay display: hierarchy) {
            // Location displayLocation = display.display.getLocation();
            drawParticleLine(display.display.getLocation(), vivDisplay.display.getLocation(), particle, particleCount, null);
            //display.spawnParticle(particle, particleCount);
        }
    }

    public static void drawParticleLine(Location loc1, Location loc2, Particle particle, Integer count, Particle.DustOptions dustOptions) {
        if (particle == null) {
            particle = Particle.DOLPHIN;
        }
        if (count == null) {
            count = 100;
        }

        World world = loc1.getWorld();
        Vector3d vector1 = new Vector3d(loc1.getX(), loc1.getY(), loc1.getZ());
        Vector3d vector2 = new Vector3d(loc2.getX(), loc2.getY(), loc2.getZ());
        // Vector3d vectorBetween = new Vector3d(vector2.x-vector1.x, vector2.y-vector1.y, vector2.z-vector1.z);


        for (int i = 0; i < count/5; i++) {
            double t = (double) i / (count/5 - 1);

            Vector3d pointOnLine = vector1.lerp(vector2, t);
            Location pointLocation = new Location(world, pointOnLine.x, pointOnLine.y, pointOnLine.z);

            if (dustOptions != null) {
                world.spawnParticle(particle, pointLocation, 5, dustOptions);
            } else {
                world.spawnParticle(particle, pointLocation, 5);
            }
        }
    }
}
