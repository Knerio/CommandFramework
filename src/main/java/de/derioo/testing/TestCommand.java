package de.derioo.testing;

import de.derioo.annotations.CommandProperties;
import de.derioo.annotations.Mapping;
import de.derioo.annotations.NeedsNoPlayer;
import de.derioo.interfaces.Command;
import de.derioo.objects.CommandBody;
import net.kyori.adventure.text.Component;

@CommandProperties(name = "test")
public class TestCommand extends Command {

    @Mapping(args = " ")
    @NeedsNoPlayer
    public void root(CommandBody body) {
        body.executor().sendMessage(Component.text("You executed noting hehe"));
    }


    @Mapping(args = "1")
    @NeedsNoPlayer
    public void one(CommandBody body) {
        body.executor().sendMessage(Component.text("You executed noting hehe"));
    }
}
