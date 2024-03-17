package me.vivian.displayer.commands;

import me.vivian.displayer.config.Config;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.CommandParsing;
import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.Arrays;
import java.util.Map;

public class TextDisplayCommands {
    static FileConfiguration config = Config.config;
    static Double minOpacity = 0.0;
    static int minOpacityInt = 0;


    public static void init(){
        minOpacity = config.getDouble("minDisplayTextOpacity");
        minOpacityInt = (int) (minOpacity*255);
    }

    public static void handleTextDisplaySetTextCommand(Player player, String[] args, TextDisplay textDisplay) {
        String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
        if (text.isEmpty()) { // case text is only whitespace, which is trimmed
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("displayCreateTextNoText"));
            return;
        }

        // actually do things
        text = text.replace("\\\\", "$ESCAPED_BACKSLASH"); // flag escaped \'s
        text = text.replace("\\n", "\n"); // replace not escaped \n's with actual newlines
        text = text.replace("$ESCAPED_BACKSLASH", "\\"); // replace flags with escaped \'s
        text = text.replace("&&", "§"); // handle color codes
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
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("invalidColor"));
            return;
        }

        // Extract RGB from args 3-5
        int red = Integer.parseInt(args[2]);
        int green = Integer.parseInt(args[3]);;
        int blue = Integer.parseInt(args[4]);;

        // ensure that's a color
        if (!(red >= 0 && red <= 255) || !(green >= 0 && green <= 255) || !(blue >= 0 && blue <= 255)) {
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("invalidColor"));
            return;
        }

        Color backgroundColor = Color.fromRGB(red, green, blue);
        textDisplay.setBackgroundColor(backgroundColor);


        // handle alpha values
        byte alpha = 0;

        if (args.length >= 6) {
            alpha = CommandParsing.parseByteFromArg(args[5]);
            if (!(alpha >= 0 && alpha <= 255)) {
                setVivDisplayOpacity(textDisplay, alpha);
                CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("invalidColor"));
            }
        }
    }


    public static void handleTextDisplaySetOpacityCommand(Player player, String[] args, TextDisplay textDisplay) {
        if (args.length < 3) {
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("textDisplaySetOpacityNoOpacity"));
            return;
        }

        String opacityArg = args[2];

        byte opacityByte = CommandParsing.parseByteFromArg(opacityArg);
        if ((int) opacityByte + 128 < minOpacityInt) {
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("textDisplaySetOpacityLowOpacity").replace("$minopacity", (int) (minOpacity * 10) + "%"));
            return;
        }

        setVivDisplayOpacity(textDisplay, opacityByte);
    }

    public static void setVivDisplayOpacity(TextDisplay textDisplay, byte opacityByte) {
        textDisplay.setTextOpacity(opacityByte);

        if ((int) opacityByte + 128 == 255) { // todo is this flag: "is completely transparent" or "has transparency"?
            textDisplay.setSeeThrough(false);
        } else {
            textDisplay.setSeeThrough(true);
        }
    }


    public static void handleTextDisplayCommand(Player player, String[] args) { // todo: filter
        // mise en place
        VivDisplay selectedDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());
        if (selectedDisplay == null){
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("noSelectedDisplay"));
            return;
        }
        if (!(selectedDisplay.display instanceof TextDisplay)){
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.errors.get("displayTextNotTextDisplay"));
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
