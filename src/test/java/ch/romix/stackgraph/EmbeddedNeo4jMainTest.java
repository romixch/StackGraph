package ch.romix.stackgraph;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import ch.romix.stackgraph.EmbeddedNeo4jMain;

import com.Ostermiller.util.CircularByteBuffer;

public class EmbeddedNeo4jMainTest {

	private InputStream oldIn;
	private PrintStream oldOut;
	private GraphDatabaseService db;

	@Before
	public void before() {
		oldIn = System.in;
		oldOut = System.out;
	}

	@After
	public void after() {
		System.setIn(oldIn);
		System.setOut(oldOut);
		if (db != null)
			db.shutdown();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExceptionIfPIDNotGiven() throws IOException, InterruptedException {
		EmbeddedNeo4jMain.main(new String[] {});
	}

	@Test(expected = NumberFormatException.class)
	public void testExceptionIfPIDNotParseable() throws IOException, InterruptedException {
		EmbeddedNeo4jMain.main(new String[] { "abc" });
	}

	@Test
	public void testStartupOfNeo4j() throws Exception {
		EmbeddedNeo4jMain embeddedMain = new EmbeddedNeo4jMain();
		embeddedMain.startNeo4J();
		db = embeddedMain.getGraphDb();
		assertNotNull(db);
	}

	@Test
	public void testPropperStart() throws Exception {
		CircularByteBuffer buffer = new CircularByteBuffer();
		System.setIn(buffer.getInputStream());
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(byteOut);
		System.setOut(printStream);
		Helpers.writeToInputStreamAfterSeconds(buffer, 5);
		EmbeddedNeo4jMain.main(new String[] { String.valueOf(Helpers.getMyProcessId()) });
		EmbeddedNeo4jMain instance = EmbeddedNeo4jMain.getInstance();
		db = instance.getGraphDb();
		try (Transaction tx = db.beginTx()) {
			Iterable<Node> nodes = GlobalGraphOperations.at(db).getAllNodes();
			assertTrue("Should have written something into neo4j database.", nodes.iterator().hasNext());
		}
	}

}
