package me.vivian.displayer;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.CommandParsing;
import me.vivian.displayerutils.TransformMath;
import me.vivian.displayerutils.WorldGuardIntegrationLoader;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;

import java.util.Arrays;
import java.util.Map;

/**
 * Holds unused code in case I need to snag it for something else
 */
public class Unused {
    static Map<String, String> errMap = Texts.getErrors();
    static Map<String, String> msgMap = Texts.getMessages();

    /**
     * constructor... why would you instantiate this?
     */
    public Unused() {}

    /**
     * Determines whether two (locations) are within a given (radius) of each other.
     *
     * @param loc1   The first location.
     * @param loc2   The second location.
     * @param radius The radius within which to check.
     * @return True if the locations are within the specified radius, false otherwise.
     */
    private boolean isWithinRadius(Location loc1, Location loc2, int radius) {
        return loc1.distanceSquared(loc2) <= radius * radius;
    }

    /**
     * Sends a hyperlink to the player for selecting a VivDisplay with a given UUID.
     *
     * @param player    The player to send the hyperlink to.
     * @param vivDisplay The VivDisplay to create a hyperlink for.
     */
    public static void createHyperlink(Player player, VivDisplay vivDisplay) {
        assert vivDisplay != null;

        Location location = vivDisplay.display.getLocation();
        Location playerLocation = player.getLocation();

        // Get distance rounded to 2 places
        double distance = TransformMath.roundTo(location.distance(playerLocation), 2);

        String name = CommandHandler.nbtm.getNBT(vivDisplay.display, "VivDisplayName", String.class);
        if (name == null) name = "";

        Material displayMaterial;
        String displayTypeStr;

        // Get material & type of display
        if (vivDisplay.display instanceof BlockDisplay) {
            displayMaterial = ((BlockDisplay) vivDisplay.display).getBlock().getMaterial();
            displayTypeStr = msgMap.get("displayNearbyHyperlink_BlockDisplayDisplayText");
        } else if (vivDisplay.display instanceof ItemDisplay) {
            ItemStack itemStack = ((ItemDisplay) vivDisplay.display).getItemStack();
            assert itemStack != null;
            displayMaterial = itemStack.getType();
            displayTypeStr = msgMap.get("displayNearbyHyperlink_ItemDisplayDisplayText");
        } else {
            displayMaterial = Material.AIR;
            displayTypeStr = msgMap.get("displayNearbyHyperlink_UnknownDisplayDisplayText");
            System.out.println(errMap.get("displayNearbyFoundUnknownItem"));
            return; // Exit early if the display is borked
        }

        // Create & send message to select this display, if it's not borked
        String hyperLinkText = msgMap.get("displayNearbyHyperlinkText");
        hyperLinkText = hyperLinkText.replace("$DisplayTypeDisplayText", displayTypeStr);
        hyperLinkText = hyperLinkText.replace("$DisplayName", name);
        hyperLinkText = hyperLinkText.replace("$DisplayMaterial", displayMaterial.toString());
        hyperLinkText = hyperLinkText.replace("$Distance", distance + "");

        TextComponent message = new TextComponent(hyperLinkText);

        // Set click event to run command for selecting the display using its UUID
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/advdisplay select " + vivDisplay.display.getUniqueId()));

        player.spigot().sendMessage(message);
    }

    /*
    public static void handleAdvDisplayRotationCommand(Player player, String[] args) {
        boolean isChange = args.length > 0 && "changerotation".equalsIgnoreCase(args[0]);

        if (args.length < 4) {
            CommandHandler.sendPlayerAifBelseC(player, errMap.get("advDisplayChangeRotationUsage"), isChange, errMap.get("advDisplaySetRotationUsage"));
            return;
        }

        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);
        if (selectedVivDisplay == null) return;

        if(!WorldGuardIntegrationLoader.canEditThisDisplay(player, selectedVivDisplay)) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("cantEditDisplayHere"));
            return;
        }

        float[] rotationOffsets = CommandParsing.parseRotationOffsets(player, args);

        if (rotationOffsets == null) return;

        boolean success = isChange ?
                selectedVivDisplay.changeRotation(rotationOffsets[0], rotationOffsets[1], rotationOffsets[2]) :
                selectedVivDisplay.setRotation(rotationOffsets[0], rotationOffsets[1], rotationOffsets[2], player);

        CommandHandler.sendPlayerAifBelseC(player, errMap.get("advDisplayRotationFailed"), !success);
    }

    public static void handleAdvDisplayPositionCommand(Player player, String[] args) {
        // get relevant invalid position err
        boolean isChange = args.length > 0 && "changeposition".equalsIgnoreCase(args[0]);
        if (args.length != 4) {
            CommandHandler.sendPlayerAifBelseC(player, errMap.get("advDisplayChangePositionUsage"), isChange, errMap.get("advDisplaySetPositionUsage"));
            return;
        }

        float[] positionOffsets = CommandParsing.parsePositionOffsets(args, player);
        if (positionOffsets == null) return; // shouldn't err unless the player calls it, which, they fukin shouldn't

        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);
        if (selectedVivDisplay == null) return;

        if(!WorldGuardIntegrationLoader.canEditThisDisplay(player, selectedVivDisplay)) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("cantEditDisplayHere"));
            return;
        }

        boolean success = isChange ?
                selectedVivDisplay.changePosition(positionOffsets[0], positionOffsets[1], positionOffsets[2]) :
                selectedVivDisplay.setPosition(positionOffsets[0], positionOffsets[1], positionOffsets[2], player);

        CommandHandler.sendPlayerAifBelseC(player, errMap.get("advDisplayPositionFailed"), !success);
    }

    public static void handleAdvDisplaySizeCommand(Player player, String[] args) {
        // get relevant invalid size err
        boolean isChange = args.length > 0 && "changesize".equalsIgnoreCase(args[0]);
        String errorMessage = isChange ?
                errMap.get("advDisplayChangeSizeInvalid") :
                errMap.get("advDisplaySetSizeInvalid");

        // ensure selectedDisplay that can be edited by this player
        VivDisplay selectedVivDisplay = DisplayHandler.getSelectedVivDisplay(player);
        if (selectedVivDisplay == null) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("noSelectedDisplay"));
            return;
        }
        if(!WorldGuardIntegrationLoader.canEditThisDisplay(player, selectedVivDisplay)) {
            CommandHandler.sendPlayerMsgIfMsg(player, errMap.get("cantEditDisplayHere"));
            return;
        }

        // get size clamped to range
        Transformation transformation = selectedVivDisplay.display.getTransformation();
        double currentSize = transformation.getScale().x;
        double minSize = config.getDouble("minDisplaySize");
        double maxSize = config.getDouble("maxDisplaySize");
        minSize = isChange ? -currentSize+minSize : minSize; // offset -current size for change size

        double sizeArg = CommandParsing.parseNumberFromArgs(args, 1, minSize, minSize, player, errorMessage); // clamps low values
        double newScale = isChange ? (currentSize + sizeArg) : sizeArg; // offset +current size for change size
        newScale = Math.min(newScale, maxSize); // clamp high values

        if (newScale >= minSize && newScale <= maxSize) { // sanity check
            System.out.println(newScale);
            transformation.getScale().set(newScale);
            selectedVivDisplay.display.setTransformation(transformation);
        } else {
            System.out.println("this path shouldn't be accessible!" + Arrays.toString(args));
            player.sendMessage(errorMessage);
        }
    }
     */
}
