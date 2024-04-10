package me.vivian.displayer;

import me.vivian.displayer.commands.*;
import me.vivian.displayer.config.Config;
import me.vivian.displayer.config.Texts;
import me.vivian.displayer.display.DisplayHandler;
import me.vivian.displayer.display.VivDisplay;
import me.vivian.displayerutils.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class DisplayPlugin extends JavaPlugin { // this should be final yea?
    public PluginDescriptionFile pluginDesc;

    @Override
    public void onEnable() {
        // init plugin
        pluginDesc = getDescription();

        // ensure config
        // todo: check config validity-
        saveResource("plugin.yml", false);
        saveResource("config.yml", false);
        Config.loadConfig(this);

        saveResource(Config.textsFileName, false);
        Texts.loadTexts(this);

        // init commandHandler & autofill
        CommandExecutor commandHandler = new CommandHandler(this);
        TabCompleter autofill = new AutoFill();

        registerCommand(commandHandler, autofill, "display");
        registerCommand(commandHandler, autofill, "advdisplay");
        registerCommand(commandHandler, autofill, "displaygroup");
        registerCommand(commandHandler, autofill, "textdisplay");

        // init event listeners
        Listener eventListener = new EventListeners(this);
        getServer().getPluginManager().registerEvents(eventListener, this);

        // init display handler
        DisplayHandler.init(this);

        // init plugin integrations
        WorldGuardIntegrationWrapper.init(this);
        LuckPermsIntegration.init(this);

        // yea
        ItemBuilder.init(this);

        // yea
        DisplayCommands.init(this);
        DisplayGroupCommands.init();
        TextDisplayCommands.init();

        VivDisplay.initStatic(this);

        // yea
        NBTMagic.init(this);

        // yea
        GUIBuilder.init();
    }

    public void registerCommand(CommandExecutor commandExecutor, TabCompleter subCommandExecutor, String commandName) {
        System.out.println(commandName);
        Objects.requireNonNull(getCommand(commandName)).setExecutor(commandExecutor);
        Objects.requireNonNull(getCommand(commandName)).setTabCompleter(subCommandExecutor);
    }
}
