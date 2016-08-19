package net.blay09.balybot;

import com.google.common.collect.Maps;
import net.blay09.balybot.impl.api.BotImplementation;
import net.blay09.balybot.impl.api.Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;

public class ServerManager {

	private static final Map<Integer, Server> servers = Maps.newHashMap();

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
