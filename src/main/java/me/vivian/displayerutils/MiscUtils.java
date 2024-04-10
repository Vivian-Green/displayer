package me.vivian.displayerutils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class MiscUtils {
    public static void sendHyperlink(String text, String hyperlink, Player player){
        TextComponent message = new TextComponent(text);

        // Set click event to run command for selecting the display using its UUID
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, hyperlink));
    }

    public static int runChecks(Boolean[] checks){
        int i = 0;
        for (boolean check: checks) {
            if (check) return i;
            i++;
        }
        return -1;
    }

    public static String runChecksWithErrs(Boolean[] checks, String[] errs){ // todo: checks have to be able to be evaluated beforehand-
        int errNum = runChecks(checks);
        if (errNum == -1 || errNum >= errs.length) {
            return "";
        }
        return errs[errNum];
    }

    public static String padTextToLength(String text, int maxLen, int alignState) {
        int halfLength = (maxLen - text.length()) / 2;
        StringBuilder halfPaddingBuilder = new StringBuilder(halfLength);
        for (int j = 0; j < halfLength; j++) {
            halfPaddingBuilder.append("_");
        }
        String halfPadding = halfPaddingBuilder.toString();
        String paddingIfCenterOrFull = alignState == 0 ? halfPadding : halfPadding + halfPadding;
        String left = alignState == -1 ? "" : paddingIfCenterOrFull;
        String right = alignState == 1 ? "" : paddingIfCenterOrFull;
        return left + text + right;
    }

    public static String replaceAny(String string, List<String> keys, List<String> replacements) {
        if (keys.size() != replacements.size()) return null;
        for (int i = 0; i < keys.size(); i++) {
            string = string.replace(keys.get(i), replacements.get(i));
        }
        return string;
    }
}
