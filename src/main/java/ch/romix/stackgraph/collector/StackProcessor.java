package ch.romix.stackgraph.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackProcessor {
	private static Pattern THREAD_HEADER_REGEX_PATTERN = Pattern.compile("\\\"(.+)\\\"");
	private static Pattern STACK_REGEX_PATTERN = Pattern.compile("([a-zA-Z0-9_$<>]+\\(.+\\))");
	private static Pattern THREAD_STATE_REGEX_PATTERN = Pattern.compile("java.lang.Thread.State: ([A-Z]+)");

	private enum ProcessorState {
		THREAD, STATE, FIRST_STACK, STACK
	};

	private List<String> threadNames = new ArrayList<>();
	private List<StackThreadState> states = new ArrayList<>();
	private List<List<StackElement>> stacks = new ArrayList<>();

	private String currentThreadName;
	private StackThreadState currentThreadState;
	private List<StackElement> currentStack;

	private ProcessorState currentState = ProcessorState.THREAD;

	public void readLine(String line) {
		try {
			line = line.trim();
			switch (currentState) {
			case THREAD:
				if (isThreadLine(line)) {
					parseThreadHeader(line);
					currentState = ProcessorState.STATE;
				}
				break;
			case STATE:
				if (isStateLine(line)) {
					parseThreadState(line);
					currentState = ProcessorState.FIRST_STACK;
				} else {
					currentState = ProcessorState.THREAD;
				}
				break;
			case FIRST_STACK:
				if (isStackLine(line)) {
					addCurrentThread();
					parseThreadStack(line);
					currentState = ProcessorState.STACK;
					break;
				} else {
					currentState = ProcessorState.THREAD;
					break;
				}
			case STACK:
				if (isStackLine(line)) {
					parseThreadStack(line);
				} else if (!isLockingHintLine(line)) {
					currentState = ProcessorState.THREAD;
				}
				break;
			}
		} catch (Exception e) {
			throw new RuntimeException("Problem parseing line '" + line + "'.", e);
		}
	}

	private boolean isThreadLine(String line) {
		return line.startsWith("\"");
	}

	private boolean isStateLine(String line) {
		return line.startsWith("java.lang.Thread.State");
	}

	private boolean isStackLine(String line) {
		return line.startsWith("at ");
	}

	private boolean isLockingHintLine(String line) {
		return line.startsWith("- ");
	}

	private void parseThreadHeader(String line) {
		Matcher matcher = THREAD_HEADER_REGEX_PATTERN.matcher(line);
		matcher.find();
		currentThreadName = matcher.group(1);
	}

	private void parseThreadState(String line) {
		Matcher matcher = THREAD_STATE_REGEX_PATTERN.matcher(line);
		matcher.find();
		String state = matcher.group(1);
		currentThreadState = StackThreadState.valueOf(state);
	}

	private void parseThreadStack(String line) {
		StackElement se = new StackElement();
		Matcher matcher = STACK_REGEX_PATTERN.matcher(line);
		matcher.find();
		String methodSignature = matcher.group(1);
		String fullClass = line.substring(3, line.length() - methodSignature.length() - 1);
		int packageClassSeparationIndex = fullClass.lastIndexOf('.');
		String packageName = fullClass.substring(0, packageClassSeparationIndex);
		se.setPackageName(packageName);
		String className = fullClass.substring(packageClassSeparationIndex + 1);
		se.setClassName(className);
		se.setMethodSignature(methodSignature);
		currentStack.add(se);
	}

	private void addCurrentThread() {
		threadNames.add(currentThreadName);
		states.add(currentThreadState);
		currentStack = new ArrayList<>();
		stacks.add(currentStack);
	}

	public int getThreadCount() {
		return threadNames.size();
	}

	public String getThreadName(int i) {
		return threadNames.get(i);
	}

	public StackThreadState getState(int i) {
		return states.get(i);
	}

	public List<StackElement> getStack(int i) {
		return stacks.get(i);
	}
}
