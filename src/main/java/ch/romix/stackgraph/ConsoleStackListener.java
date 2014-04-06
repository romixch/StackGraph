package ch.romix.stackgraph;

import java.util.List;

import ch.romix.stackgraph.collector.StackElement;
import ch.romix.stackgraph.collector.StackListener;
import ch.romix.stackgraph.collector.StackThreadState;

public class ConsoleStackListener implements StackListener {

	@Override
	public void onStack(List<StackElement> stack, String threadName, StackThreadState state) {
		System.out.println(threadName + "  " + state.name());
		for (int i = 0; i < stack.size(); i++) {
			System.out.println("  at " + stack.get(i));
		}
	}
}
