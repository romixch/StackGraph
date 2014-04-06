package ch.romix.stackgraph.looper;

import org.junit.Test;

import ch.romix.stackgraph.Helpers;
import ch.romix.stackgraph.collector.StackCollector;

public class CollectorLooperTest {

	@Test
	public void testLooping() throws InterruptedException {
		CollectorLooper looper = new CollectorLooper(new StackCollector(Helpers.getMyProcessId()));
		Thread thread = new Thread(looper);
		thread.start();
		Thread.sleep(2500);
		looper.stop();
		thread.join(500);
	}

}
