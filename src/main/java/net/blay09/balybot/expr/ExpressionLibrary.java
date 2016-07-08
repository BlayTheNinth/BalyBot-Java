package net.blay09.balybot.expr;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.jel.CompilationException;
import gnu.jel.CompiledExpression;
import gnu.jel.Evaluator;
import gnu.jel.Library;

import java.util.List;
import java.util.Map;

public class ExpressionLibrary {

	private static Map<String, Library> libraryMap = Maps.newHashMap();
	private static Map<String, Object[]> contextMap = Maps.newHashMap();

	public static Library init(String channelName) {
		List<Class> staticClasses = Lists.newArrayList();
		staticClasses.add(Math.class);
		staticClasses.add(StringExpressions.class);
		staticClasses.add(TwitchExpressions.class);
		List<Class> dynamicClasses = Lists.newArrayList();
		dynamicClasses.add(TwitchContext.class);
		Library library = new Library(staticClasses.toArray(new Class[staticClasses.size()]), dynamicClasses.toArray(new Class[dynamicClasses.size()]), null, null, null);
		try {
			library.markStateDependent("random", null);
		} catch (CompilationException e) {
			e.printStackTrace();
		}
		libraryMap.put(channelName.toLowerCase(), library);

		TwitchContext[] contexts = new TwitchContext[1];
		contexts[0] = new TwitchContext(channelName);
		contextMap.put(channelName.toLowerCase(), contexts);
		return library;
	}

	public static Object eval(String channelName, String expr) throws Throwable {
		Library library = libraryMap.get(channelName.toLowerCase());
		if(library == null) {
			library = init(channelName);
		}
		CompiledExpression compiledExpression = Evaluator.compile(expr, library);
		if(compiledExpression != null) {
			return compiledExpression.evaluate(contextMap.get(channelName.toLowerCase()));
		}
		return null;
	}


}
