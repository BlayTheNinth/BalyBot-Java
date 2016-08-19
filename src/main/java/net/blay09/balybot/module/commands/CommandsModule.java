package net.blay09.balybot.module.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.blay09.balybot.Database;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleDef;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

public class CommandsModule extends ModuleDef {

	private PreparedStatement insertCommand;
	private PreparedStatement replaceCommand;
	private PreparedStatement deleteCommand;

	public CommandsModule() {
		super("commands", "Commands Module", "Base module for custom commands. Provides commands to manage modules and config options.");

		try {
			Database.createTable("commands", true,
					"`channel_fk` INTEGER",
					"`name` VARCHAR(32)",
					"`pattern` VARCHAR(128)",
					"`message` TEXT",
					"`level` INTEGER",
					"`condition` TEXT",
					"`whisper_to` VARCHAR(32)");

			insertCommand = Database.prepareStatement("INSERT INTO `commands` (`channel_fk`, `name`, `pattern`, `message`, `level`, `condition`, `whisper_to`) VALUES (?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
			replaceCommand = Database.prepareStatement("REPLACE INTO `commands` (`id`, `channel_fk`, `name`, `pattern`, `message`, `level`, `condition`, `whisper_to`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			deleteCommand = Database.prepareStatement("DELETE FROM `commands` WHERE `id` = ?");
		} catch (SQLException e) {
			throw new RuntimeException(e);
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
			ResultSet rs = stmt.executeQuery("SELECT * FROM `commands` WHERE `channel_fk` = " + module.getChannel().getId());
			while(rs.next()) {
				commands.add(new CustomBotRegexCommand(rs.getString("name"), rs.getString("pattern"), rs.getString("message"), rs.getInt("level"), rs.getString("condition"), rs.getString("whisper_to")));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return commands;
	}

	public int dbInsertCommand(CustomBotRegexCommand command, int channelId) throws SQLException {
		insertCommand.setInt(1, channelId);
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

	public void dbReplaceCommand(CustomBotRegexCommand command, int channelId) throws SQLException {
		replaceCommand.setInt(1, command.getId());
		replaceCommand.setInt(2, channelId);
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
