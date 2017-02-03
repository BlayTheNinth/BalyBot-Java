package net.blay09.balybot;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.Server;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleContext;
import net.blay09.balybot.module.ModuleDef;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;

@Log4j2
public class ChannelManager {

	private static final Map<Integer, Channel> channels = Maps.newHashMap();
	private static final Multimap<Server, Channel> channelsByServer = ArrayListMultimap.create();
	private static final Multimap<Channel, Module> activeModules = ArrayListMultimap.create();
	private static final Table<Channel, String, String> channelConfig = HashBasedTable.create();

	public static void loadModules() {
		activeModules.clear();
		try {
			Statement stmt = Database.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM `channel_modules`");
			while(rs.next()) {
				Channel channel = getChannel(rs.getInt("channel_fk"));
				String moduleId = rs.getString("module_id");
				ModuleDef moduleDef = BalyBot.getInstance().getModuleDef(moduleId);
				if(moduleDef != null) {
					activeModules.put(channel, moduleDef.create(new ModuleContext(channel)));
				} else {
					log.warn("Channel " + channel + " tried to loadConfig " + moduleId + ", but it could not be found.");
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static Module activateModule(Channel channel, String moduleId) {
		log.info("Activating module " + moduleId + " for " + channel);
		ModuleDef moduleDef = BalyBot.getInstance().getModuleDef(moduleId);
		if(moduleDef != null) {
			Module module = moduleDef.create(new ModuleContext(channel));
			activeModules.put(channel, module);
			try {
				Database.activateChannelModule(channel.getId(), moduleId);
			} catch (SQLException e) {
				log.error("Failed to update module status in database: " + e.getMessage());
				log.error("The module will be deactivated upon reload.");
			}
			return module;
		} else {
			log.error("Module " + moduleId + " can not be found.");
			return null;
		}
	}

	public static void deactivateModule(Channel channel, String moduleId) {
		log.info("Deactivating module " + moduleId + " for " + channel);
		Module foundModule = null;
		for(Module module : activeModules.get(channel)) {
			if(module.getId().equals(moduleId)) {
				foundModule = module;
				break;
			}
		}
		if(foundModule != null) {
			activeModules.remove(channel, foundModule);
			try {
				Database.deactivateChannelModule(channel.getId(), moduleId);
			} catch (SQLException e) {
				log.error("Failed to update module status in database: " + e.getMessage());
				log.error("The module will be reactivated upon reload.");
			}
		} else {
			log.error("Module " + moduleId + " can not be found.");
		}
	}

	public static Collection<Module> getModules(Channel channel) {
		return activeModules.get(channel);
	}

	public static void loadChannels() {
		try {
			Statement stmt = Database.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM `channels`");
			while(rs.next()) {
				addChannel(new Channel(rs.getInt("id"), ServerManager.getServer(rs.getInt("server_fk")), rs.getString("name")));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void addChannel(Channel channel) {
		channels.put(channel.getId(), channel);
		channelsByServer.put(channel.getServer(), channel);
	}

	private static Channel getChannel(int id) {
		return channels.get(id);
	}

	public static void loadConfig() {
		channelConfig.clear();
		try {
			Statement stmt = Database.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM `channel_config` ORDER BY `channel_fk`");
			Channel channel = null;
			while(rs.next()) {
				int channelFK = rs.getInt("channel_fk");
				if(channel == null || channel.getId() != channelFK) {
					channel = ChannelManager.channels.get(channelFK);
				}
				channelConfig.put(channel, rs.getString("name"), rs.getString("value"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getChannelString(Channel channel, String option, String defaultVal) {
		String value = channelConfig.get(channel, option);
		return value != null ? value : defaultVal;
	}

	public static void setChannelString(Channel channel, String option, String value) {
		channelConfig.put(channel, option, value);
		try {
			Database.setChannelConfig(channel.getId(), option, value);
		} catch (SQLException e) {
			log.error("Failed to update config in database: " + e.getMessage());
			log.error("BotProperties value will reset upon reload.");
		}
	}

	public static int getChannelInt(Channel channel, String option, int defaultVal) {
		String value = channelConfig.get(channel, option);
		try {
			return value != null ? Integer.parseInt(value) : defaultVal;
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	public static Collection<Channel> getChannels() {
		return channels.values();
	}

	public static Collection<Channel> getChannelsByServer(Server server) {
		return channelsByServer.get(server);
	}

	public static void postInitModules() {
		for(Module module : activeModules.values()) {
			module.registerCommands();
		}
	}
}
