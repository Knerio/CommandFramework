
# CommandFramework by Derio


# Contents



[Installation](#Installation)
- [Repository](#repository)
- [Dependency](#dependency)

[Getting Started](#getting-started)

[Simple Command](#simple-command)

[Custom Placeholder](#custom-placeholder)

#

## Installation

To use the Framework you have to install it via a Repository and a Dependency

# Repository
  ```
  <repository>
    <id>commandFramework</id>
    <url>https://nexus.derioo.de/nexus/content/repositories/CommandFramework</url>
  </repository>
```
#
# Dependency

```
<dependency>
  <groupId>de.derioo</groupId>
  <artifactId>CommandFramework</artifactId>
  <version>3.0-RELEASE</version>
</dependency>

```

## Getting started

To create Commands and more you first have to initialize the Framework in the onEnable method in your plugin

```java
new CommandFramework(plugin);
```

## Simple Command

To create a basic GUI in e.g. in a CMD you can use the following

```java

@CommandProperties(name = "test", permission = "test.test")
public class TestCommand extends Command {

    @Override
    public void onNoMappingFound(@NotNull Player player, String[] args) {
        player.sendMessage(Component.text("This command is not declared"));
    }

    @Override
    public void onNoMappingFound(@NotNull CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("This command is not declared"));
    }

    @Mapping(args = "help")
    @NeedsNoPlayer
    public void banForDuration(CommandBody body) {

    }

}

```

This will create a command named "test" wich needs no player with a "/test help" sub command

#

To register it use the following:

```java
@Override
public void onEnable() {
    new CommandFramework(this);
    CommandFramework.register(new TestCommand());
}

```



## Custom Placeholder

To create a custom placeholder u can use the following
```java
@CommandProperties(name = "test", permission = "test.test")
public class TestCommand extends Command {

    @Override
    public void onNoMappingFound(@NotNull Player player, String[] args) {
        player.sendMessage(Component.text("This command is not declared"));
    }

    @Override
    public void onNoMappingFound(@NotNull CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("This command is not declared"));
    }

    @Mapping(args = "help {player} {time} {unit}")
    @Possibilities(args = "{player}->~getPlayers~ {unit}->s;m;h {time}->~1-200~")
    @NeedsNoPlayer
    public void banForDuration(CommandBody body) {
        body.executor().sendMessage(Component.text(body.get("player") + " with " + body.get("time") +" unit: " + body.get("unit")));
    }

    public List<String> getPlayers() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}

```

This will create a command wich can be used like this:

/test help <player> <number between 1 and 200> <s | m | h>

#
 
{player} uses a method,

{unit} uses pre possibilities, 

{time} uses every number in between 1 and 200	






