package me.vivian.displayer;

import me.vivian.displayer.commands.CommandHandler;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.TransformMath;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

/**
 * Holds unused code in case I need to snag it for something else
 */
public class Unused {
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
}
