package ch.romix.stackgraph;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;

import ch.romix.stackgraph.collector.StackCollector;
import ch.romix.stackgraph.collector.StackListener;
import ch.romix.stackgraph.looper.CollectorLooper;
import ch.romix.stackgraph.neo4j.Neo4JStackListener;

public class EmbeddedNeo4jMain {

	private static final String NEO4J_DATA_PATH = "target/stackData";
	private static EmbeddedNeo4jMain INSTANCE;
	private GraphDatabaseService graphDb;

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length != 1) {
			throw new IllegalArgumentException("Must give pid to get the stack traces from.");
		}
		int pid = Integer.parseInt(args[0]);
		INSTANCE = new EmbeddedNeo4jMain();
		INSTANCE.startNeo4J();
		INSTANCE.startCollectorLoop(pid);
	}

	void startNeo4J() throws IOException {
		FileUtils.deleteRecursively(new File(NEO4J_DATA_PATH));
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(NEO4J_DATA_PATH);
		registerShutdownHook(graphDb);
	}

	void startCollectorLoop(int pid) throws IOException, InterruptedException {
		StackListener listener = new Neo4JStackListener(graphDb);
		StackCollector collector = new StackCollector(pid);
		collector.registerListener(listener);
		CollectorLooper looper = new CollectorLooper(collector);
		Thread thread = new Thread(looper);
		thread.setName("Stack collector loop");
		thread.start();
		System.in.read();
		looper.stop();
	}

	GraphDatabaseService getGraphDb() {
		return graphDb;
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (graphDb.isAvailable(0)) {
					graphDb.shutdown();
				}
			}
		});
	}

	public static EmbeddedNeo4jMain getInstance() {
		return INSTANCE;
	}
}
