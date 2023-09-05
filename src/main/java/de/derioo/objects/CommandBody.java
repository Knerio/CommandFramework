package de.derioo.objects;

import lombok.Data;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.util.Map;

public record CommandBody(String[] args, Player player, CommandExecutor executor, Map<String, String> placeholderMap) {

    public String get(String placeholder) {
        return placeholderMap.getOrDefault(placeholder, null);
    }

    public void addPlaceholder(String placeholder, String value) {
        placeholderMap.put(placeholder, value);
    }
}
