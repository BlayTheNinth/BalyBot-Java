package net.blay09.balybot.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import gnu.jel.CompilationException;
import gnu.jel.CompiledExpression;
import gnu.jel.Evaluator;
import gnu.jel.Library;
import net.blay09.balybot.impl.api.BotImplementation;
import net.blay09.balybot.impl.api.Channel;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class ExpressionLibrary {

	private final List<Class<?>> globalStaticClasses = Lists.newArrayList();
	private final Multimap<BotImplementation, Class<?>> staticClasses = ArrayListMultimap.create();
	private final Multimap<BotImplementation, Class<?>> dynamicClasses = ArrayListMultimap.create();
	private final List<String> stateDependentFunctions = Lists.newArrayList();

	private final Map<Channel, Library> libraryCache = Maps.newHashMap();
	private final Map<Channel, Object[]> contextCache = Maps.newHashMap();
	private final Table<Channel, String, CompiledExpression> expressionCache = HashBasedTable.create();

	public void registerGlobalStaticClass(Class<?> clazz) {
		globalStaticClasses.add(clazz);
	}

	public void registerStaticClass(BotImplementation impl, Class<?> clazz) {
		staticClasses.put(impl, clazz);
	}

	public void registerDynamicClass(BotImplementation impl, Class<?> clazz) {
		dynamicClasses.put(impl, clazz);
	}

	public void markStateDependent(String functionName) {
		stateDependentFunctions.add(functionName);
	}

	private Library getLibrary(Channel channel) {
		Library library = libraryCache.get(channel);
		if (library == null) {
			List<Class<?>> statics = Lists.newArrayList();
			statics.addAll(globalStaticClasses);
			statics.addAll(staticClasses.get(channel.getImplementation()));

			List<Class<?>> dynamics = Lists.newArrayList();
			dynamics.addAll(dynamicClasses.get(channel.getImplementation()));

			library = new Library(statics.toArray(new Class<?>[statics.size()]), dynamics.toArray(new Class<?>[dynamics.size()]), null, null, null);
			try {
				for (String function : stateDependentFunctions) {
					library.markStateDependent(function, null);
				}
			} catch (CompilationException e) {
				throw new RuntimeException(e);
			}
			libraryCache.put(channel, library);

			Object[] contexts = new Object[dynamics.size()];
			for(int i = 0; i < contexts.length; i++) {
				try {
					contexts[i] = dynamics.get(i).getConstructor(Channel.class).newInstance(channel);
				} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
			contextCache.put(channel, contexts);
		}
		return library;
	}

	public Object eval(Channel channel, String expr) throws Throwable {
		CompiledExpression compiledExpression = expressionCache.get(channel, expr);
		if(compiledExpression == null) {
			compiledExpression = Evaluator.compile(expr, getLibrary(channel));
			if (compiledExpression != null) {
				expressionCache.put(channel, expr, compiledExpression);
			}
		}
		if (compiledExpression != null) {
			return compiledExpression.evaluate(contextCache.get(channel));
		}
		return null;
	}


}
