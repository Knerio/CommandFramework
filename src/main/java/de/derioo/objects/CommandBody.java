package de.derioo.objects;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Used to receive properties from a sub command
 * @param args the args
 * @param player the executor as a player
 * @param executor the executor as a CommandSender
 * @param placeholderMap the placeholders
 */
public record CommandBody(String[] args, Player player, CommandSender executor, Map<String, String> placeholderMap) {

    /**
     * Gets a specific placeholder
     * @param placeholder the placeholder
     * @return the placeholder
     */
    public String get(String placeholder) {
        return placeholderMap.getOrDefault(placeholder, null);
    }

    /**
     * Adds a placeholder
     * Only used in the core
     * @param placeholder the placeholder
     * @param value the value
     */
    public void addPlaceholder(String placeholder, String value) {
        placeholderMap.put(placeholder, value);
    }
}
