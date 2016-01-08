package net.blay09.balybot.module;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.Database;
import net.blay09.balybot.EventManager;
import net.blay09.balybot.command.BotCommand;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public abstract class Module {

    private static final Map<String, Class<? extends Module>> availableModules = new HashMap<>();
    private static final Multimap<String, Module> activeModules = HashMultimap.create();

    public static void registerModule(String name, Class<? extends Module> moduleClass) {
        availableModules.put(name, moduleClass);
    }

    public static void load(Database database) {
        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM modules");
            while (rs.next()) {
                Class<? extends Module> moduleClass = availableModules.get(rs.getString("module_name"));
                if (moduleClass != null) {
                    try {
                        String channelName = rs.getString("channel_name");
                        Module module = moduleClass.getConstructor(String.class, String.class).newInstance(channelName, rs.getString("module_prefix"));
                        module.activate(EventManager.get(channelName));
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
    protected final String prefix;

    public Module(String context, String prefix) {
        this.context = context;
        this.prefix = prefix;
    }

    public void registerCommand(BotCommand botCommand) {
        commands.add(CommandHandler.get(context).addCommand(botCommand));
    }

    public void unregisterCommands() {
        for(BotCommand command : commands) {
            CommandHandler.get(context).removeCommand(command);
        }
        commands.clear();
    }

    public abstract void activate(EventBus eventBus);
    public abstract void deactivate(EventBus eventBus);

    public String getOwnerContext() {
        return context;
    }

    public abstract String getModuleCode();

    public String getModuleName() {
        return getModuleCode();
    }

    public String getModuleDescription() {
        return "<no description set>";
    }

    public static Collection<Module> getActiveModules(String channel) {
        return activeModules.get(channel);
    }

    public Collection<BotCommand> getCommands() {
        return commands;
    }

    public String getPrefix() {
        return prefix;
    }
}
