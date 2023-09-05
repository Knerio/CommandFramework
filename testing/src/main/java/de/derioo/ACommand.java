package de.derioo;

import de.derioo.annotations.Async;
import de.derioo.annotations.CommandInit;
import de.derioo.interfaces.Command;
import de.derioo.objects.CommandBody;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandInit(name = "test", permission = "perm")
public class ACommand implements Command {


    @Mapping(args = "{time}{unit} {player}")
    @TabCompletion(args = "{time}->~1-100~ {unit}->s;m;h {player}->~players~")
    public void banForDuration(CommandBody body) {

    }

}
