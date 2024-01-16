package de.derioo.manager;

import de.derioo.annotations.*;
import de.derioo.interfaces.Command;
import de.derioo.objects.CommandBody;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A command handler to handle commands
 */
public class CommandHandler implements CommandExecutor {

    private final String name;
    private final String basePermission;

    private final Command command;

    private final Map<String, String> placeholderMap = new HashMap<>();

    /**
     * Used to create an instance of the handler
     *
     * @param command the command
     */
    public CommandHandler(@NotNull Command command) {
        CommandProperties annotation = command.getClass().getAnnotation(CommandProperties.class);
        this.name = annotation.name();
        this.basePermission = annotation.permission();

        this.command = command;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        this.placeholderMap.clear();

        Method matchingMethod = getMatchingMethod(sender, args);

        if (matchingMethod == null) {
            if (sender instanceof Player) {
                this.command.onNoMappingFound((Player) sender, args);
                return false;
            }
            this.command.onNoMappingFound(sender, args);
            return false;
        }
        CommandBody commandBody = new CommandBody(args, sender instanceof Player ? (Player) sender : null, sender, placeholderMap);

        if (!this.command.onAll(commandBody)) {
            return false;
        }

        if (matchingMethod.isAnnotationPresent(Async.class)) {
            Bukkit.getScheduler().runTaskAsynchronously(CommandFramework.getPlugin(), () -> {
                invoke(matchingMethod, commandBody);
            });
            return true;
        }
        if (matchingMethod.isAnnotationPresent(Sync.class)) {
            Bukkit.getScheduler().runTask(CommandFramework.getPlugin(), () -> {
                invoke(matchingMethod, commandBody);
            });
            return true;
        }

        invoke(matchingMethod, commandBody);


        return true;
    }

    private void invoke(Method method, CommandBody body) {
        try {

            method.invoke(this.command, body);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private @Nullable Method getMatchingMethod(CommandSender sender, String[] args) {
        for (Method method : command.getClass().getDeclaredMethods()) {
            Optional<Mapping> annotation = this.getAnnotation(method);
            if (annotation.isEmpty()) continue;


            String annotationArgs = annotation.get().args();
            String[] mappingArgsSplit = annotationArgs.isEmpty() ? new String[0] : annotationArgs.split(" ");

            if (mappingArgsSplit.length != args.length) continue;

            boolean matchesMapping = checkIfMatches(mappingArgsSplit, args, method);


            if (!hasPermissionToExecute(sender, annotation.get())) continue;

            if (matchesMapping) {
                if (sender instanceof Player) return method;
                if (method.isAnnotationPresent(NeedsNoPlayer.class)) return method;
            }


        }

        return null;
    }

    private boolean checkIfMatches(String[] mappingArgsSplit, String[] args, Method method) {
        for (int i = 0; i < mappingArgsSplit.length; i++) {
            String arg = args[i];
            if (!hasPlaceholder(mappingArgsSplit[i])) {
                if (equals(mappingArgsSplit[i], args[i], method)) {
                    continue;
                }
                return false;
            }
            placeholderMap.put(mappingArgsSplit[i].substring(1, mappingArgsSplit[i].length() - 1), arg);

            String[] placeholders = getPlaceholder(mappingArgsSplit[i]);

            for (String placeholder : placeholders) {
                for (String tabCompletion : method.getAnnotation(Possibilities.class).args().split(" ")) {
                    if (!tabCompletion.startsWith("{" + placeholder + "}")) continue;

                    String afterArrow = tabCompletion.split("->")[1];

                    if (!afterArrow.startsWith("~") || !afterArrow.endsWith("~")) return true;
                    String substring = afterArrow.substring(1, afterArrow.length() - 1);
                    switch (substring.toLowerCase()) {

                        case "players":
                            if (Bukkit.getOnlinePlayers().stream().map(Player::getName).noneMatch(s -> s.equalsIgnoreCase(arg))) {
                                return false;
                            }
                            break;

                        case "all":
                            break;
                        case "int":
                            if (!this.isInt(arg)) return false;
                            break;
                        default:
                            String[] numbers = substring.split("-");

                            if (isInt(numbers[0]) && isInt(numbers[1]) && isInt(arg)) {
                                int parsedInt = Integer.parseInt(arg);
                                if (Integer.parseInt(numbers[0]) > parsedInt || Integer.parseInt(numbers[1]) < parsedInt) {
                                    return false;
                                }
                            }

                            Method[] declaredMethods = command.getClass().getDeclaredMethods();
                            Optional<Method> any = Arrays.stream(declaredMethods).filter(m -> m.getName().equals(substring)).findFirst();
                            if (any.isEmpty()) return false;

                            try {
                                Object invoked = any.get().invoke(this.command);
                                if (!(invoked instanceof List<?>)) return false;

                                List<?> list = convertObjectToList(invoked);
                                if (list.stream().noneMatch(s -> s.toString().equalsIgnoreCase(arg))) return false;

                            } catch (IllegalAccessException | InvocationTargetException ex) {
                                throw new RuntimeException(ex);
                            }


                            break;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Checks if string is int
     * @param s the string
     * @return the bool
     */
    public boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    /**
     * Checks if sender has permission to execute
     * @param sender the sender
     * @param annotation the annotation
     * @return the bool
     */
    public boolean hasPermissionToExecute(CommandSender sender, Mapping annotation) {
        if (annotation.extraPermission())
            return sender.hasPermission(annotation.permission());

        return sender.hasPermission(this.basePermission);
    }

    /**
     * Gets an optional annotation
     * @param method the method to get the annotation from
     * @return the optional
     */
    public Optional<Mapping> getAnnotation(Method method) {
        if (method.isAnnotationPresent(Mapping.class)) return Optional.of(method.getAnnotation(Mapping.class));
        return Optional.empty();
    }

    private boolean equals(String o, String other, Method method) {
        if (method.isAnnotationPresent(IgnoreCase.class)) {
            return o.equalsIgnoreCase(other);
        }
        return o.equals(other);
    }

    /**
     * Checks if a stri#ng has a placeholder
     * @param s the string
     * @return the bool
     */
    public boolean hasPlaceholder(String s) {
        return this.getPlaceholder(s).length != 0;
    }

    /**
     * Converts an object to a list
     *
     * @param obj the object
     * @return the list
     */
    public List<?> convertObjectToList(Object obj) {
        List<?> list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[]) obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>) obj);
        }
        return list;
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
        return list.toArray(new String[0]);
    }


}
