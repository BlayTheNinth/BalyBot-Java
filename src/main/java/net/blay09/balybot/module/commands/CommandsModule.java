package net.blay09.balybot.module.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.blay09.balybot.ChannelManager;
import net.blay09.balybot.Database;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleDef;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class CommandsModule extends ModuleDef {

	private PreparedStatement insertCommand;
	private PreparedStatement replaceCommand;
	private PreparedStatement deleteCommand;

	public CommandsModule() {
		super("commands", "Commands Module", "Base module for custom commands. Provides commands to manage modules and config options.");

		try {
			Database.execute("CREATE TABLE IF NOT EXISTS commands (command_id INTEGER PRIMARY KEY " + Database.autoIncrementOrEmpty() + ", command_channel VARCHAR(64) NOT NULL, command_name VARCHAR(32) NOT NULL, command_pattern VARCHAR(128) NOT NULL, command_message TEXT, command_level INTEGER(4), command_condition TEXT, command_whisper VARCHAR(32))");

			insertCommand = Database.prepareStatement("INSERT INTO commands (command_channel, command_name, command_pattern, command_message, command_level, command_condition, command_whisper) VALUES (?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
			replaceCommand = Database.prepareStatement("REPLACE INTO commands (command_id, command_channel, command_name, command_pattern, command_message, command_level, command_condition, command_whisper) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			deleteCommand = Database.prepareStatement("DELETE FROM commands WHERE command_id = ?");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		commandSorting = (o1, o2) -> {
			if(o1 instanceof CustomBotRegexCommand && !(o2 instanceof CustomBotRegexCommand)) {
				return 1;
			} else if(o2 instanceof CustomBotRegexCommand && !(o1 instanceof CustomBotRegexCommand)) {
				return -1;
			} else {
				return Strings.nullToEmpty(o2.getCondition()).length() - Strings.nullToEmpty(o1.getCondition()).length();
			}
		};
	}

	@Override
	public Collection<BotCommand> createCommands(Module module) {
		List<BotCommand> commands = Lists.newArrayList();
		commands.add(new SetCommand(module));
		commands.add(new SetRegexCommand(module));
		commands.add(new SetUserLevelCommand(module));
		commands.add(new UnsetCommand(module));

		try {
			Statement stmt = Database.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM commands WHERE command_channel = " + ChannelManager.getId(module.getChannelName()));
			while(rs.next()) {
				commands.add(new CustomBotRegexCommand(rs.getString("command_name"), rs.getString("command_pattern"), rs.getString("command_message"), rs.getInt("command_level"), rs.getString("command_condition"), rs.getString("command_whisper")));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return commands;
	}

	public int dbInsertCommand(CustomBotRegexCommand command, String channelName) throws SQLException {
		insertCommand.setInt(1, ChannelManager.getId(channelName));
		insertCommand.setString(2, command.getName());
		insertCommand.setString(3, command.getPattern().pattern());
		insertCommand.setString(4, command.getCommandMessage());
		insertCommand.setInt(5, command.getUserLevelValue());
		insertCommand.setString(6, command.getCondition());
		insertCommand.setString(7, command.getWhisperTo());
		insertCommand.execute();
		ResultSet rs = insertCommand.getGeneratedKeys();
		if(rs.next()) {
			return rs.getInt(1);
		}
		return 0;
	}

	public void dbReplaceCommand(CustomBotRegexCommand command, String channelName) throws SQLException {
		replaceCommand.setInt(1, command.getId());
		replaceCommand.setInt(2, ChannelManager.getId(channelName));
		replaceCommand.setString(3, command.getName());
		replaceCommand.setString(4, command.getPattern().pattern());
		replaceCommand.setString(5, command.getCommandMessage());
		replaceCommand.setInt(6, command.getUserLevelValue());
		replaceCommand.setString(7, command.getCondition());
		replaceCommand.setString(8, command.getWhisperTo());
		replaceCommand.execute();
	}

	public void dbDeleteCommand(CustomBotRegexCommand command) throws SQLException {
		deleteCommand.setInt(1, command.getId());
		deleteCommand.execute();
	}

}
