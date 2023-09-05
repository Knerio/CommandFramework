package de.derioo;

import de.derioo.annotations.Async;
import de.derioo.annotations.CommandInit;
import de.derioo.interfaces.Command;
import de.derioo.objects.CommandBody;

@CommandInit(name = "test", permission = "perm")
public class ACommand implements Command {


    public void sendHelp(CommandBody body) {

    }

}
