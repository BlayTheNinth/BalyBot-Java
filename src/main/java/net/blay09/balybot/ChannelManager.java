package net.blay09.balybot;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleDef;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

@Log4j2
public class ChannelManager {

	private static final BiMap<Integer, String> idToName = HashBiMap.create();
	private static final BiMap<String, Integer> nameToId = idToName.inverse();
	private static final Multimap<String, Module> activeModules = ArrayListMultimap.create();

	public static void joinChannel(String channelName) {
		if(!nameToId.containsKey(channelName)) {
			log.info("Joining channel " + channelName + "...");
			try {
				int id = Database.dbInsertChannel(channelName);
				idToName.put(id, channelName);

				activateModule(channelName, "manager");
				activateModule(channelName, "commands");
				activateModule(channelName, "test");

				BalyBot.getInstance().getClient().join(channelName);

				DocBuilder.buildDocs(channelName);
			} catch (SQLException e) {
				log.error("Failed to join channel: " + e.getMessage());
			}
		} else {
			log.info("Channel " + channelName + " is already joined.");
		}
	}

	public static void partChannel(String channelName) {
		log.info("Leaving channel " + channelName + "...");
		BalyBot.getInstance().getClient().part(channelName);
		try {
			Database.dbUpdateChannelActive(channelName, false);
		} catch (SQLException e) {
			log.error("Failed to update channel status in database: " + e.getMessage());
			log.error("The bot will rejoin the channel upon reload.");
		}
	}

	public static Module activateModule(String channelName, String moduleId) {
		log.info("Activating module " + moduleId + " for " + channelName);
		ModuleDef moduleDef = BalyBot.getInstance().getModuleDef(moduleId);
		if(moduleDef != null) {
			Module module = moduleDef.create(channelName);
			activeModules.put(channelName, module);
			try {
				Database.dbReplaceModule(channelName, moduleId);
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

	public static void deactivateModule(String channelName, String moduleId) {
		log.info("Deactivating module " + moduleId + " for " + channelName);
		Module foundModule = null;
		for(Module module : activeModules.get(channelName)) {
			if(module.getId().equals(moduleId)) {
				foundModule = module;
				break;
			}
		}
		if(foundModule != null) {
			activeModules.remove(channelName, foundModule);
			try {
				Database.dbDeleteModule(channelName, moduleId);
			} catch (SQLException e) {
				log.error("Failed to update module status in database: " + e.getMessage());
				log.error("The module will be reactivated upon reload.");
			}
		} else {
			log.error("Module " + moduleId + " can not be found.");
		}
	}

	public static Collection<Module> getModules(String channelName) {
		return activeModules.get(channelName.toLowerCase());
	}

	public static void loadChannels() {
		try {
			Statement stmt = Database.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM channels");
			while(rs.next()) {
				idToName.put(rs.getInt("channel_id"), rs.getString("channel_name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void loadModules() {
		try {
			Statement stmt = Database.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM modules");
			while(rs.next()) {
				String channelName = idToName.get(rs.getInt("module_channel"));
				String moduleId = rs.getString("module_id");
				ModuleDef moduleDef = BalyBot.getInstance().getModuleDef(moduleId);
				if(moduleDef != null) {
					activeModules.put(channelName, moduleDef.create(channelName));
				} else {
					log.warn("Channel " + channelName + " tried to loadFromDatabase " + moduleId + ", but it could not be found.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Collection<String> getChannels() {
		return idToName.values();
	}

	public static String getName(int id) {
		if(id == 0) {
			return "*";
		}
		return idToName.get(id);
	}

	public static int getId(String name) {
		if(name.equals("*")) {
			return 0;
		}
		return nameToId.get(name);
	}

}
