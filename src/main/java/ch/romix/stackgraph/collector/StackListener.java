package ch.romix.stackgraph.collector;

import java.util.List;

public interface StackListener {

	public void onStack(List<StackElement> stack, String threadName, StackThreadState state);
}
