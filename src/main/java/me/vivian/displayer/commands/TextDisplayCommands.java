package me.vivian.displayer.commands;

import me.vivian.displayer.config.Config;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.CommandParsing;
import me.vivian.displayerutils.MiscUtils;
import me.vivian.displayerutils.TMath;
import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextDisplayCommands {
    static FileConfiguration config = Config.config;
    static Double minOpacity = 0.0;
    static int minOpacityInt = 0;

    static String noSelectedDisplayErr;
    static String invalidColorErr;
    static String displayTextNotTextDisplayErr;
    static String textDisplaySetOpacityNoOpacityErr;
    static String textDisplaySetOpacityLowOpacityErr;
    static String displayCreateTextNoTextErr;

    public static void init(){
        minOpacity = config.getDouble("minDisplayTextOpacity");
        minOpacityInt = (int) (minOpacity*255);

        invalidColorErr = Texts.getError("invalidColor");
        noSelectedDisplayErr = Texts.getError("noSelectedDisplay");
        displayTextNotTextDisplayErr = Texts.getError("displayTextNotTextDisplay");
        textDisplaySetOpacityNoOpacityErr = Texts.getError("textDisplaySetOpacityNoOpacity");
        textDisplaySetOpacityLowOpacityErr = Texts.getError("textDisplaySetOpacityLowOpacity");
        displayCreateTextNoTextErr = Texts.getError("displayCreateTextNoText");
    }

    public static void handleTextDisplaySetTextCommand(Player player, String[] args, TextDisplay textDisplay) {
        String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
        if (text.isEmpty()) { // case text is only whitespace, which is trimmed
            CommandHandler.sendPlayerMsgIfMsg(player, displayCreateTextNoTextErr);
            return;
        }

        // actually do things
        List<String> keys =         List.of("\\\\",               "\\", "$ESCAPED_BACKSLASH", "&&");
        List<String> replacements = List.of("$ESCAPED_BACKSLASH", "\n", "\\",                 "§");
        text = MiscUtils.replaceAny(text, keys, replacements);
        textDisplay.setText(text);
    }

    /**
     * Handles the command to set the background color for a text display.
     *
     * @param player      The player who issued the command.
     * @param args        The command arguments.
     * @param textDisplay The TextDisplay instance for which the background color is set.
     */
    public static void handleTextDisplaySetBackgroundColorCommand(Player player, String[] args, TextDisplay textDisplay) {
        if (args.length < 5) {
            CommandHandler.sendPlayerMsgIfMsg(player, invalidColorErr);
            return;
        }

        // ensure valid color from args 3-5
        Vector colorVec = CommandParsing.parseVectorArgs(args, 2);
        if (colorVec == null || !TMath.vecIsClamped(colorVec, 0, 255)) {
            CommandHandler.sendPlayerMsgIfMsg(player, invalidColorErr);
            return;
        }
        Color backgroundColor = Color.fromRGB((int) colorVec.getX(), (int) colorVec.getY(), (int) colorVec.getZ());
        textDisplay.setBackgroundColor(backgroundColor);

        // handle alpha values
        if (args.length >= 6) {
            setVivDisplayOpacity(textDisplay, CommandParsing.parseByteFromArg(args[5]));
            CommandHandler.sendPlayerMsgIfMsg(player, invalidColorErr);
        }
    }


    public static void handleTextDisplaySetOpacityCommand(Player player, String[] args, TextDisplay textDisplay) {
        if (args.length < 3) {
            CommandHandler.sendPlayerMsgIfMsg(player, textDisplaySetOpacityNoOpacityErr);
            return;
        }

        String opacityArg = args[2];

        byte opacityByte = CommandParsing.parseByteFromArg(opacityArg);
        if ((int) opacityByte + 128 < minOpacityInt) {
            CommandHandler.sendPlayerMsgIfMsg(player, textDisplaySetOpacityLowOpacityErr.replace("$minopacity", (int) (minOpacity * 10) + "%"));
            return;
        }

        setVivDisplayOpacity(textDisplay, opacityByte);
    }

    public static void setVivDisplayOpacity(TextDisplay textDisplay, byte opacityByte) {
        textDisplay.setTextOpacity(opacityByte);
        textDisplay.setSeeThrough((int) opacityByte + 128 != 255);
    }


    public static void handleTextDisplayCommand(Player player, String[] args) { // todo: filter
        // mise en place
        VivDisplay selectedDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
        if (selectedDisplay == null){
            CommandHandler.sendPlayerMsgIfMsg(player, noSelectedDisplayErr);
            return;
        }
        if (!(selectedDisplay.display instanceof TextDisplay)){
            CommandHandler.sendPlayerMsgIfMsg(player, displayTextNotTextDisplayErr);
            return;
        }
        TextDisplay textDisplay = (TextDisplay) selectedDisplay.display;

        String subCommand = args[0].toLowerCase();
        switch (subCommand.toLowerCase()) {
            case "set":
                subCommand = args[1].toLowerCase();
                switch (subCommand.toLowerCase()) {
                    case "text":
                        handleTextDisplaySetTextCommand(player, args, textDisplay);
                        break;
                    case "opacity":
                        handleTextDisplaySetOpacityCommand(player, args, textDisplay);
                        break;
                    case "backgroundcolor":
                        handleTextDisplaySetBackgroundColorCommand(player, args, textDisplay);
                        break;
                }
                break;
            case "togglebackground":
                handleTextDisplayToggleBackgroundCommand(textDisplay);
                break;
            case "toggleshadow":
                handleTextDisplayToggleShadowCommand(textDisplay);
                break;
        }
    }

    private static void handleTextDisplayToggleBackgroundCommand(TextDisplay textDisplay) {
        textDisplay.setDefaultBackground(!textDisplay.isDefaultBackground());
        textDisplay.setSeeThrough(!textDisplay.isDefaultBackground());
    }

    private static void handleTextDisplayToggleShadowCommand(TextDisplay textDisplay) {
        textDisplay.setShadowed(!textDisplay.isShadowed());
    }
}
