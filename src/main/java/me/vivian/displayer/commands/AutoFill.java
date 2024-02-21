package me.vivian.displayer.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AutoFill implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String lowerCaseCommandName = command.getName().toLowerCase();

        if ("display".equals(lowerCaseCommandName)) {
            return handleDisplayerSubcommands(sender, args);
        } else if ("advdisplay".equals(lowerCaseCommandName)) {
            return handleAdvDisplaySubcommands(sender, args);
        } else if ("displaygroup".equals(lowerCaseCommandName)) {
            return handleDisplayGroupSubcommands(sender, args);
        }

        return Collections.emptyList();
    }


    private List<String> handleDisplayerSubcommands(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<> ();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "nearby", "closest", "destroy", "gui", "help"));
        } else {
            String currentSubcommand = args[0].toLowerCase();
            switch (currentSubcommand) {
                case "create":
                    switch (args.length) {
                        case 2:
                            completions.add("item");
                            completions.add("block");
                            break;
                        case 3:
                            completions.add("atSelected");
                            break;
                    }
                    break;
                case "nearby":
                case "closest":
                    if (args.length == 1) {
                        completions.addAll(Arrays.asList("[radius]", "5"));
                    }
                    break;
                case "destroy":
                    switch (args.length) {
                        case 2:
                            completions.add("nearby");
                            break;
                        case 3:
                            completions.addAll(Arrays.asList("[maxCount]", "1"));
                            break;
                        case 4:
                            completions.addAll(Arrays.asList("[radius]", "5"));
                            break;
                    }
                    break;
            }
        }

        return completions;
    }

    private List<String> handleAdvDisplaySubcommands(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("select", "setrotation", "changerotation", "setposition", "changeposition", "setsize", "changesize", "rename", "details"));
        } else {
            float yaw = 0;
            float pitch = 0;
            double x = 0;
            double y = 0;
            double z = 0;
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Location eyeLocation = player.getEyeLocation();

                yaw = eyeLocation.getYaw();
                pitch = eyeLocation.getPitch();

                x = eyeLocation.getX();
                y = eyeLocation.getY();
                z = eyeLocation.getZ();
            }

            String currentSubcommand = args[0].toLowerCase();
            switch (currentSubcommand) {
                case "select":
                    if (args.length == 2) {
                        completions.add("<index>");
                    }
                    break;
                case "setrotation":
                    switch (args.length) {
                        case 2:
                            completions.add(yaw + "");
                            break;
                        case 3:
                            completions.add(pitch + "");
                            break;
                        case 4:
                            completions.addAll(Arrays.asList("<roll>", "0"));
                            break;
                    }
                case "changerotation":
                    switch (args.length) {
                        case 2:
                            completions.add("<yaw>");
                            break;
                        case 3:
                            completions.add("<pitch>");
                            break;
                        case 4:
                            completions.add("[roll]");
                            break;
                    }
                    break;
                case "setposition":
                    switch (args.length) {
                        case 2:
                            completions.add(x + "");
                            break;
                        case 3:
                            completions.add(y + "");
                            break;
                        case 4:
                            completions.add(z + "");
                            break;
                    }
                case "changeposition":
                    switch (args.length) {
                        case 2:
                            completions.add("<x>");
                            break;
                        case 3:
                            completions.add("<y>");
                            break;
                        case 4:
                            completions.add("<z>");
                            break;
                    }
                    break;
                case "setsize":
                case "changesize":
                    if (args.length == 2) {
                        completions.add("<size>");
                    }
                    break;
                case "rename":
                    if (args.length == 2) {
                        completions.add("<name>");
                    }
                    break;
                case "details":
                    // No additional arguments needed for this subcommand
                    break;
            }
        }

        return completions;
    }

    private List<String> handleDisplayGroupSubcommands(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("parent", "unparent", "copypaste", "show", "rotate", "translate"));
        } else {
            String currentSubcommand = args[0].toLowerCase();
            switch (currentSubcommand) {
                case "parent":
                    if (args.length == 2) {
                        completions.add("<parentName>");
                    }
                    break;
                case "unparent":
                case "copypaste":
                case "show":
                    // No additional arguments needed for these subcommands
                    break;
                case "rotate":
                    switch (args.length) {
                        case 2:
                            completions.add("<yaw>");
                            break;
                        case 3:
                            completions.add("<pitch>");
                            break;
                        case 4:
                            completions.add("[roll]");
                            break;
                    }
                    break;
                case "translate":
                    switch (args.length) {
                        case 2:
                            completions.add("<x>");
                            break;
                        case 3:
                            completions.add("<y>");
                            break;
                        case 4:
                            completions.add("<z>");
                            break;
                    }
                    break;
            }
        }

        return completions;
    }
}