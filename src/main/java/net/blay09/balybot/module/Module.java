package net.blay09.balybot.module;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.Database;
import net.blay09.balybot.EventManager;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;

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

    private final List<BotCommand> commands = Lists.newArrayList();
    private final List<ConfigEntry> configEntries = Lists.newArrayList();
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

    private static boolean isModuleActive(IRCChannel channel, String moduleName) {
        for(Module module : activeModules.get(channel.getName())) {
            if(module.getModuleCode().equals(moduleName)) {
                return true;
            }
        }
        return false;
    }

    public static void activateModule(IRCChannel channel, String moduleName, String prefix) {
        if(isModuleActive(channel, moduleName)) {
            deactivateModule(channel, moduleName);
        }
        Class<? extends Module> moduleClass = availableModules.get(moduleName);
        if(moduleClass != null) {
            try {
                BalyBot.instance.getDatabase().activateModule(channel.getName(), moduleName, prefix);
                Module module = moduleClass.getConstructor(String.class, String.class).newInstance(channel.getName(), prefix);
                module.activate(EventManager.get(channel.getName()));
                activeModules.put(module.getOwnerContext(), module);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }

    public static void deactivateModule(IRCChannel channel, String moduleName) {
        Iterator<Module> it = activeModules.get(channel.getName()).iterator();
        while(it.hasNext()) {
            Module module = it.next();
            if(module.getModuleCode().equals(moduleName)) {
                module.unregisterCommands();
                BalyBot.instance.getDatabase().deactivateModule(channel.getName(), moduleName);
                it.remove();
                return;
            }
        }
    }

    public void addConfigEntry(ConfigEntry entry) {
        configEntries.add(entry);
    }
}
