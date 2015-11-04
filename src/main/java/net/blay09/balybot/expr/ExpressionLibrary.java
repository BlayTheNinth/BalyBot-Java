package net.blay09.balybot.expr;

import gnu.jel.CompilationException;
import gnu.jel.CompiledExpression;
import gnu.jel.Evaluator;
import gnu.jel.Library;
import net.blay09.balybot.irc.IRCChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionLibrary {

    private static Map<String, Library> libraryMap = new HashMap<>();
    private static Map<String, Object[]> contextMap = new HashMap<>();

    public static Library init(IRCChannel channel) {
        List<Class> staticClasses = new ArrayList<>();
        staticClasses.add(Math.class);
        staticClasses.add(StringExpressions.class);
        staticClasses.add(TwitchExpressions.class);
        List<Class> dynamicClasses = new ArrayList<>();
        dynamicClasses.add(TwitchContext.class);
        Library library = new Library(staticClasses.toArray(new Class[staticClasses.size()]), dynamicClasses.toArray(new Class[dynamicClasses.size()]), null, null, null);
        try {
            library.markStateDependent("random", null);
        } catch (CompilationException e) {
            e.printStackTrace();
        }
        libraryMap.put(channel.getName().toLowerCase(), library);

        TwitchContext[] contexts = new TwitchContext[1];
        contexts[0] = new TwitchContext(channel);
        contextMap.put(channel.getName().toLowerCase(), contexts);
        return library;
    }

    public static Object eval(IRCChannel channel, String expr) throws Throwable {
        Library library = libraryMap.get(channel.getName().toLowerCase());
        if(library == null) {
            library = init(channel);
        }
        CompiledExpression compiledExpression = Evaluator.compile(expr, library);
        if(compiledExpression != null) {
            return compiledExpression.evaluate(contextMap.get(channel.getName().toLowerCase()));
        }
        return null;
    }

}
