package ch.romix.stackgraph;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.romix.stackgraph.ConsolePrintMain;

import com.Ostermiller.util.CircularByteBuffer;

public class ConsolePrintMainTest {

	private InputStream oldIn;
	private PrintStream oldOut;

	@Before
	public void setup() {
		oldIn = System.in;
		oldOut = System.out;
	}

	@After
	public void after() {
		System.setIn(oldIn);
		System.setOut(oldOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoArgument() throws IOException {
		ConsolePrintMain.main(new String[] {});
	}

	@Test(expected = NumberFormatException.class)
	public void testNotParseableId() throws IOException {
		ConsolePrintMain.main(new String[] { "abc" });
	}

	@Test
	public void testPropperStart() throws Exception {
		CircularByteBuffer buffer = new CircularByteBuffer();
		System.setIn(buffer.getInputStream());
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(byteOut);
		System.setOut(printStream);
		Helpers.writeToInputStreamAfterSeconds(buffer, 1);
		ConsolePrintMain.main(new String[] { String.valueOf(Helpers.getMyProcessId()) });
		assertTrue("Main should have written something to the console.", byteOut.size() > 0);
	}
}
