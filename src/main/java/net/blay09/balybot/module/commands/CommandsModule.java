package net.blay09.balybot.module.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.blay09.balybot.Database;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleContext;
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
					"`server_fk` INTEGER",
					"`channel_fk` INTEGER",
					"`name` VARCHAR(32)",
					"`pattern` VARCHAR(128)",
					"`message` TEXT",
					"`level` INTEGER",
					"`condition` TEXT",
					"`whisper_to` VARCHAR(32)");

			insertCommand = Database.prepareStatement("INSERT INTO `commands` (`server_fk`, `channel_fk`, `name`, `pattern`, `message`, `level`, `condition`, `whisper_to`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
			replaceCommand = Database.prepareStatement("REPLACE INTO `commands` (`id`, `server_fk`, `channel_fk`, `name`, `pattern`, `message`, `level`, `condition`, `whisper_to`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
	public Collection<BotCommand> createCommands(Module module, ModuleContext context) {
		List<BotCommand> commands = Lists.newArrayList();
		commands.add(new SetCommand(module));
		commands.add(new SetRegexCommand(module));
		commands.add(new SetUserLevelCommand(module));
		commands.add(new UnsetCommand(module));

		try {
			Statement stmt = Database.createStatement();
			StringBuilder sb = new StringBuilder("SELECT * FROM `commands` WHERE ");
			if(context.getChannel() != null) {
				sb.append("`channel_fk` = ").append(context.getChannel().getId());
			} else {
				sb.append("`server_fk` = ").append(context.getServer().getId());
			}
			ResultSet rs = stmt.executeQuery(sb.toString());
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

	public int addNewCommand(CustomBotRegexCommand command, Channel channel) throws SQLException {
		boolean channelsShared = channel.getImplementation().areChannelsShared();
		insertCommand.setInt(1, channelsShared ? channel.getServer().getId() : 0);
		insertCommand.setInt(2, !channelsShared ? channel.getId() : 0);
		insertCommand.setString(3, command.getName());
		insertCommand.setString(4, command.getPattern().pattern());
		insertCommand.setString(5, command.getCommandMessage());
		insertCommand.setInt(6, command.getUserLevelValue());
		insertCommand.setString(7, command.getCondition());
		insertCommand.setString(8, command.getWhisperTo());
		insertCommand.execute();
		ResultSet rs = insertCommand.getGeneratedKeys();
		if (rs.next()) {
			return rs.getInt(1);
		}
		return 0;
	}

	public void setCommand(CustomBotRegexCommand command, Channel channel) throws SQLException {
		boolean channelsShared = channel.getImplementation().areChannelsShared();
		replaceCommand.setInt(1, command.getId());
		replaceCommand.setInt(2, channelsShared ? channel.getServer().getId() : 0);
		replaceCommand.setInt(3, !channelsShared ? channel.getId() : 0);
		replaceCommand.setString(4, command.getName());
		replaceCommand.setString(5, command.getPattern().pattern());
		replaceCommand.setString(6, command.getCommandMessage());
		replaceCommand.setInt(7, command.getUserLevelValue());
		replaceCommand.setString(8, command.getCondition());
		replaceCommand.setString(9, command.getWhisperTo());
		replaceCommand.execute();
	}

	public void deleteCommand(CustomBotRegexCommand command) throws SQLException {
		deleteCommand.setInt(1, command.getId());
		deleteCommand.execute();
	}

}
