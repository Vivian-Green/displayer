package me.vivian.displayer;

import me.vivian.displayer.display.DisplayGroupHandler;
import me.vivian.displayer.display.VivDisplay;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.joml.Vector3d;

import java.util.List;
import java.util.Random;

public class ParticleHandler {
    /**
     * spawns particles at this display
     */
    public static void spawnParticle(Display display, Particle particle, Integer count) {
        Location displayLocation = display.getLocation();

        Vector3d offset = new Vector3d(0, 1, 0);

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

    public static void spawnParticlesAtHierarchy(VivDisplay vivDisplay, Particle particle, int particleCount) {
        // Get all displays in the hierarchy
        List<VivDisplay> hierarchy = DisplayGroupHandler.getAllDisplaysInHierarchy(vivDisplay);

        // Spawn particles at each display
        for (VivDisplay display: hierarchy) {
            // Location displayLocation = display.display.getLocation();
            drawParticleLine(display.display.getLocation(), vivDisplay.display.getLocation(), particle, particleCount);
            //display.spawnParticle(particle, particleCount);
        }
    }

    public static void drawParticleLine(Location loc1, Location loc2, Particle particle, Integer count) {
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

        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            double t = (double) i / (count - 1);

            Vector3d pointOnLine = vector1.lerp(vector2, t);
            Vector3d randomOffset = new Vector3d(-0.1 + (0.1 - (-0.1)) * rand.nextDouble(), -0.1 + (0.1 - (-0.1)) * rand.nextDouble(), -0.1 + (0.1 - (-0.1)) * rand.nextDouble());
            Location pointLocation = new Location(world, pointOnLine.x + randomOffset.x, pointOnLine.y + randomOffset.y, pointOnLine.z + randomOffset.z);


            world.spawnParticle(particle, pointLocation, 1);
        }
    }
}
