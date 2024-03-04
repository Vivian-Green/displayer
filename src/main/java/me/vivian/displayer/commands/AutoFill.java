package me.vivian.displayer.commands;

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
    static final List<String> displaySubcommands = Arrays.asList("replaceitem", "create", "nearby", "closest", "destroy", "gui", "help", "rename");
    static final List<String> advDisplaySubcommands = Arrays.asList("select", "setrotation", "changerotation", "setposition", "changeposition", "setsize", "changesize", "details");
    static final List<String> displayGroupSubcommands = Arrays.asList("parent", "unparent", "copypaste", "show", "rotate", "translate");

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
        }

        return Collections.emptyList();
    }


    private List<String> handleDisplaySubcommands(CommandSender sender, String[] args) {
        if (args.length == 1) return displaySubcommands;

        List<String> completions = new ArrayList<> ();
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

        return completions;
    }

    private List<String> handleAdvDisplaySubcommands(CommandSender sender, String[] args) {
        if (args.length == 1) return advDisplaySubcommands;

        List<String> completions = new ArrayList<>();
        if (!(sender instanceof Player)) return completions;
        Player player = (Player) sender;

        Location eyeLocation = player.getEyeLocation();
        float yaw = TransformMath.roundTo(eyeLocation.getYaw(), 3);
        float pitch = TransformMath.roundTo(eyeLocation.getPitch(), 3);

        double x = TransformMath.roundTo(eyeLocation.getX(), 3);
        double y = TransformMath.roundTo(eyeLocation.getY(), 3);
        double z = TransformMath.roundTo(eyeLocation.getZ(), 3);

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
                        completions.addAll(Arrays.asList("[roll]", "0"));
                        break;
                }
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
        }

        return completions;
    }

    private List<String> handleDisplayGroupSubcommands(CommandSender sender, String[] args) {
        if (args.length == 1) return displayGroupSubcommands;

        List<String> completions = new ArrayList<>();

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