package ch.romix.stackgraph;

import java.lang.management.ManagementFactory;

import ch.romix.stackgraph.collector.StackElement;

import com.Ostermiller.util.CircularByteBuffer;

public class Helpers {
	public static int getMyProcessId() {
		final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		final int indexOfAt = jvmName.indexOf('@');
		return Integer.parseInt(jvmName.substring(0, indexOfAt));
	}

	public static void writeToInputStreamAfterSeconds(final CircularByteBuffer buffer, final int secondsToWait) {
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * secondsToWait);
					buffer.getOutputStream().write(System.getProperty("line.separator").getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public static StackElement createStackElement(String packageName, String className, String methodSignature) {
		StackElement stackElement = new StackElement();
		stackElement.setPackageName(packageName);
		stackElement.setClassName(className);
		stackElement.setMethodSignature(methodSignature);
		return stackElement;
	}
}
