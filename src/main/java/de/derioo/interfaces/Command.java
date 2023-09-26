package de.derioo.interfaces;

import de.derioo.objects.CommandBody;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class wich has to be extended to use the framework
 */
public abstract class Command {

    /**
     * Basic constructor
     */
    public Command() {

    }

    /**
     * Is called when no mapping has been found
     * @param player the sender as a player
     * @param args the args
     */
    public void onNoMappingFound(@NotNull Player player, String[] args) {
        player.sendMessage(Component.text("This command is not declared"));
    }

    /**
     * Is called when no mapping has been found
     * @param sender the sender
     * @param args the args
     */
    public void onNoMappingFound(@NotNull CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("This command is not declared"));
    }

    /**
     * Is called on every command, wich would normally be executed
     * @param body the command properties
     * @return true if it should be executed, false if not
     */
    public boolean onAll(CommandBody body) {
        return true;
    }

}
