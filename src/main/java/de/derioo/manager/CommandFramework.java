package de.derioo.manager;

import de.derioo.annotations.CommandInit;
import de.derioo.interfaces.Command;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class CommandFramework {

    @Getter
    private static JavaPlugin plugin;

    public CommandFramework(JavaPlugin plugin) {
        CommandFramework.plugin = plugin;
    }

    public static void register(@NotNull Class<? extends Command> clazz) {
        try {
            CommandFramework.register(clazz.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void register(@NotNull Command command) {
        CommandInit annotation = command.getClass().getAnnotation(CommandInit.class);
        CommandHandler handler = new CommandHandler(command);
        PluginCommand pluginCommand = CommandFramework.plugin.getCommand(annotation.name());
        if (pluginCommand == null) {
            Bukkit.getConsoleSender().sendMessage(Component.text("The command '" + annotation.name() + "' isn't in the plugin.yml, please add it to use it"));
            return;
        }
        pluginCommand.setExecutor(handler);
        pluginCommand.setTabCompleter(handler);

    }


}
