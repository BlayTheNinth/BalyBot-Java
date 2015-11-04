package net.blay09.balybot.module.calc;

import net.blay09.balybot.expr.ExpressionLibrary;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import org.apache.commons.lang3.StringUtils;

public class MathBotCommand extends BotCommand {

    public MathBotCommand() {
        super("math", "^!math\\s?(.*)", UserLevel.REGULAR);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        String message = StringUtils.join(args, ' ', 0, args.length);
        try {
            Object result = ExpressionLibrary.eval(channel, message);
            if (result == null) {
                channel.message(message + " = void");
            } else {
                channel.message(message + " = " + result.toString());
            }
        } catch (Throwable e) {
            channel.message("Invalid expression: " + e.getMessage());
        }
    }

}
