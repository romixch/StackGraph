package ch.romix.stackgraph.collector;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.romix.stackgraph.collector.StackElement;
import ch.romix.stackgraph.collector.StackProcessor;
import ch.romix.stackgraph.collector.StackThreadState;

public class StackProcessorTest {

	private StackProcessor processor;
	private Iterator<StackElement> stackIterator;

	@Before
	public void before() {
		processor = new StackProcessor();
	}

	@Test
	public void testSimpleThreadNames() throws IOException {
		readStack("SimpleThreadWithStack.txt");
		assertEquals(1, processor.getThreadCount());
		assertEquals("process reaper", processor.getThreadName(0));
	}

	@Test
	public void testSimpleThreadState() throws IOException {
		readStack("SimpleThreadWithStack.txt");
		assertEquals(StackThreadState.RUNNABLE, processor.getState(0));
	}

	@Test
	public void testSimpleThreadStacks() throws IOException {
		readStack("SimpleThreadWithStack.txt");
		prepareAssertStack(processor.getStack(0));
		assertStack("java.lang", "UNIXProcess", "waitForProcessExit(Native Method)");
		assertStack("java.lang", "UNIXProcess", "access$200(UNIXProcess.java:54)");
		assertStack("java.lang", "UNIXProcess$3", "run(UNIXProcess.java:174)");
		assertStack("java.util.concurrent", "ThreadPoolExecutor", "runWorker(ThreadPoolExecutor.java:1145)");
		assertStack("java.util.concurrent", "ThreadPoolExecutor$Worker", "run(ThreadPoolExecutor.java:615)");
		assertStack("java.lang", "Thread", "run(Thread.java:724)");
	}

	@Test
	public void testStackWithConstructor() throws IOException {
		readStack("StackWithConstructor.txt");
		prepareAssertStack(processor.getStack(0));
		assertStack("ch.romix.jstackgraph.collector", "JStackCollector", "<init>(JStackCollector.java:14)");
	}

	@Test
	public void testStackWithStateTerminated() throws IOException {
		readStack("StackWithTerminatedState.txt");
		assertEquals(StackThreadState.TERMINATED, processor.getState(0));
	}

	@Test
	public void testStackWithLockingHints() throws IOException {
		readStack("StackWithLockingHints.txt");
		prepareAssertStack(processor.getStack(0));
		assertStack("java.net", "SocketInputStream", "socketRead0(Native Method)");
		assertStack("java.net", "SocketInputStream", "read(SocketInputStream.java:150)");
		assertStack("java.net", "SocketInputStream", "read(SocketInputStream.java:121)");
		assertStack("sun.nio.cs", "StreamDecoder", "readBytes(StreamDecoder.java:283)");
		assertStack("sun.nio.cs", "StreamDecoder", "implRead(StreamDecoder.java:325)");
		assertStack("sun.nio.cs", "StreamDecoder", "read(StreamDecoder.java:177)");
		assertStack("java.io", "InputStreamReader", "read(InputStreamReader.java:184)");
		assertStack("java.io", "BufferedReader", "fill(BufferedReader.java:154)");
		assertStack("java.io", "BufferedReader", "readLine(BufferedReader.java:317)");
		assertStack("java.io", "BufferedReader", "readLine(BufferedReader.java:382)");
		assertStack("org.eclipse.jdt.internal.junit.runner", "RemoteTestRunner$ReaderThread", "run(RemoteTestRunner.java:140)");
	}

	@Test
	public void testThreadsWithoutStack() throws IOException {
		readStack("ThreadsWithoutStack.txt");
		assertEquals(0, processor.getThreadCount());
	}

	private void prepareAssertStack(List<StackElement> stack) {
		stackIterator = stack.iterator();
	}

	private void assertStack(String packageName, String className, String method) {
		StackElement stackElement = stackIterator.next();
		assertEquals(packageName, stackElement.getPackageName());
		assertEquals(className, stackElement.getClassName());
		assertEquals(method, stackElement.getMethodSignature());
	}

	private void readStack(String resource) throws IOException {
		InputStream fakeStackAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
		InputStreamReader inputStreamReader = new InputStreamReader(fakeStackAsStream);
		try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
			String line;
			while ((line = reader.readLine()) != null) {
				processor.readLine(line);
			}
		}
	}
}
