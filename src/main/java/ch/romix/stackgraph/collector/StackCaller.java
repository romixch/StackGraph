package ch.romix.stackgraph.collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StackCaller {

	public BufferedReader callJStackForPID(int pid) {
		String jstackCommand = getJStackCommand(pid);
		Process process;
		try {
			process = Runtime.getRuntime().exec(jstackCommand);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			process.waitFor();
			return reader;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private String getJStackCommand(int pid) {
		String command = System.getProperty("java.home");
		command = command + "/../bin/jstack";
		command = command + ' ' + pid;
		return command;
	}
}
