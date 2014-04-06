package ch.romix.stackgraph.collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StackCollector {
	private List<StackListener> listeners = new ArrayList<>();
	private StackCaller jStackCaller;
	private int pid;

	public StackCollector(int pid) {
		this.pid = pid;
		jStackCaller = new StackCaller();
	}

	public void registerListener(StackListener listener) {
		listeners.add(listener);
	}

	public void collectOnce() {
		StackProcessor processor = new StackProcessor();
		runJStackAndProcessOutput(pid, processor);
		fireStacks(processor);
	}

	private void runJStackAndProcessOutput(int pid, StackProcessor processor) {
		try (BufferedReader reader = getjStackCaller().callJStackForPID(pid)) {
			processJStackOutput(processor, reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void processJStackOutput(StackProcessor processor, BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			processor.readLine(line);
		}
	}

	private void fireStacks(StackProcessor processor) {
		for (StackListener listener : listeners) {
			for (int i = 0; i < processor.getThreadCount(); i++) {
				listener.onStack(processor.getStack(i), processor.getThreadName(i), processor.getState(i));
			}
		}
	}

	StackCaller getjStackCaller() {
		return jStackCaller;
	}
}
