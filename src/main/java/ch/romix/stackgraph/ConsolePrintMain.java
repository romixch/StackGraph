package ch.romix.stackgraph;

import java.io.IOException;

import ch.romix.stackgraph.collector.StackCollector;
import ch.romix.stackgraph.looper.CollectorLooper;

public class ConsolePrintMain {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			throw new IllegalArgumentException("Must give pid to get stack traces from.");
		}
		Integer pid = Integer.valueOf(args[0]);
		ConsoleStackListener stackListener = new ConsoleStackListener();
		StackCollector stackCollector = new StackCollector(pid);
		stackCollector.registerListener(stackListener);
		CollectorLooper looper = new CollectorLooper(stackCollector);
		Thread thread = new Thread(looper);
		thread.setName("Stack collector loop");
		thread.start();
		System.in.read();
		looper.stop();
	}
}
