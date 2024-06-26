package me.vivian.displayerutils;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Texts;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;

public class CommandParsing {

    /**
     * Parses a double from the specified (index) of (args) and ensures
     * it is >= a given (minValue), defaulting to (defaultValue)
     *
     * @param args         The string array containing the arguments to parse.
     * @param index        The index at which to parse the number.
     * @param minValue     The minimum value that the parsed number must be greater than or equal to.
     * @param defaultValue The default value to return if the index is out of bounds.
     * @param player       The player to whom the error message should be sent.
     * @param errorMessage The error message to send if parsing fails or the value is invalid.
     * @return The parsed number, or the default value if parsing fails or the value is invalid.
     */
    public static double parseNumberFromArgs(String[] args, int index, double minValue, double defaultValue, Player player, String errorMessage) {
        if (args.length <= index) {
            return defaultValue;
        }

        try {
            double value = Double.parseDouble(args[index]);
            if (value < minValue) {
                CommandHandler.sendPlayerMsgIfMsg(player, errorMessage);
                return defaultValue; // Return the default value
            }
            return value;
        } catch (NumberFormatException e) {
            CommandHandler.sendPlayerMsgIfMsg(player, errorMessage);
            return defaultValue; // Return the default value
        }
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder titleCase = new StringBuilder();
        boolean nextCharToTitleCase = true;

        for (char c : input.toCharArray()) {
            c = Character.toLowerCase(c); // Convert all characters to lowercase

            if (nextCharToTitleCase) {
                c = Character.toUpperCase(c); // Make the first character or word start uppercase
            }

            titleCase.append(c);
            nextCharToTitleCase = Character.isWhitespace(c); // Set nextCharToTitleCase to true after whitespace
        }

        return titleCase.toString();
    }

    /**
     * Parses rotation offsets from the given command arguments.
     *
     * @param player The player issuing the command. Used to send error messages if the parsing fails.
     * @param args   The command arguments, where args[1] is the yaw offset, args[2] is the pitch offset, and optionally, args[3] is the roll offset.
     * @return A float array containing the parsed yaw, pitch, and roll offsets, or null if parsing fails.
     */
    public static float[] parseRotationOffsets(Player player, String[] args) {
        float yawOffset, pitchOffset, rollOffset = 0;

        try {
            yawOffset = Float.parseFloat(args[1]);
            pitchOffset = Float.parseFloat(args[2]);

            if (args.length >= 4) {
                rollOffset = Float.parseFloat(args[3]);
            }
        } catch (NumberFormatException e) {
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.getError("parseInvalidRotation"));
            return null;
        }

        return new float[] {yawOffset, pitchOffset, rollOffset};
    }

    public static Vector parseVectorArgs(String[] args, int startArgi) {
        try {
            return new Vector(Double.parseDouble(args[startArgi]), Double.parseDouble(args[startArgi+1]), Double.parseDouble(args[startArgi+2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static byte parseByteFromArg(String opacityArg) {
        if (opacityArg.equals("1")) { // oh no you have to input 0.005 to set an opacity of 1/255 which you just shouldn't anyway
            return (byte) 255;
        } else {
            float opacityFloat;
            int opacityInt;
            try {
                opacityFloat = Float.parseFloat(opacityArg);
                if (opacityFloat < 1) {
                    opacityFloat *= 255;
                }
                opacityInt = (int) opacityFloat + 128;
                if (0 > opacityInt || opacityInt > 255) {
                    return 0;
                }

                return (byte) opacityInt; // can cast direct from float but that feels WRONG and I don't know how it handles that anyway
            } catch (NumberFormatException e) {
                // todo: warn invalid opacity
                return 0;
            }
        }
    }
}
