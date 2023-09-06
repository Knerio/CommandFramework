package de.derioo.testing;

import de.derioo.framework.annotations.CommandProperties;
import de.derioo.framework.annotations.Mapping;
import de.derioo.framework.annotations.NeedsNoPlayer;
import de.derioo.framework.interfaces.Command;
import de.derioo.framework.objects.CommandBody;
import net.kyori.adventure.text.Component;

@CommandProperties(name = "test")
public class TestCommand extends Command {

    @Mapping(args = "")
    @NeedsNoPlayer
    public void root(CommandBody body) {
        body.executor().sendMessage(Component.text("You executed noting hehe"));
    }
    
}
