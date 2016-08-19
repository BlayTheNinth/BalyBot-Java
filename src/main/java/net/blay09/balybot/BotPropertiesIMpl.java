package net.blay09.balybot;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.api.BotProperties;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

@Log4j2
public class BotPropertiesIMpl implements BotProperties {

	private final Properties properties = new Properties();

	public boolean loadFromFile() {
		try(FileReader reader = new FileReader("balybot.properties")) {
			properties.load(reader);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public void saveToFile() {
		try(FileWriter writer = new FileWriter("balybot.properties")) {
			properties.store(writer, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	@Override
	public String getProperty(String key, String defaultVal) {
		String value = properties.getProperty(key);
		return value != null ? value : defaultVal;
	}

	@Override
	public int getProperty(String key, int defaultVal) {
		String value = properties.getProperty(key);
		try {
			return value != null ? Integer.parseInt(value) : defaultVal;
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	@Override
	public boolean getProperty(String key, boolean defaultVal) {
		String value = properties.getProperty(key);
		return value != null ? Boolean.parseBoolean(properties.getProperty(key)) : defaultVal;
	}



	/// TODO TO BE MOVED:

	private static final Table<String, String, String> channelConfig = HashBasedTable.create();

	public static void loadFromDatabase() {
		channelConfig.clear();
		try {
			Statement stmt = Database.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM channel_config");
			while(rs.next()) {
				channelConfig.put(ChannelManager.getName(rs.getInt("config_channel")), rs.getString("config_name"), rs.getString("config_value"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getChannelString(String channel, String option, String defaultVal) {
		String value = channelConfig.get(channel, option);
		return value != null ? value : defaultVal;
	}

	public static void setChannelString(String channel, String option, String value) {
		channelConfig.put(channel, option, value);
		try {
			Database.dbReplaceConfig(channel, option, value);
		} catch (SQLException e) {
			log.error("Failed to update config in database: " + e.getMessage());
			log.error("BotPropertiesIMpl value will reset upon reload.");
		}
	}

	public static int getChannelInt(String channel, String option, int defaultVal) {
		String value = channelConfig.get(channel, option);
		try {
			return value != null ? Integer.parseInt(value) : defaultVal;
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}
}
