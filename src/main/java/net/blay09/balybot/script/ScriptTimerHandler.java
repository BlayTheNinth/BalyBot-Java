package net.blay09.balybot.script;

import com.google.common.collect.Lists;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.blay09.balybot.module.Module;

import java.util.List;

public class ScriptTimerHandler implements Runnable {

	public static class Timer {
		private final Module module;
		private final ScriptObjectMirror function;
		private final int interval;
		private final boolean isRepeating;

		private int timePassed;

		public Timer(Module module, ScriptObjectMirror function, int interval, boolean isRepeating) {
			this.module = module;
			this.function = function;
			this.interval = interval;
			this.isRepeating = isRepeating;
		}

		public boolean update(int timePassed) {
			this.timePassed += timePassed;
			if(this.timePassed > interval) {
				ScriptManager.getInstance().setCurrentScript(module);
				ScriptManager.getInstance().callSafely(function, null);
				this.timePassed = 0;
				return isRepeating;
			}
			return true;
		}
	}

	private final List<Timer> timerList = Lists.newArrayList();
	private final Thread thread;
	private volatile boolean running;

	public ScriptTimerHandler() {
		this.thread = new Thread(this, "ScriptTimerHandler");
	}

	public void addTimeout(ScriptObjectMirror function, int interval) {
		synchronized (timerList) {
			timerList.add(new Timer(ScriptManager.getInstance().getCurrentScript(), function, interval, false));
		}
	}

	public void addInterval(ScriptObjectMirror function, int interval) {
		synchronized (timerList) {
			timerList.add(new Timer(ScriptManager.getInstance().getCurrentScript(), function, interval, false));
		}
	}

	public void start() {
		this.thread.start();
	}

	@Override
	public void run() {
		long lastTime = System.currentTimeMillis();
		long now;
		while(running) {
			now = System.currentTimeMillis();
			int timePassed = (int) (lastTime - now);
			synchronized (timerList) {
				for (int i = timerList.size() - 1; i >= 0; i--) {
					if (!timerList.get(i).update(timePassed)) {
						timerList.remove(i);
					}
				}
			}
			lastTime = now;
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {}
		}
	}

	public void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException ignored) {}
	}
}
