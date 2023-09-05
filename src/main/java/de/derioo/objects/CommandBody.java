package de.derioo.objects;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public record CommandBody(String[] args, Player player, CommandSender executor, Map<String, String> placeholderMap) {

    public String get(String placeholder) {
        return placeholderMap.getOrDefault(placeholder, null);
    }

    public void addPlaceholder(String placeholder, String value) {
        placeholderMap.put(placeholder, value);
    }
}
