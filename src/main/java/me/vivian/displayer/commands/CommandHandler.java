package me.vivian.displayer.commands;

import me.vivian.displayer.config.Config;
import me.vivian.displayer.config.Texts;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

// todo: CommandHandler should exist and shouldn't be taped to main-
//  why is eventlisteners the plugin we're passing around-
// todo: aliases in config

// handles commands for creating, manipulating, and interacting with displays
public class CommandHandler implements CommandExecutor {
    public CommandHandler(){} // oops I accidentally a don't need constructor anymore rip

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return onConsoleCommand(sender, command, label, args);
        }

        Player player = (Player) sender;
        return onPlayerCommand(player, command, label, args);
    }

    private boolean onPlayerCommand(Player player, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("display")) {
            return onPlayerDisplayCommand(player, args);
        } else if (label.equalsIgnoreCase("advdisplay")) {
            return onPlayerAdvDisplayCommand(player, args);
        } else if (label.equalsIgnoreCase("displaygroup")) {
            return onPlayerDisplayGroupCommand(player, args);
        } else if (label.equalsIgnoreCase("textdisplay")) {
            return onPlayerTextDisplayCommand(player, args);
        }
        return true;
    }

    private boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
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

    private boolean onPlayerTextDisplayCommand(Player player, String[] args) {
        if (args.length < 1) {
            sendPlayerMsgIfMsg(player, Texts.errors.get("textDisplayUsage"));
            return false;
        }

        TextDisplayCommands.handleTextDisplayCommand(player, args);
        return true;
    }

    private boolean onPlayerDisplayCommand(Player player, String[] args) {
        if (args.length < 1) {
            sendPlayerMsgIfMsg(player, Texts.errors.get("displayUsage"));
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
            case "rename":
                DisplayCommands.handleDisplayRenameCommand(player, args);
                break;
            default:
                sendPlayerMsgIfMsg(player, Texts.errors.get("displayInvalidSubcommand"));
        }
        return true;
    }

    private boolean onPlayerAdvDisplayCommand(Player player, String[] args) {
        if (args.length < 1) {
            sendPlayerMsgIfMsg(player, Texts.errors.get("advDisplayUsage"));
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "select":
                AdvDisplayCommands.handleAdvDisplaySelectCommand(player, args);
                break;
            case "details":
                AdvDisplayCommands.handleAdvDisplayDetailsCommand(player);
                break;
            case "debug":
                handleDebugCommand(player);
            default:
                sendPlayerMsgIfMsg(player, Texts.errors.get("advDisplayInvalidSubcommand"));
        }
        return true;
    }

    private boolean onPlayerDisplayGroupCommand(Player player, String[] args) {
        if (!Config.config.getBoolean("doDisplayGroups")) {
            // this is actually enough to disable all of this, since you can't set a parent (create a group) otherwise
            sendPlayerMsgIfMsg(player, Texts.errors.get("displayGroupDisabled"));
            return false;
        }

        if (args.length < 1) {
            sendPlayerMsgIfMsg(player, Texts.errors.get("displayGroupUsage"));
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
                sendPlayerMsgIfMsg(player, Texts.errors.get("displayGroupInvalidSubcommand"));
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


    private static void handleDebugCommand(Player player) {
        return;
    }
}