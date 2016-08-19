package net.blay09.balybot;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.impl.api.BotImplementation;
import net.blay09.balybot.impl.api.Server;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleContext;
import net.blay09.balybot.module.ModuleDef;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Log4j2
public class ServerManager {

	private static final Map<Integer, Server> servers = Maps.newHashMap();
	private static final Multimap<Server, Module> activeModules = ArrayListMultimap.create();

	public static void loadModules() {
		activeModules.clear();
		try {
			Statement stmt = Database.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM `server_modules`");
			while(rs.next()) {
				Server server = getServer(rs.getInt("server_fk"));
				String moduleId = rs.getString("module_id");
				ModuleDef moduleDef = BalyBot.getInstance().getModuleDef(moduleId);
				if(moduleDef != null) {
					activeModules.put(server, moduleDef.create(new ModuleContext(server)));
				} else {
					log.warn("Server " + server + " tried to loadConfig " + moduleId + ", but it could not be found.");
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static Module activateModule(Server server, String moduleId) {
		Optional<Module> optionalModule = activeModules.get(server).stream().filter(module -> module.getId().equals(moduleId)).findAny();
		if(optionalModule.isPresent()) {
			return optionalModule.get();
		}
		log.info("Activating module " + moduleId + " for " + server);
		ModuleDef moduleDef = BalyBot.getInstance().getModuleDef(moduleId);
		if(moduleDef != null) {
			Module module = moduleDef.create(new ModuleContext(server));
			activeModules.put(server, module);
			try {
				Database.activateServerModule(server.getId(), moduleId);
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

	public static void deactivateModule(Server server, String moduleId) {
		log.info("Deactivating module " + moduleId + " for " + server);
		Module foundModule = null;
		for(Module module : activeModules.get(server)) {
			if(module.getId().equals(moduleId)) {
				foundModule = module;
				break;
			}
		}
		if(foundModule != null) {
			activeModules.remove(server, foundModule);
			try {
				Database.deactivateServerModule(server.getId(), moduleId);
			} catch (SQLException e) {
				log.error("Failed to update module status in database: " + e.getMessage());
				log.error("The module will be reactivated upon reload.");
			}
		} else {
			log.error("Module " + moduleId + " can not be found.");
		}
	}

	public static Collection<Module> getModules(Server server) {
		return activeModules.get(server);
	}

	public static void addServer(Server server) {
		servers.put(server.getId(), server);
	}

	public static void loadServers() {
		try {
			Statement stmt = Database.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM `servers`");
			while(rs.next()) {
				BotImplementation impl = BalyBot.getInstance().getImplementation(rs.getString("implementation"));
				Server server = new Server(rs.getInt("id"), impl, rs.getString("host"));
				servers.put(server.getId(), server);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static Server findServer(String host) {
		for(Server server : servers.values()) {
			if(server.getServerHost().equals(host)) {
				return server;
			}
		}
		return null;
	}

	public static Server getServer(int id) {
		return servers.get(id);
	}

	public static Collection<Server> getServers() {
		return servers.values();
	}
}
