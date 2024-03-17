package me.vivian.displayer.commands;

import me.vivian.displayer.config.Config;
import me.vivian.displayerutils.TransformMath;
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
    static final List<String> displaySubcommands = Arrays.asList("locate", "replaceitem", "create", "nearby", "closest", "destroy", "gui", "help", "rename");
    static final List<String> advDisplaySubcommands = Arrays.asList("select", "details");
    static final List<String> displayGroupSubcommands = Arrays.asList("parent", "unparent", "copypaste", "show", "rotate", "translate");
    static final List<String> textDisplaySubcommands = Arrays.asList("set", "togglebackground", "toggleshadow");

    static final List<String> textDisplaySetSubcommands = Arrays.asList("text", "backgroundcolor", "opacity");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String lowerCaseCommandName = command.getName().toLowerCase();

        switch (lowerCaseCommandName) {
            case "display":
                return handleDisplaySubcommands(sender, args);
            case "advdisplay":
                return handleAdvDisplaySubcommands(sender, args);
            case "displaygroup":
                return handleDisplayGroupSubcommands(sender, args);
            case "textdisplay":
                return handleTextDisplaySubcommands(sender, args);
        }

        return Collections.emptyList();
    }

    private List<String> handleTextDisplaySubcommands(CommandSender sender, String[] args) {
        if (args.length == 1) return textDisplaySubcommands;

        List<String> completions = new ArrayList<> ();
        String currentSubcommand = args[0].toLowerCase();
        switch (currentSubcommand) {
            case "set":
                if (args.length == 2) return textDisplaySetSubcommands;
                currentSubcommand = args[1].toLowerCase();
                switch (currentSubcommand) {
                    case "text":
                        completions.add("something");
                    case "opacity":
                        completions.addAll(Arrays.asList("0.25", "0.5", "0.75", "1"));
                }
                break;
        }

        return completions;
    }


    private List<String> handleDisplaySubcommands(CommandSender sender, String[] args) {
        if (args.length == 1) return displaySubcommands;

        List<String> completions = new ArrayList<> ();
        String currentSubcommand = args[0].toLowerCase();
        switch (currentSubcommand) {
            case "create":
                switch (args.length) {
                    case 2:
                        completions.addAll(Arrays.asList("item", "block", "text"));
                        break;
                    case 3:
                        if (args[1].equals("text")){
                            completions.add("<whatever text>");
                        } else {
                            completions.add("atSelected");
                        }
                        break;
                }
                break;
            case "nearby":
            case "closest":
                if (args.length == 2) {
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
            case "rename":
                if (args.length == 2) {
                    completions.add("<name>");
                }
                break;
        }

        return completions;
    }

    private List<String> handleAdvDisplaySubcommands(CommandSender sender, String[] args) {
        if (args.length == 1) return advDisplaySubcommands;

        List<String> completions = new ArrayList<>();
        if (!(sender instanceof Player)) return completions;

        String currentSubcommand = args[0].toLowerCase();
        switch (currentSubcommand) {
            case "select":
                if (args.length == 2) {
                    completions.add("<index>");
                }
                break;
        }

        return completions;
    }

    private List<String> handleDisplayGroupSubcommands(CommandSender sender, String[] args) {
        if (args.length == 1) return displayGroupSubcommands;
        List<String> completions = new ArrayList<>();
        if (Config.config.getBoolean("doDisplayGroups")) return completions;

        String currentSubcommand = args[0].toLowerCase();
        switch (currentSubcommand) {
            case "parent":
                if (args.length == 2) {
                    completions.add("<parentName>");
                }
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
            case "unparent":
            case "copypaste":
            case "show":
                break;
        }

        return completions;
    }
}