package net.blay09.balybot;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import lombok.extern.log4j.Log4j2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

@Log4j2
public class Config {

    private static final Map<String, String> globalConfig = Maps.newHashMap();
	private static final Table<String, String, String> channelConfig = HashBasedTable.create();

    public static void load() {
        globalConfig.clear();
		channelConfig.clear();
        try {
            Statement stmt = Database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM config");
            while(rs.next()) {
				int channelId = rs.getInt("config_channel");
				if(channelId == 0) {
					globalConfig.put(rs.getString("config_name"), rs.getString("config_value"));
				} else {
					channelConfig.put(ChannelManager.getName(channelId), rs.getString("config_name"), rs.getString("config_value"));
				}
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public static String getGlobalString(String option, String defaultVal) {
		String value = globalConfig.get(option);
		if(value == null) {
			value = defaultVal;
		}
		return value;
	}

	public static int getGlobalInt(String option, int defaultVal) {
		String value = globalConfig.get(option);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	public static String getChannelString(String channel, String option, String defaultVal) {
		if(!channelConfig.contains(channel, option)) {
			return getGlobalString(option, defaultVal);
		}
		return channelConfig.get(channel, option);
	}

	public static void setChannelString(String channel, String option, String value) {
		channelConfig.put(channel, option, value);
		try {
			Database.dbReplaceConfig(channel, option, value);
		} catch (SQLException e) {
			log.error("Failed to update config in database: " + e.getMessage());
			log.error("Config value will reset upon reload.");
		}
	}

	public static int getChannelInt(String channel, String option, int defaultVal) {
		if(!channelConfig.contains(channel, option)) {
			return getGlobalInt(option, defaultVal);
		}
		String value = channelConfig.get(channel, option);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	public static boolean hasGlobalValue(String option) {
		return globalConfig.containsKey(option);
	}
}
