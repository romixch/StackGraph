package ch.romix.stackgraph.collector;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import ch.romix.stackgraph.Helpers;

public class StackCollectorTest {

	@Test
	public void testCollectOnceWithFakeStackTrace() {
		InputStream fakeStackAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("FullStack.txt");
		final StackCaller caller = Mockito.mock(StackCaller.class);
		Mockito.when(caller.callJStackForPID(42)).thenReturn(new BufferedReader(new InputStreamReader(fakeStackAsStream)));
		StackCollector collector = new StackCollector(42) {
			@Override
			StackCaller getjStackCaller() {
				return caller;
			}
		};
		StackListener listenerMock = mock(StackListener.class);
		collector.registerListener(listenerMock);
		collector.collectOnce();
		verify(listenerMock, Mockito.times(5)).onStack(any(List.class), any(String.class), any(StackThreadState.class));
	}

	@Test
	public void testCollectOnceWithRealOwnStack() {
		StackCollector collector = new StackCollector(Helpers.getMyProcessId());
		collector.registerListener(new StackListener() {
			@Override
			public void onStack(List<StackElement> stack, String threadName, StackThreadState state) {
				assertNotNull(stack);
				assertTrue(threadName.length() > 0);
			}
		});
		collector.collectOnce();
	}
}
