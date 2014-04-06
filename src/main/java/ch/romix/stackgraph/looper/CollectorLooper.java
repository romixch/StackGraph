package ch.romix.stackgraph.looper;

import java.util.concurrent.atomic.AtomicBoolean;

import ch.romix.stackgraph.collector.StackCollector;

public class CollectorLooper implements Runnable {
	private AtomicBoolean keepLooping;

	private StackCollector jStackCollector;

	public CollectorLooper(StackCollector jStackCollector) {
		this.jStackCollector = jStackCollector;
		keepLooping = new AtomicBoolean(true);
	}

	public void stop() {
		keepLooping.set(false);
	}

	@Override
	public void run() {
		while (keepLooping.get()) {
			jStackCollector.collectOnce();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
		}
	}

}
