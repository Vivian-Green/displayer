package me.vivian.displayer.commands;

import me.vivian.displayer.config.Config;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.ItemManipulation;
import me.vivian.displayerutils.NBTMagic;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.*;

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
        boolean isPlayer = sender instanceof Player;
        if (!isPlayer) {
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
            case "parent":
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
}