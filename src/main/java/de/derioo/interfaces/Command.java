package de.derioo.interfaces;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class Command {

    public void onNoPermission(@NotNull CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("You have no permission to execute this command!"));
    }

    public void onNoPermission(@NotNull Player player, String[] args) {
        player.sendMessage(Component.text("You have no permission to execute this command!"));
    }

    public void onNoMappingFound(@NotNull Player player, String[] args) {
        player.sendMessage(Component.text("This command is not declared"));
    }

    public  void onNoMappingFound(@NotNull CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("This command is not declared"));
    }


}