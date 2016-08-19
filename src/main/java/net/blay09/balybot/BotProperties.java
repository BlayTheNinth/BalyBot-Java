package net.blay09.balybot;

import lombok.extern.log4j.Log4j2;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

@Log4j2
public class BotProperties {

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
			log.error("Could not save properties file; runtime changes will only last until the next restart", e);
		}
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public String getProperty(String key, String defaultVal) {
		String value = properties.getProperty(key);
		return value != null ? value : defaultVal;
	}

	public int getProperty(String key, int defaultVal) {
		String value = properties.getProperty(key);
		try {
			return value != null ? Integer.parseInt(value) : defaultVal;
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	public boolean getProperty(String key, boolean defaultVal) {
		String value = properties.getProperty(key);
		return value != null ? Boolean.parseBoolean(properties.getProperty(key)) : defaultVal;
	}

}
