package net.blay09.balybot.module;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.Database;
import net.blay09.balybot.command.BotCommand;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Module {

    private static final Map<String, Class<? extends Module>> availableModules = new HashMap<>();
    private static final Multimap<String, Module> activeModules = HashMultimap.create();

    public static void registerModule(String name, Class<? extends Module> moduleClass) {
        availableModules.put(name, moduleClass);
    }

    public static void load(Database database, EventBus eventBus) {
        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM modules");
            while (rs.next()) {
                Class<? extends Module> moduleClass = availableModules.get(rs.getString("module_name"));
                if (moduleClass != null) {
                    try {
                        String modulePrefix = rs.getString("module_prefix");
                        Module module = moduleClass.getConstructor(String.class, char.class).newInstance(rs.getString("channel_name"), modulePrefix.length() > 0 ? modulePrefix.charAt(0) : '!');
                        ;
                        module.activate(eventBus);
                        activeModules.put(module.getOwnerContext(), module);
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private final List<BotCommand> commands = new ArrayList<>();
    protected final String context;
    protected final char prefix;

    public Module(String context, char prefix) {
        this.context = context;
        this.prefix = prefix;
    }

    public void registerCommand(BotCommand botCommand) {
        commands.add(CommandHandler.addCommand(context, botCommand));
    }

    public void unregisterCommands() {
        for(BotCommand command : commands) {
            CommandHandler.removeCommand(context, command);
        }
        commands.clear();
    }

    public abstract void activate(EventBus eventBus);
    public abstract void deactivate(EventBus eventBus);

    public String getOwnerContext() {
        return context;
    }
}
