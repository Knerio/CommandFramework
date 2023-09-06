package de.derioo.manager;

import de.derioo.annotations.CommandProperties;
import de.derioo.interfaces.Command;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * A class to init the Framework and register commands
 */
public class CommandFramework {

    @Getter
    private static JavaPlugin plugin;

    /**
     * Used to init the framework
     * @param plugin the plugin
     */
    public CommandFramework(JavaPlugin plugin) {
        CommandFramework.plugin = plugin;
    }

    /**
     * Register the commands via class
     * @param clazz the class
     */
    public static void register(@NotNull Class<? extends Command> clazz) {
        try {
            CommandFramework.register(clazz.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers commands via the instance
     * @param command the instance
     */
    public static void register(@NotNull Command command) {
        CommandProperties annotation = command.getClass().getAnnotation(CommandProperties.class);
        PluginCommand pluginCommand = CommandFramework.plugin.getCommand(annotation.name());
        if (pluginCommand == null) {
            Bukkit.getConsoleSender().sendMessage(Component.text("The command '" + annotation.name() + "' isn't in the plugin.yml, please add it to use it"));
            return;
        }
        CommandHandler commandHandler = new CommandHandler(command);
        pluginCommand.setExecutor(commandHandler);
        pluginCommand.setTabCompleter(new TabCompleterHandler(command, commandHandler));

    }


}
