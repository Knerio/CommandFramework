package de.derioo.manager;

import de.derioo.annotations.CommandInit;
import de.derioo.annotations.IgnoreCase;
import de.derioo.annotations.Mapping;
import de.derioo.interfaces.Command;
import it.unimi.dsi.fastutil.shorts.ShortRBTreeSet;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final String name;
    private final String basePermission;

    private final Command command;

    public CommandHandler(@NotNull Command command) {
        CommandInit annotation = command.getClass().getAnnotation(CommandInit.class);
        this.name = annotation.name();
        this.basePermission = annotation.permission();

        this.command = command;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {





        return false;
    }

    private Method getMatchingMethod(CommandSender sender, String[] args) {
        for (Method method : command.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Mapping.class)) continue;
            Mapping annotation = method.getAnnotation(Mapping.class);



            String[] split = annotation.args().split(" ");

            if (split.length != args.length) continue;

            boolean matches = true;


            for (int i = 0; i < split.length; i++) {
                String arg = split[i];
                if (!hasPlaceholder(arg)) {
                    if (equals(arg, args[i], method)) continue;
                    matches = false;
                    continue;
                }
                String[] placeholder = getPlaceholder(arg);

            }



        }

        return null;
    }

    private boolean equals(String o, String other, Method method) {
        if (method.isAnnotationPresent(IgnoreCase.class)) {
            return o.equalsIgnoreCase(other);
        }
        return o.equals(other);
    }

    private boolean hasPlaceholder(String s){
        return this.getPlaceholder(s).length != 0;
    }



    private String[] getPlaceholder(String s) {
        String[] emptyArray = new String[0];
        if (!s.contains("{")) return emptyArray;
        if (!s.contains("}"))return emptyArray;
        List<String> list = new ArrayList<>();
        boolean isOpen = false;
        StringBuilder builder = new StringBuilder();
        for (String stringPart : s.split("")) {
            if (stringPart.equals("{")){
                isOpen = true;
                continue;
            }
            if (stringPart.equals("}")) {
                isOpen = false;
                list.add(builder.toString());
                builder = new StringBuilder();
                continue;
            }
            if (isOpen)builder.append(stringPart);
        }

        return (String[]) list.toArray();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();



        return list;
    }
}
