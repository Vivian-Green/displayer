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
    public static void spawnParticle(Display display){
        spawnParticle(display.getLocation(), null, null, null);
    }
    
    public static void spawnParticle(Location location){
        spawnParticle(location, null, null, null);
    }

    public static void spawnParticle(Display display, Particle particle, Integer count) {
        spawnParticle(display.getLocation(), particle, count, null);
    }

    public static void spawnParticle(Location location, Particle particle, Integer count) {
        spawnParticle(location, particle, count, null);
    }
    
    public static void spawnParticle(Display display, Particle particle, Integer count, Particle.DustOptions dustOptions) {
        spawnParticle(display.getLocation(), particle, count, dustOptions);
    }

    public static void spawnParticle(Location location, Particle particle, Integer count, Particle.DustOptions dustOptions) {
        World world = location.getWorld();
        Vector offset = new Vector();

        if (particle == null) {
            particle = Particle.ENCHANTMENT_TABLE;
            offset = new Vector(0, 1, 0); // only offset these ones- they're weird and go *below* the table
        }
        if (count == null) count = 1;

        Location offsetLocation = new Location(world, location.getX() + offset.getX(), location.getY() + offset.getY(), location.getZ() + offset.getZ(), location.getYaw(), location.getPitch());

        if (dustOptions != null) {
            world.spawnParticle(particle, offsetLocation, count, dustOptions);
        } else {
            world.spawnParticle(particle, offsetLocation, count);
        }
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
    
    public static void particleBetween(Location loc1, Location loc2, Particle particle, Particle.DustOptions dustOptions){
        Vector pos1 = loc1.toVector();
        Vector pos2 = loc2.toVector();
        double scale = Math.random();
        Vector posBetween = new Vector(pos1.getX()+scale*(pos2.getX() - pos1.getX()), pos1.getY()+scale*(pos2.getY() - pos1.getY()), pos1.getZ()+scale*(pos2.getZ() - pos1.getZ()));
        Location locationBetween = new Location(loc1.getWorld(), posBetween.getX(), posBetween.getY(), posBetween.getZ(), loc1.getYaw(), loc1.getPitch());

        spawnParticle(locationBetween, particle, 5, null);
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


        for (int i = 0; i < count/2; i++) {
            double t = (double) i / (count/2 - 1);

            Vector3d pointOnLine = vector1.lerp(vector2, t);
            Location pointLocation = new Location(world, pointOnLine.x, pointOnLine.y, pointOnLine.z);

            if (dustOptions != null) {
                world.spawnParticle(particle, pointLocation, 2, dustOptions);
            } else {
                world.spawnParticle(particle, pointLocation, 2);
            }
        }
    }
}
