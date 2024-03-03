package me.vivian.displayer.commands;

import java.util.*;

import me.vivian.displayer.config.Config;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.NBTMagic;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;


// todo: aliases in config

// handles commands for creating, manipulating, and interacting with displays
public class CommandHandler implements CommandExecutor {
    public static PluginDescriptionFile pluginDesc;
    private static Plugin plugin;
    public static NBTMagic nbtm;

    public static Map<String, String> errMap;

    public CommandHandler(Plugin thisPlugin) {
        plugin = thisPlugin;
        pluginDesc = plugin.getDescription();
        nbtm = new NBTMagic(plugin);

        Config.loadConfig();
        Texts.loadTexts();

        errMap = Texts.getErrors();
    }

    public static Plugin getPlugin() {
        return plugin;
    }
    public static final Map<String, VivDisplay> vivDisplays = new HashMap<>();
    public static final Map<Player, VivDisplay> selectedVivDisplays = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return onConsoleCommand(sender, command, label, args);
        }

        Player player = (Player) sender;
        return onPlayerCommand(player, command, label, args);
    }

    public boolean onPlayerCommand(Player player, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("display")) {
            return onPlayerDisplayCommand(player, args);
        } else if (label.equalsIgnoreCase("advdisplay")) {
            return onPlayerAdvDisplayCommand(player, args);
        } else if (label.equalsIgnoreCase("displaygroup")) {
            return onPlayerDisplayGroupCommand(player, args);
        }
        return true;
    }

    public boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
        String subCommand = args[0].toLowerCase();

        if (label.equalsIgnoreCase("display")) {
            switch (subCommand) {
                case "destroy":
                    Player player = Bukkit.getPlayer(args[4]);
                    ///display destroy nearby 10 10 GreensUsername
                    DisplayCommands.handleDisplayDestroyCommand(player, args);
                    break;
            }
        }

        return true;
    }

    public boolean onPlayerDisplayCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(errMap.get("displayUsage"));
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                DisplayCommands.handleDisplayCreateCommand(player, args);
                break;
            case "closest":
                DisplayCommands.handleDisplayClosestCommand(player);
                break;
            case "nearby":
                DisplayCommands.handleDisplayNearbyCommand(player, args);
                break;
            case "gui":
                DisplayCommands.handleDisplayGUICommand(player);
                break;
            case "destroy":
                DisplayCommands.handleDisplayDestroyCommand(player, args);
                break;
            case "help":
                DisplayCommands.handleDisplayHelpCommand(player);
                break;
            case "replaceitem":
                DisplayCommands.handleDisplayReplaceItemCommand(player);
                break;
            default:
                player.sendMessage(errMap.get("displayInvalidSubcommand"));
        }
        return true;
    }

    public boolean onPlayerAdvDisplayCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(errMap.get("advDisplayUsage"));
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "select":
                DisplayCommands.handleAdvDisplaySelectCommand(player, args);
                break;
            case "setrotation":
            case "changerotation":
                DisplayCommands.handleAdvDisplayRotationCommand(player, args);
                break;
            case "setposition":
            case "changeposition":
                DisplayCommands.handleAdvDisplayPositionCommand(player, args);
                break;
            case "setsize":
            case "changesize":
                DisplayCommands.handleAdvDisplaySizeCommand(player, args);
                break;
            case "rename":
                DisplayCommands.handleAdvDisplayRenameCommand(player, args);
                break;
            case "details":
                DisplayCommands.handleAdvDisplayDetailsCommand(player);
                break;
            case "debug":
                handleDebugCommand(player);
            default:
                player.sendMessage(errMap.get("advDisplayInvalidSubcommand"));
        }
        return true;
    }

    public boolean onPlayerDisplayGroupCommand(Player player, String[] args) {
        if (Config.getConfig().getBoolean("doDisplayGroups") == false) {
            // this is actually enough to disable all of this, since you can't set a parent (create a group) otherwise
            player.sendMessage(errMap.get("displayGroupDisabled"));
            return false;
        }

        if (args.length < 1) {
            player.sendMessage(errMap.get("displayGroupUsage"));
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "parent": // todo: command to temp set default parent, command to clear; reset on rejoin,
                // todo: recursive parenting is recursive
                DisplayGroupCommands.handleDisplayGroupSetParentCommand(player, args);
                break;
            case "unparent":
                DisplayGroupCommands.handleDisplayGroupUnparentCommand(player);
                break;
            case "copypaste":
                DisplayGroupCommands.handleDisplayGroupCopyPasteCommand(player, args);
                break;
            case "rotate":
                DisplayGroupCommands.handleDisplayGroupRotateCommand(player, args);
                break;
            case "translate":
                DisplayGroupCommands.handleDisplayGroupTranslateCommand(player, args);
                break;
            case "show":
                DisplayGroupCommands.handleDisplayGroupShowCommand(player, args);
                break;
            default:
                player.sendMessage(errMap.get("displayGroupInvalidSubcommand"));
        }
        return true;
    }

    // Sends a message to a player, but only if the message is not empty.
    static void sendPlayerMessageIfExists(Player player, String message) {
        if (!message.isEmpty()) {
            player.sendMessage(message);
        }
    }

    static boolean sendPlayerMessageIf(Player player, String message, Boolean condition) {
        if (condition) {
            player.sendMessage(message);
            return true;
        }
        return false;
    }

    /*public static final float YAW_OFFSET = 90f;
    public static final float SHOULDER_X_OFFSET = 5f / 16f;
    public static final float SHOULDER_Y_OFFSET = 22f / 16f;
    public static final float ARM_X_OFFSET = 10f / 16f;

    public Vector getDirection(double yaw, double pitch, double roll) {
        // Convert to radians
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        // Calculate the direction vector
        double x = Math.cos(yawRad) * Math.cos(pitchRad);
        double y = Math.sin(pitchRad);
        double z = Math.sin(yawRad) * Math.cos(pitchRad);

        // Create and return the vector
        return new Vector(x, y, z);
    }*/

    /*public static Location getArmTip(ArmorStand armorStand) {
        // Gets shoulder location
        Location shoulderLocation = armorStand.getLocation().clone();
        shoulderLocation.setYaw(shoulderLocation.getYaw() + YAW_OFFSET);
        org.bukkit.util.Vector direction = shoulderLocation.getDirection();
        shoulderLocation.setX(shoulderLocation.getX() + SHOULDER_X_OFFSET * direction.getX());
        shoulderLocation.setY(shoulderLocation.getY() + SHOULDER_Y_OFFSET);
        shoulderLocation.setZ(shoulderLocation.getZ() + SHOULDER_X_OFFSET * direction.getZ());
        shoulderLocation.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, shoulderLocation, 1);
        // Get Hand Location

        EulerAngle armPose = armorStand.getRightArmPose();
        Vector armDirection = getDirection(armPose.getY(), armPose.getX(), -armPose.getZ());
        armDirection = rotateAroundAxisY(armDirection, Math.toRadians(shoulderLocation.getYaw() - YAW_OFFSET));
        shoulderLocation.setX(shoulderLocation.getX() + ARM_X_OFFSET * armDirection.getX());
        shoulderLocation.setY(shoulderLocation.getY() + ARM_X_OFFSET * armDirection.getY());
        shoulderLocation.setZ(shoulderLocation.getZ() + ARM_X_OFFSET * armDirection.getZ());

        shoulderLocation.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, shoulderLocation, 1);
        return shoulderLocation;
    }*/

    static void handleDebugCommand(Player player) {

    }
}