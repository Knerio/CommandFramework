package de.derioo;

import de.derioo.manager.CommandFramework;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        new CommandFramework(this);
        CommandFramework.register(new ACommand());
    }

}
