package ch.romix.stackgraph;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.romix.stackgraph.ConsoleStackListener;
import ch.romix.stackgraph.collector.StackElement;
import ch.romix.stackgraph.collector.StackThreadState;

public class ConsoleStackListenerTest {

	private ByteArrayOutputStream sysout;
	private ConsoleStackListener consoleStackListener;

	@Before
	public void before() {
		sysout = new ByteArrayOutputStream();
		System.setOut(new PrintStream(sysout));
		consoleStackListener = new ConsoleStackListener();
	}

	@After
	public void after() {
		System.setOut(null);
	}

	@Test
	public void testSimpleThreadWithOneStackEntry() {
		StackElement stackElement = Helpers.createStackElement("ch.romix.jstackgraph", "Main", "main(Main.java:30)");
		List<StackElement> stack = new ArrayList<>();
		stack.add(stackElement);
		consoleStackListener.onStack(stack, "Main Thread", StackThreadState.RUNNABLE);
		String actual = getSystemOut();
		String expected = "Main Thread  RUNNABLE\n  at " + stackElement.toString();
		assertEquals(expected, actual);
	}

	private String getSystemOut() {
		return sysout.toString().trim();
	}
}
