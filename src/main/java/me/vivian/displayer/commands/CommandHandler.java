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
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayUsage"));
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
                CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayInvalidSubcommand"));
        }
        return true;
    }

    public boolean onPlayerAdvDisplayCommand(Player player, String[] args) {
        if (args.length < 1) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("advDisplayUsage"));
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "select":
                AdvDisplayCommands.handleAdvDisplaySelectCommand(player, args);
                break;
            case "setrotation":
            case "changerotation":
                AdvDisplayCommands.handleAdvDisplayRotationCommand(player, args);
                break;
            case "setposition":
            case "changeposition":
                AdvDisplayCommands.handleAdvDisplayPositionCommand(player, args);
                break;
            case "setsize":
            case "changesize":
                AdvDisplayCommands.handleAdvDisplaySizeCommand(player, args);
                break;
            case "rename":
                AdvDisplayCommands.handleAdvDisplayRenameCommand(player, args);
                break;
            case "details":
                AdvDisplayCommands.handleAdvDisplayDetailsCommand(player);
                break;
            case "debug":
                handleDebugCommand(player);
            default:
                CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("advDisplayInvalidSubcommand"));
        }
        return true;
    }

    public boolean onPlayerDisplayGroupCommand(Player player, String[] args) {
        if (!Config.getConfig().getBoolean("doDisplayGroups")) {
            // this is actually enough to disable all of this, since you can't set a parent (create a group) otherwise
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayGroupDisabled"));
            return false;
        }

        if (args.length < 1) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayGroupUsage"));
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
                CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("displayGroupInvalidSubcommand"));
        }
        return true;
    }

    // Sends a message to a player, but only if the message is not empty.
    public static boolean sendPlayerMsgIfMsg(Player player, String message) {
        if (message != null && !message.isEmpty()) {
            player.sendMessage(message);
            return true;
        }
        return false;
    }

    static boolean sendPlayerAifBelseC(Player player, String message, Boolean condition) {
        return sendPlayerAifBelseC(player, message, condition, null);
    }

    static boolean sendPlayerAifBelseC(Player player, String message, Boolean condition, String messageOnFalse) {
        if (condition) {
            sendPlayerMsgIfMsg(player, message);
            return true;
        }
        sendPlayerMsgIfMsg(player, messageOnFalse);
        return false;
    }


    static void handleDebugCommand(Player player) {
        return;
    }
}