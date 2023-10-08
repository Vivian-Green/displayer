package me.vivian.displayer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// todo: /display changeposition changeposition changeposition changeposition changeposition...
//  suggest... args? lmao
//  someone else has coded this already-
/**
 * Incredibly jank tab completer for handling subcommands "and their args"
 * reads subcommand information from plugin.yml
 * provides tab completions for subcommands "and their associated arguments"
 */
public class SubCommandExecutor implements TabCompleter {
    private final Map<String, List<String>> subcommandArgs = new HashMap<>();

    /**
     * Constructor
     *
     * @param pluginDescription The plugin's description file containing subcommand information.
     */
    public SubCommandExecutor(PluginDescriptionFile pluginDescription) {
        Map<String, Map<String, Object>> commands = pluginDescription.getCommands();

        for (Map.Entry<String, Map<String, Object>> entry : commands.entrySet()) {
            String subcommand = entry.getKey();
            Map<String, Object> subcommandInfo = entry.getValue();

            if (subcommandInfo.containsKey("subcommands")) {
                // This command has subcommands, parse them
                Map<String, Map<String, Object>> subcommands = (Map<String, Map<String, Object>>) subcommandInfo.get("subcommands");

                List<String> arguments = new ArrayList<>();
                for (Map.Entry<String, Map<String, Object>> subcommandEntry : subcommands.entrySet()) {
                    String subcommandName = subcommandEntry.getKey();
                    arguments.add(subcommandName);
                }

                subcommandArgs.put(subcommand, arguments);
            } else {
                // No subcommands, just list the command itself
                subcommandArgs.put(subcommand, new ArrayList<>());
            }
        }
    }


    /**
     * Provides tab completions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        String[] argsWithCommand = tapeArgsToCommand(command, args);

        if (argsWithCommand.length == 1) {
            return handleFirstArgument(completions, argsWithCommand);
        } else if (argsWithCommand.length > 1) {
            return handleSubsequentArguments(completions, argsWithCommand);
        }

        return completions;
    }


    /**
     * Prepares the command arguments by adding the command label at the beginning.
     *
     * @param command  The command being executed.
     * @param args     The original command arguments.
     * @return         The modified array of arguments with the command label.
     */
    private String[] tapeArgsToCommand(Command command, String[] args) {
        // Create a new array with an additional slot for the command label
        String[] argsWithCommand = new String[args.length + 1];

        // Add the command label to the start of the new array
        argsWithCommand[0] = command.getLabel();

        // Copy the original args to the new array
        System.arraycopy(args, 0, argsWithCommand, 1, args.length);

        return argsWithCommand;
    }

    /**
     * Handles tab completions for the first argument (subcommands).
     *
     * @param completions  The list of completion suggestions to populate.
     * @param args         The array of command arguments.
     * @return             A list of tab completion suggestions for the first argument.
     */
    private List<String> handleFirstArgument(List<String> completions, String[] args) {
        // Provide tab completions for the first argument (subcommands)
        String partialSubcommand = args[0].toLowerCase();

        for (String subcommand : subcommandArgs.keySet()) {
            if (subcommand.startsWith(partialSubcommand)) {
                completions.add(subcommand);
            }
        }

        return completions;
    }


    /**
     * Handles tab completions for subsequent arguments after the first one.
     *
     * @param completions  The list of completion suggestions to populate.
     * @param args         The array of command arguments.
     * @return             A list of tab completion suggestions for subsequent arguments.
     */
    private List<String> handleSubsequentArguments(List<String> completions, String[] args) {
        // Check if a subcommand is specified
        String subcommand = args[0].toLowerCase();

        List<String> subcommandArgList = subcommandArgs.get(subcommand);

        if (subcommandArgList != null && args.length - 2 < subcommandArgList.size()) {
            handleSubcommandArguments(completions, subcommandArgList, args);
        }

        return completions;
    }

    /**
     * Handles tab completions for arguments specific to a subcommand.
     *
     * @param completions       The list of completion suggestions to populate.
     * @param subcommandArgList The list of arguments specific to a subcommand.
     * @param args              The array of command arguments.
     */
    private void handleSubcommandArguments(List<String> completions, List<String> subcommandArgList, String[] args) {
        int argIndex = args.length - 2;

        String partialArg = args[args.length - 1].toLowerCase();

        for (String arg : subcommandArgList) {
            if (arg.startsWith(partialArg)) {
                completions.add(arg);
            }
        }
    }
}
