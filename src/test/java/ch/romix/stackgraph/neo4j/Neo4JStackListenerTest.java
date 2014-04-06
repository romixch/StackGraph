package ch.romix.stackgraph.neo4j;

import static ch.romix.stackgraph.neo4j.Neo4jConstants.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.test.TestGraphDatabaseFactory;

import ch.romix.stackgraph.Helpers;
import ch.romix.stackgraph.collector.StackElement;
import ch.romix.stackgraph.collector.StackThreadState;

public class Neo4JStackListenerTest {

	private GraphDatabaseService db;
	private Neo4JStackListener listener;
	private ExecutionEngine engine;

	@Before
	public void before() throws IOException {
		db = new TestGraphDatabaseFactory().newImpermanentDatabase();
		listener = new Neo4JStackListener(db);
		engine = new ExecutionEngine(db);
	}

	@After
	public void after() {
		db.shutdown();
	}

	@Test
	public void testOneSimpleStack() throws Exception {
		List<StackElement> elements = new ArrayList<>();
		addStackElement(elements, "ch.romix.jstackgraph", "CoolClass", "coolStuff(CoolClass.java:10)");
		listener.onStack(elements, "My Thread", StackThreadState.RUNNABLE);
		try (Transaction tx = db.beginTx()) {
			ExecutionResult result = engine.execute("start n=node(*) return n");
			assertTrue(result.iterator().hasNext());
			Iterator<Node> n_column = result.columnAs("n");
			for (Node node : IteratorUtil.asIterable(n_column)) {
				assertEquals(STACKELEMENT_LABEL, node.getLabels().iterator().next().name());
				assertEquals(elements.get(0).asString(), node.getProperty(STACKELEMENT_NODE_PROPERTY));
			}
		}
	}

	@Test
	public void testLeafs() throws Exception {
		setupFourStacks();
		try (Transaction tx = db.beginTx()) {
			List<String> names = getLeafNames();
			assertEquals(3, names.size());
			assertTrue(names.contains("ch.romix.jstackgraph.CoolClass.coolStuff(CoolClass.java:10)"));
			assertTrue(names.contains("ch.romix.jstackgraph.FooClass.fooStuff(FooClass.java:59)"));
			assertTrue(names.contains("java.util.ArrayList.trimToSize(ArrayList.java:442)"));
		}
	}

	@Test
	public void testRelations() throws Exception {
		setupFourStacks();
		try (Transaction tx = db.beginTx()) {
			// TODO
		}
	}

	private void setupFourStacks() {
		listener.onStack(createCoolStack(), "My Thread", StackThreadState.RUNNABLE);
		listener.onStack(createFooStack(), "My Thread", StackThreadState.RUNNABLE);
		listener.onStack(createJDKStack(), "My Thread", StackThreadState.RUNNABLE);
		listener.onStack(createFooStack(), "My Thread", StackThreadState.RUNNABLE);
	}

	private List<StackElement> createCoolStack() {
		List<StackElement> elements = new ArrayList<>();
		addStackElement(elements, "ch.romix.jstackgraph", "CoolClass", "coolStuff(CoolClass.java:10)");
		addStackElement(elements, "ch.romix.jstackgraph", "SomeClass", "someStuff(SomeClass.java:43)");
		addStackElement(elements, "ch.romix.jstackgraph", "Main", "main(Main.java:143)");
		return elements;
	}

	private List<StackElement> createFooStack() {
		List<StackElement> elements = new ArrayList<>();
		addStackElement(elements, "ch.romix.jstackgraph", "FooClass", "fooStuff(FooClass.java:59)");
		addStackElement(elements, "ch.romix.jstackgraph", "SomeClass", "someStuff(SomeClass.java:43)");
		addStackElement(elements, "ch.romix.jstackgraph", "Main", "main(Main.java:143)");
		return elements;
	}

	private List<StackElement> createJDKStack() {
		List<StackElement> elements = new ArrayList<>();
		addStackElement(elements, "java.util", "ArrayList", "trimToSize(ArrayList.java:442)");
		addStackElement(elements, "ch.romix.jstackgraph", "FooClass", "fooStuff(FooClass.java:59)");
		addStackElement(elements, "ch.romix.jstackgraph", "SomeClass", "someStuff(SomeClass.java:43)");
		addStackElement(elements, "ch.romix.jstackgraph", "Main", "main(Main.java:143)");
		return elements;
	}

	private void addStackElement(List<StackElement> elements, String packageName, String className, String methodSignature) {
		StackElement stackElement = Helpers.createStackElement(packageName, className, methodSignature);
		elements.add(stackElement);
	}

	private List<String> getLeafNames() {
		String cypherQuery = "MATCH (a:" + STACKELEMENT_LABEL + ") WHERE a." + LEAFCOUNT_NODE_PROPERTY + " > 0 RETURN a";
		ExecutionResult result = engine.execute(cypherQuery);
		List<String> names = new ArrayList<>();
		Iterator<Node> columns = result.columnAs("a");
		for (Node node : IteratorUtil.asIterable(columns)) {
			names.add((String) node.getProperty(STACKELEMENT_NODE_PROPERTY));
		}
		return names;
	}
}
