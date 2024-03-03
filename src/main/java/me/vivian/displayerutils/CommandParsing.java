package me.vivian.displayerutils;

import me.vivian.displayer.config.Texts;
import org.bukkit.entity.Player;

import java.util.Map;

public class CommandParsing {
    static Map<String, String> errMap = Texts.getErrors();

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
                player.sendMessage(errorMessage);
                return defaultValue; // Return the default value
            }
            return value;
        } catch (NumberFormatException e) {
            player.sendMessage(errorMessage);
            return defaultValue; // Return the default value
        }
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
            player.sendMessage(errMap.get("parseInvalidRotation"));
            return null;
        }

        return new float[] {yawOffset, pitchOffset, rollOffset};
    }

    public static double[] parsePositionOffsets(String[] args, Player player) {
        double x, y, z;

        try {
            x = Double.parseDouble(args[1]);
            y = Double.parseDouble(args[2]);
            z = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(errMap.get("parseInvalidPosition"));
            return null;
        }

        return new double[] {x, y, z};
    }
}
