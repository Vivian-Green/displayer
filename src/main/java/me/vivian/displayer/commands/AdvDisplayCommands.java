package me.vivian.displayer.commands;

import me.vivian.displayer.config.Config;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.TMath;
import me.vivian.displayerutils.WorldGuardIntegrationWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;

import java.util.UUID;

public class AdvDisplayCommands {
    static FileConfiguration config = Config.config;

    /**
     * writes an awful, technical, details message
     *
     * @param player The player who issued the command.
     */
    static void handleAdvDisplayDetailsCommand(Player player) { // todo: EW AAAAA GROSS EW NO
        // Get the selected VivDisplay for the player
        VivDisplay selectedVivDisplay = DisplayHandler.selectedVivDisplays.get(player.getUniqueId());

        if (selectedVivDisplay == null) return;
        if(!WorldGuardIntegrationWrapper.canEditThisDisplay(player, selectedVivDisplay)) {
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.getError("cantEditDisplayHere"));
            return;
        }

        // Get display information directly from the selected VivDisplay
        Location displayLocation = TMath.locationRoundedTo(selectedVivDisplay.display.getLocation(), 2);
        double currentYaw = TMath.roundTo(displayLocation.getYaw(), 2);
        double currentPitch = TMath.roundTo(displayLocation.getPitch(), 2);
        double currentRoll = TMath.roundTo(TMath.getTransRoll(selectedVivDisplay.display.getTransformation()), 2);

        // Send the details to the player
        player.sendMessage("Display Name: " + selectedVivDisplay.displayName);

        if (selectedVivDisplay.display instanceof ItemDisplay || selectedVivDisplay.display instanceof BlockDisplay) {
            player.sendMessage("Display Material: " + selectedVivDisplay.getMaterial());
            player.sendMessage("Display Size: " + TMath.roundTo(selectedVivDisplay.display.getTransformation().getScale(), 2));
        }

        player.sendMessage("Display Position: X=" + displayLocation.getX() + " Y=" + displayLocation.getY() + " Z=" + displayLocation.getZ());
        player.sendMessage("Display Rotation: Yaw=" + currentYaw + " Pitch=" + currentPitch + " Roll=" + currentRoll);


        player.sendMessage("Distance to Display: " + TMath.roundTo(player.getLocation().distance(displayLocation), 2));

        // Send NBT data related to parent and child
        CommandHandler.sendPlayerAifBelseC(player, "Parent UUID: " + selectedVivDisplay.parentUUID, selectedVivDisplay.isChild);

        player.sendMessage("Is Parent: " + selectedVivDisplay.isParentDisplay());
    }

    /**
     * Selects a display for the player given a UUID.
     *
     * @param player The player who issued the command.
     * @param args   Command arguments: - args[1]: The UUID of the display to select.
     */
    static void handleAdvDisplaySelectCommand(Player player, String[] args) { // this should never be executed by the player
        // todo: early return if player
        if (args.length < 2) return;

        // if UUID specified
        UUID displayUUID;
        try {
            displayUUID = UUID.fromString(args[1]);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("handleAdvDisplaySelectCommand: failed to create a UUID from arg 1 of '/advdisplay select ?'"); // todo: config this
            return;
        }

        Entity entity = Bukkit.getServer().getEntity(displayUUID);
        if (!(entity instanceof Display)) return;
        VivDisplay selectedVivDisplay = new VivDisplay((Display) entity);

        if(!WorldGuardIntegrationWrapper.canEditThisDisplay(player, selectedVivDisplay)) {
            CommandHandler.sendPlayerMsgIfMsg(player, Texts.getError("cantEditDisplayHere"));
            return;
        }

        DisplayHandler.selectedVivDisplays.put(player.getUniqueId(), selectedVivDisplay);
        //ParticleHandler.spawnParticle(selectedVivDisplay.display, null, 100);

        // open gui if selecting from here
        player.performCommand("display gui");
        //player.performCommand("display locate");
    }
}
