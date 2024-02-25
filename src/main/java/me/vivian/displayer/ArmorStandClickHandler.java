package me.vivian.displayer;

import me.vivian.displayer.commands.AutoFill;
import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Texts;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

public class ArmorStandClickHandler extends JavaPlugin implements Listener {

    public static final float SHOULDER_X_OFFSET = 5f / 16f;
    public static final float SHOULDER_Y_OFFSET = 22f / 16f;

    @Override
    public void onEnable() {
        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);
    }

    public static void onInteractWithArmorStand(PlayerInteractAtEntityEvent event) {
        System.out.println("AAAAAAAAAAAA");

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();

        // Get the ItemStack in each hand
        ItemStack rightHandItemStack = armorStand.getEquipment().getItemInMainHand();
        ItemStack leftHandItemStack = armorStand.getEquipment().getItemInOffHand();

        // Check if the ArmorStand is holding an item in each hand
        if (!rightHandItemStack.getType().equals(Material.AIR)) {
            // Get the item's location for the right hand
            Location rightHandItemLocation = getItemLocation(armorStand, false);

            // Spawn the Display entity at the item's location for the right hand
            spawnDisplayEntity(rightHandItemLocation, rightHandItemStack, 0.625);
        }

        if (!leftHandItemStack.getType().equals(Material.AIR)) {
            // Get the item's location for the left hand
            Location leftHandItemLocation = getItemLocation(armorStand, true);

            // Spawn the Display entity at the item's location for the left hand
            spawnDisplayEntity(leftHandItemLocation, leftHandItemStack, 0.625);
        }
    }

    public static Location getItemLocation(ArmorStand armorStand, boolean isLeftArm) {
        // Get the ArmorStand's location
        Location armorStandLocation = armorStand.getLocation();

        System.out.println("armorstand rotation:" + armorStandLocation.getDirection());

        // Estimate the shoulder position
        //Vector shoulderOffset = new Vector(isLeftArm ? -SHOULDER_X_OFFSET : SHOULDER_X_OFFSET, SHOULDER_Y_OFFSET, 0);
        //shoulderOffset = rotateVectorAroundY(shoulderOffset, armorStandLocation.getYaw());

        //Location shoulderLocation = armorStandLocation.add(shoulderOffset);
        Location shoulderLocation = armorStandLocation.add(0, SHOULDER_Y_OFFSET, 0); // Add 22 pixels to the y-coordinate
        shoulderLocation.setYaw(shoulderLocation.getYaw()+90);
        Vector direction = shoulderLocation.getDirection();
        shoulderLocation.setYaw(shoulderLocation.getYaw()-90);

        shoulderLocation.add(direction.multiply(isLeftArm? -SHOULDER_X_OFFSET : SHOULDER_X_OFFSET)); // Offset 5 pixels in the direction

        // Get the arm direction vector
        EulerAngle armPose = getArmRotation(armorStand, isLeftArm);
        shoulderLocation.setPitch((float) armPose.getX() + (float) shoulderLocation.getPitch()); // todo: needs to be in armorstand's base rotation space
        shoulderLocation.setYaw((float) armPose.getY() + (float) shoulderLocation.getYaw());

        spawnDisplayEntity(shoulderLocation, new ItemStack(Material.ENDER_CHEST), 0.15);

        Vector armDirection = getArmDirection(armorStand, isLeftArm);





        // Get the offset for the item
        Vector itemOffset;
        if (isLeftArm) {
            itemOffset = new Vector(-0.0625F, 0.125F, -0.625F);
        } else {
            itemOffset = new Vector(0.0625F, 0.125F, -0.625F);
        }

        // Rotate the item offset to match the arm's rotation
        // todo: use armPose
        itemOffset = rotateVector(itemOffset, armDirection);

        // Add the item offset to the shoulder location to get the item's location
        Vector itemLocationVector = shoulderLocation.toVector().add(itemOffset);

        // Convert the item location Vector back to a Location
        Location itemLocation = itemLocationVector.toLocation(armorStand.getWorld(), armorStandLocation.getYaw(), armorStandLocation.getPitch());


        return itemLocation;
    }



    public static Location getArmTip(ArmorStand as, boolean isLeftArm) {
        // Gets shoulder location
        Location asl = as.getLocation().clone();
        asl.setYaw(asl.getYaw() + 90f);
        Vector dir = asl.getDirection();
        asl.setX(asl.getX() + 5f / 16f * dir.getX());
        asl.setY(asl.getY() + 22f / 16f);
        asl.setZ(asl.getZ() + 5f / 16f * dir.getZ());

        // Get Hand Location
        EulerAngle ea = isLeftArm ? as.getLeftArmPose() : as.getRightArmPose();
        Vector armDir = getDirection(ea.getY(), ea.getX(), -ea.getZ());
        armDir = rotateVectorAroundY(armDir, Math.toRadians(asl.getYaw() - 90f));
        asl.setX(asl.getX() + 10f / 16f * armDir.getX());
        asl.setY(asl.getY() + 10f / 16f * armDir.getY());
        asl.setZ(asl.getZ() + 10f / 16f * armDir.getZ());

        return asl;
    }


    public static Vector rotateVectorAroundY(Vector v, double yaw) {
        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);

        double x = v.getX() * cosYaw - v.getZ() * sinYaw;
        double z = v.getX() * sinYaw + v.getZ() * cosYaw;

        return new Vector(x, v.getY(), z);
    }


    public static EulerAngle getArmRotation(ArmorStand armorStand, boolean isLeftArm) {
        // Get the pose of the right arm
        EulerAngle armPose = isLeftArm ? armorStand.getLeftArmPose() : armorStand.getRightArmPose();

        return armPose;
    }

    public static Vector getArmDirection(ArmorStand armorStand, boolean isLeftArm) {
        // Get the pose of the right arm
        EulerAngle armPose = getArmRotation(armorStand, isLeftArm);

        // Convert the EulerAngle to a direction vector
        Vector armDirection = eulerAngleToDirectionVector(armPose);

        return armDirection;
    }

    public static Vector getDirection(double yaw, double pitch, double roll) {
        double pitchRadians = Math.toRadians(pitch);
        double yawRadians = Math.toRadians(yaw);

        double x = Math.cos(pitchRadians) * Math.cos(yawRadians);
        double y = Math.sin(pitchRadians);
        double z = Math.cos(pitchRadians) * Math.sin(yawRadians);

        return new Vector(x, y, z);
    }


    public static Vector eulerAngleToDirectionVector(EulerAngle eulerAngle) {
        double yaw = Math.toRadians(Math.toDegrees(eulerAngle.getY()) + 90);
        double pitch = Math.toRadians(Math.toDegrees(eulerAngle.getX()) + 180);

        double x = Math.sin(pitch) * Math.cos(yaw);
        double y = Math.sin(pitch) * Math.sin(yaw);
        double z = Math.cos(pitch);

        return new Vector(x, z, y);
    }

    public static Vector rotateVector(Vector v, Vector rotation) {
        double sinPitch = Math.sin(rotation.getX());
        double cosPitch = Math.cos(rotation.getX());
        double sinYaw = Math.sin(rotation.getY());
        double cosYaw = Math.cos(rotation.getY());

        Vector rotated = new Vector();

        // Rotate around X-axis
        rotated.setY(v.getY() * cosPitch - v.getZ() * sinPitch);
        rotated.setZ(v.getY() * sinPitch + v.getZ() * cosPitch);

        // Rotate around Y-axis
        double x = v.getX() * cosYaw - rotated.getZ() * sinYaw;
        rotated.setZ(v.getX() * sinYaw + rotated.getZ() * cosYaw);
        rotated.setX(x);

        return rotated;
    }


    public static void spawnDisplayEntity(Location itemLocation, ItemStack itemStack, double scale) {
        ItemDisplay display = (ItemDisplay) itemLocation.getWorld().spawnEntity(itemLocation, EntityType.ITEM_DISPLAY);

        display.setRotation(itemLocation.getYaw(), itemLocation.getPitch());

        Transformation transformation = display.getTransformation();
        // double currentSize = transformation.getScale().x;
        transformation.getScale().set(scale);
        display.setTransformation(transformation);

        display.setItemStack(itemStack);
    }
}