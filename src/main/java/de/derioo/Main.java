package de.derioo;

import de.derioo.manager.CommandFramework;
import de.derioo.testing.TestCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {


    @Override
    public void onEnable() {
        new CommandFramework(this);
        CommandFramework.register(new TestCommand());
    }

}
