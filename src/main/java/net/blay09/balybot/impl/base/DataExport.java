package net.blay09.balybot.impl.base;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.ChannelManager;
import net.blay09.balybot.ServerManager;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.command.SimpleMessageBotCommand;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.Server;
import net.blay09.balybot.module.ConfigEntry;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleDef;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Log4j2
public class DataExport {

	public static void exportToJson() {
		JsonObject jsonRoot = new JsonObject();

		JsonObject jsonModules = new JsonObject();
		for(ModuleDef moduleDef : BalyBot.getInstance().getAvailableModules()) {
			JsonObject jsonModule = new JsonObject();
			jsonModule.addProperty("id", moduleDef.getId());
			jsonModule.addProperty("name", moduleDef.getName());
			jsonModule.addProperty("description", moduleDef.getDescription());
			JsonObject jsonConfig = new JsonObject();
			for(ConfigEntry entry : moduleDef.getConfigEntries()) {
				jsonConfig.addProperty("name", entry.name);
				jsonConfig.addProperty("description", entry.description);
				jsonConfig.addProperty("default", entry.defaultVal);
			}
			jsonModule.add("config", jsonConfig);
			jsonModules.add(moduleDef.getId(), jsonModule);
		}
		jsonRoot.add("modules", jsonModules);

		JsonObject jsonServers = new JsonObject();
		for(Server server : ServerManager.getServers()) {
			JsonObject jsonServer = new JsonObject();
			jsonServer.addProperty("id", server.getId());
			jsonServer.addProperty("host", server.getServerHost());
			jsonServer.addProperty("implementation", server.getImplementation().getId());

			JsonArray jsonServerModules = new JsonArray();
			for(Module module : ServerManager.getModules(server)) {
				jsonServerModules.add(new JsonPrimitive(module.getId()));
			}
			jsonServer.add("modules", jsonServerModules);

			JsonArray jsonServerCommands = new JsonArray();
			for(Module module : ServerManager.getModules(server)) {
				for(BotCommand command : module.getCommands()) {
					JsonObject jsonCommand = new JsonObject();
					jsonCommand.addProperty("name", command.getName());
					jsonCommand.addProperty("pattern", command.getPattern().pattern());
					jsonCommand.addProperty("whisper_to", command.getWhisperTo());
					jsonCommand.addProperty("condition", command.getCondition());
					jsonCommand.addProperty("syntax", command.getCommandSyntax());
					jsonCommand.addProperty("userlevel_name", command.getUserLevel().getName());
					jsonCommand.addProperty("userlevel", command.getUserLevelValue());
					jsonCommand.addProperty("message", command instanceof SimpleMessageBotCommand ? ((SimpleMessageBotCommand) command).getCommandMessage() : "");
					jsonCommand.addProperty("module", module.getId());
					jsonServerCommands.add(jsonCommand);
				}
			}
			jsonServer.add("commands", jsonServerCommands);

			JsonObject jsonChannels = new JsonObject();
			for(Channel channel : ChannelManager.getChannelsByServer(server)) {
				JsonObject jsonChannel = new JsonObject();
				jsonChannel.addProperty("id", channel.getId());
				jsonChannel.addProperty("name", channel.getName());

				JsonArray jsonChannelModules = new JsonArray();
				for(Module module : ChannelManager.getModules(channel)) {
					jsonChannelModules.add(new JsonPrimitive(module.getId()));
				}
				jsonChannel.add("modules", jsonChannelModules);

				JsonArray jsonChannelCommands = new JsonArray();
				for(Module module : ChannelManager.getModules(channel)) {
					for(BotCommand command : module.getCommands()) {
						JsonObject jsonCommand = new JsonObject();
						jsonCommand.addProperty("name", command.getName());
						jsonCommand.addProperty("pattern", command.getPattern().pattern());
						jsonCommand.addProperty("whisper_to", command.getWhisperTo());
						jsonCommand.addProperty("condition", command.getCondition());
						jsonCommand.addProperty("syntax", command.getCommandSyntax());
						jsonCommand.addProperty("userlevel_name", command.getUserLevel().getName());
						jsonCommand.addProperty("userlevel", command.getUserLevelValue());
						jsonCommand.addProperty("message", command instanceof SimpleMessageBotCommand ? ((SimpleMessageBotCommand) command).getCommandMessage() : "");
						jsonCommand.addProperty("module", module.getId());
						jsonChannelCommands.add(jsonCommand);
					}
				}
				jsonChannel.add("commands", jsonChannelCommands);

				jsonChannels.add(channel.getName(), jsonChannel);
			}
			jsonServer.add("channels", jsonChannels);
			jsonServers.add(server.getServerHost(), jsonServer);
		}
		jsonRoot.add("servers", jsonServers);

		Gson gson = new Gson();
		try(JsonWriter jsonWriter = new JsonWriter(new FileWriter(new File("balybot_export.json")))) {
			jsonWriter.setIndent("  ");
			gson.toJson(jsonRoot, jsonWriter);
		} catch (IOException e) {
			log.error("Failed to export BalyBot data: {}", e);
		}
	}

}
