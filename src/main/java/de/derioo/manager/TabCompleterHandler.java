package de.derioo.manager;

import de.derioo.annotations.DisableSearchCompletion;
import de.derioo.annotations.DisableTabCompletion;
import de.derioo.annotations.Mapping;
import de.derioo.annotations.Possibilities;
import de.derioo.interfaces.Command;
import org.bukkit.Bukkit;
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
import java.util.stream.Collectors;

/**
 * A Tab Completer handler to handle commands
 */
public class TabCompleterHandler implements TabCompleter {

    private final Command command;

    private final CommandHandler handler;


    /**
     * Used to create an instance of the handler
     *
     * @param command        the command
     * @param commandHandler the handler of the command, used to access util methods
     */
    public TabCompleterHandler(Command command, CommandHandler commandHandler) {
        this.command = command;
        this.handler = commandHandler;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();

        for (Method method : this.command.getClass().getDeclaredMethods()) {
            Optional<Mapping> annotation = this.handler.getAnnotation(method);
            if (annotation.isEmpty()) continue;

            if (method.isAnnotationPresent(DisableTabCompletion.class)) continue;

            if (!this.handler.hasPermissionToExecute(sender, annotation.get())) continue;

            if (args.length != 1) {
                if (lastAreCorrect(method, args[args.length - 1].isEmpty() ? Arrays.copyOf(args, args.length - 1) : args))
                    continue;
            }

            String[] split = annotation.get().args().split(" ");
            if (split.length <= args.length - 1) continue;
            String s = split[args.length - 1];

            if (!this.handler.hasPlaceholder(s)) {
                list.add(s);
                continue;
            }

            list.addAll(this.getTranslatedPlaceholder(s, method));

            System.out.println(list);
        }


        if (this.command.getClass().isAnnotationPresent(DisableSearchCompletion.class)) {
            return list;
        }

        return this.getSearchCompletion(list, args);
    }

    private boolean lastAreCorrect(Method method, String @NotNull [] args) {
        if (args.length == 0) return true;

        Optional<Mapping> annotation = this.handler.getAnnotation(method);
        if (annotation.isEmpty()) throw new RuntimeException();

        boolean isCorrect = false;

        for (int i = 0; i < args.length; i++) {
            String[] split = annotation.get().args().split(" ");
            if (split.length <= i) continue;

            String s = split[i];

            if (!this.handler.hasPlaceholder(s)) {
                if (!args[i].equalsIgnoreCase(s)) {
                    isCorrect = true;
                }
                continue;
            }

            boolean isOnRight = false;
            for (String translatedPlaceholder : this.getTranslatedPlaceholder(s, method)) {
                if (args[i].equalsIgnoreCase(translatedPlaceholder)) {
                    isOnRight = true;
                }
                isCorrect = !isOnRight;
            }

        }
        return isCorrect;
    }

    private @NotNull List<String> getSearchCompletion(@NotNull List<String> list, String @NotNull [] args) {
        List<String> completeList = new ArrayList<>();
        String currentArg = args[args.length - 1].toLowerCase();
        for (String s : list) {
            try {
                String s1 = s.toLowerCase();
                if (s1.startsWith(currentArg)) {
                    completeList.add(s);
                }
            } catch (Exception ignored) {
            }
        }
        return completeList;
    }

    private List<String> getTranslatedPlaceholder(String placeholder, Method method) {

        for (String tabCompletion : method.getAnnotation(Possibilities.class).args().split(" ")) {

            if (!tabCompletion.startsWith(placeholder)) continue;

            String afterArrow = tabCompletion.split("->")[1];

            if (!afterArrow.startsWith("~") || !afterArrow.endsWith("~")) {
                return Arrays.stream(afterArrow.split(";")).collect(Collectors.toList());
            }


            String substring = afterArrow.substring(1, afterArrow.length() - 1);
            if (substring.equalsIgnoreCase("players"))
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            String[] numbers = substring.split("-");

            if (this.handler.isInt(numbers[0]) && this.handler.isInt(numbers[1])) {
                int start = Integer.parseInt(numbers[0]);
                int end = Integer.parseInt(numbers[1]);
                List<String> list = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    list.add(String.valueOf(i));
                }
                return list;
            }

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
                List<?> list = this.handler.convertObjectToList(invoked);
                return list.stream().map(Object::toString).toList();
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }


        }

        return new ArrayList<>();
    }

}
