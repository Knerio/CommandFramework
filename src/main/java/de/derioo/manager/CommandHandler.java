package de.derioo.manager;

import de.derioo.annotations.*;
import de.derioo.interfaces.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        Method matchingMethod = getMatchingMethod(sender, args);

        if (matchingMethod == null) {
            if (sender instanceof Player) {
                this.command.onNoMappingFound((Player) sender, args);
                return false;
            }
            this.command.onNoMappingFound(sender, args);
            return false;
        }

        if (matchingMethod.isAnnotationPresent(Async.class)) {
            Bukkit.getScheduler().runTaskAsynchronously(CommandFramework.getPlugin(), () -> {
                invoke(matchingMethod);
            });
            return false;
        }
        if (matchingMethod.isAnnotationPresent(Sync.class)) {
            Bukkit.getScheduler().runTask(CommandFramework.getPlugin(), () -> {
                invoke(matchingMethod);
            });
            return false;
        }

        invoke(matchingMethod);




        return false;
    }

    private void invoke(Method method) {
        try {
            method.invoke(this.command);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private @Nullable Method getMatchingMethod(CommandSender sender, String[] args) {
        for (Method method : command.getClass().getDeclaredMethods()) {
            Optional<Mapping> annotation = this.getAnnotation(method);
            if (annotation.isEmpty()) continue;


            String[] split = annotation.get().args().split(" ");

            if (split.length != args.length) continue;

            boolean matches = true;


            for (int i = 0; i < split.length; i++) {
                String arg = split[i];
                if (!hasPlaceholder(arg)) {
                    if (equals(arg, args[i], method)) continue;
                    matches = false;
                    continue;
                }
                String[] placeholders = getPlaceholder(arg);
                String[] tabCompletions = method.getAnnotation(TabCompletion.class).args().split(" ");
                for (String placeholder : placeholders) {
                    for (String tabCompletion : tabCompletions) {
                        if (!tabCompletion.startsWith("{" + placeholder + "}")) continue;

                        String afterArrow = tabCompletion.split("->")[1];

                        if (afterArrow.startsWith("~") && afterArrow.endsWith("~")) {
                            String substring = afterArrow.substring(1, afterArrow.length() - 1);

                            switch (substring.toLowerCase()) {

                                case "players":
                                    if (Bukkit.getOnlinePlayers().stream().map(Player::getName).noneMatch(s -> s.equalsIgnoreCase(arg))) {
                                        matches = false;
                                        continue;
                                    }
                                    break;
                                default:
                                    String[] numbers = substring.split("-");
                                    try {
                                        int parsedInt = Integer.parseInt(arg);
                                        if (Integer.parseInt(numbers[0]) >= parsedInt || Integer.parseInt(numbers[1]) <= parsedInt) {
                                            matches = false;
                                            continue;
                                        }
                                    } catch (NumberFormatException e) {
                                        Optional<Method> any = Arrays.stream(command.getClass().getDeclaredMethods()).filter(m -> m.getName().equals(substring)).findAny();
                                        if (any.isEmpty()) {
                                            matches = false;
                                            continue;
                                        }
                                        try {
                                            Object invoked = any.get().invoke(this.command);
                                            if (!(invoked instanceof List<?>)) {
                                                matches = false;
                                                continue;
                                            }
                                            List<String> list = (List<String>) invoked;
                                            if (list.stream().noneMatch(s -> s.equalsIgnoreCase(arg))) {
                                                matches = false;
                                                continue;
                                            }
                                        } catch (IllegalAccessException | InvocationTargetException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    }
                                    break;
                            }


                        } else if (Arrays.stream(afterArrow.split(";")).noneMatch(s -> s.equalsIgnoreCase(arg))) {
                            matches = true;
                            continue;
                        }
                    }
                }
            }

            if (!hasPermissionToExecute(sender, method, annotation.get())) continue;

            if (matches) {
                if (sender instanceof Player) return method;
                if (method.isAnnotationPresent(NeedsNoPlayer.class)) return method;
            }


        }

        return null;
    }

    private boolean hasPermissionToExecute(CommandSender sender, Method method, Mapping annotation) {
        if (annotation.extraPermission())
            return sender.hasPermission(annotation.permission());

        return sender.hasPermission(this.basePermission);
    }

    private Optional<Mapping> getAnnotation(Method method) {
        if (method.isAnnotationPresent(Mapping.class)) return Optional.of(method.getAnnotation(Mapping.class));
        return Optional.empty();
    }

    private boolean equals(String o, String other, Method method) {
        if (method.isAnnotationPresent(IgnoreCase.class)) {
            return o.equalsIgnoreCase(other);
        }
        return o.equals(other);
    }

    private boolean hasPlaceholder(String s) {
        return this.getPlaceholder(s).length != 0;
    }


    private String[] getPlaceholder(String s) {
        String[] emptyArray = new String[0];
        if (!s.contains("{")) return emptyArray;
        if (!s.contains("}")) return emptyArray;
        List<String> list = new ArrayList<>();
        boolean isOpen = false;
        StringBuilder builder = new StringBuilder();
        for (String stringPart : s.split("")) {
            if (stringPart.equals("{")) {
                isOpen = true;
                continue;
            }
            if (stringPart.equals("}")) {
                isOpen = false;
                list.add(builder.toString());
                builder = new StringBuilder();
                continue;
            }
            if (isOpen) builder.append(stringPart);
        }

        return (String[]) list.toArray();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();


        return list;
    }
}
