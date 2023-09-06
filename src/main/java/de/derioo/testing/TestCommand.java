package de.derioo.testing;

import de.derioo.annotations.CommandProperties;
import de.derioo.annotations.Mapping;
import de.derioo.annotations.NeedsNoPlayer;
import de.derioo.annotations.Possibilities;
import de.derioo.interfaces.Command;
import de.derioo.objects.CommandBody;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

@CommandProperties(name = "test")
public class TestCommand extends Command {

    @Mapping(args = "")
    @NeedsNoPlayer
    public void root(CommandBody body) {
        body.executor().sendMessage(Component.text("You executed noting hehe"));
    }


    @Mapping(args = "1 {player}")
    @Possibilities(args = "{player}->~getPlayers~")
    @NeedsNoPlayer
    public void one(CommandBody body) {
        body.executor().sendMessage(Component.text("You executed noting hehe"));
    }

    @Mapping(args = "13 {int}")
    @Possibilities(args = "{int}->~1-20~")
    public void test(CommandBody body) {

    }

    public List<String> getPlayers() {
        return List.of("DeRio");
    }
}
