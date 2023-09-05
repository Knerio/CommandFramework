package de.derioo.manager;

import de.derioo.annotations.*;
import de.derioo.interfaces.Command;
import de.derioo.objects.CommandBody;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final String name;
    private final String basePermission;

    private final Command command;

    private final Map<String, String> placeholderMap = new HashMap<>();

    public CommandHandler(@NotNull Command command) {
        CommandInit annotation = command.getClass().getAnnotation(CommandInit.class);
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

        if (matchingMethod.isAnnotationPresent(Async.class)) {
            Bukkit.getScheduler().runTaskAsynchronously(CommandFramework.getPlugin(), () -> {
                invoke(matchingMethod, args, sender);
            });
            return false;
        }
        if (matchingMethod.isAnnotationPresent(Sync.class)) {
            Bukkit.getScheduler().runTask(CommandFramework.getPlugin(), () -> {
                invoke(matchingMethod, args, sender);
            });
            return false;
        }

        invoke(matchingMethod, args, sender);


        return false;
    }

    private void invoke(Method method, String[] args, CommandSender sender) {
        try {
            CommandBody commandBody = new CommandBody(args, sender instanceof Player ? (Player) sender : null, sender, placeholderMap);
            method.invoke(this.command, commandBody);
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
                String arg = args[i];
                if (!hasPlaceholder(split[i])) {
                    if (equals(split[i], args[i], method)) {
                        continue;
                    }
                    matches = false;
                    continue;
                }
                placeholderMap.put(split[i].substring(1, split[i].length() - 1), arg);
                String[] placeholders = getPlaceholder(split[i]);
                for (String placeholder : placeholders) {
                    for (String tabCompletion : method.getAnnotation(TabCompletion.class).args().split(" ")) {
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

                                case "all":
                                    break;
                                default:
                                    String[] numbers = substring.split("-");
                                    try {
                                        int parsedInt = Integer.parseInt(arg);
                                        if (Integer.parseInt(numbers[0]) > parsedInt || Integer.parseInt(numbers[1]) < parsedInt) {
                                            matches = false;
                                            continue;
                                        }
                                    } catch (NumberFormatException e) {
                                        Method[] declaredMethods = command.getClass().getDeclaredMethods();
                                        Optional<Method> any = Arrays.stream(declaredMethods).filter(m -> m.getName().equals(substring)).findFirst();
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
                                            List<?> list = convertObjectToList(invoked);
                                            if (list.stream().noneMatch(s -> s.toString().equalsIgnoreCase(arg))) {
                                                matches = false;
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

    public static List<?> convertObjectToList(Object obj) {
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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();

        for (Method method : this.command.getClass().getDeclaredMethods()) {
            Optional<Mapping> annotation = this.getAnnotation(method);
            if (annotation.isEmpty()) continue;

            if (!hasPermissionToExecute(sender, method, annotation.get()))continue;

            try {
                String s = annotation.get().args().split(" ")[args.length-1];

                if (!this.hasPlaceholder(s)) {
                    list.add(s);
                    continue;
                }

                list.addAll(this.getTranslatedPlaceholder(s, method));
            }catch (IndexOutOfBoundsException e){

            }


        }

        if (this.command.getClass().isAnnotationPresent(DisableSearchCompletion.class)) {
            return list;
        }

        List<String> completeList = new ArrayList<>();
        String currentarg = args[args.length - 1].toLowerCase();
        for (String s : list) {
            try {
                String s1 = s.toLowerCase();
                if (s1.startsWith(currentarg)) {
                    completeList.add(s);
                }
            } catch (Exception ignored) {
            }
        }
        return completeList;
    }

    private List<String> getTranslatedPlaceholder(String placeholder, Method method) {

        for (String tabCompletion : method.getAnnotation(TabCompletion.class).args().split(" ")) {

            if (!tabCompletion.startsWith(placeholder)) continue;

            String afterArrow = tabCompletion.split("->")[1];

            if (afterArrow.startsWith("~") && afterArrow.endsWith("~")) {
                String substring = afterArrow.substring(1, afterArrow.length() - 1);
                switch (substring.toLowerCase()) {

                    case "players":
                        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                    default:
                        String[] numbers = substring.split("-");
                        try {
                            int start = Integer.parseInt(numbers[0]);
                            int end = Integer.parseInt(numbers[1]);
                            List<String> list = new ArrayList<>();
                            for (int i = start; i < end; i++) {
                                list.add(String.valueOf(i));
                            }
                            return list;
                        } catch (NumberFormatException e) {
                            Method[] declaredMethods = command.getClass().getDeclaredMethods();
                            Optional<Method> any = Arrays.stream(declaredMethods).filter(m -> m.getName().equals(substring)).findFirst();
                            if (any.isEmpty()) {
                                continue;
                            }
                            try {
                                Object invoked = any.get().invoke(this.command);
                                if (!(invoked instanceof List<?>)) {
                                    continue;
                                }
                                List<?> list = convertObjectToList(invoked);
                                return list.stream().map(Object::toString).toList();
                            } catch (IllegalAccessException | InvocationTargetException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                }


            } else {
                return Arrays.stream(afterArrow.split(";")).collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

}
