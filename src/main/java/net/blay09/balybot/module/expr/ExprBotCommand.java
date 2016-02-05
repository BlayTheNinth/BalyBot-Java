package net.blay09.balybot.module.expr;

import net.blay09.balybot.expr.ExpressionLibrary;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

public class ExprBotCommand extends BotCommand {

    private final String prefix;

    public ExprBotCommand(String prefix, UserLevel userLevel) {
        super("expr", "^" + prefix + "expr(?:\\s+(.*)|$)", userLevel);
        this.prefix = prefix;
    }

    @Override
    public String getCommandSyntax() {
        return prefix + "expr <expression>";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        int startIdx = message.indexOf(' ');
        if(startIdx == -1) {
            return "Not enough parameters for expr command. Syntax: " + getCommandSyntax();
        }
        String expr = message.substring(startIdx);
        try {
            Object result = ExpressionLibrary.eval(channel, expr);
            if (result == null) {
                return expr + " = void";
            } else {
                return expr + " = " + result.toString();
            }
        } catch (Throwable e) {
            return "Invalid expression: " + e.getMessage();
        }
    }

}
