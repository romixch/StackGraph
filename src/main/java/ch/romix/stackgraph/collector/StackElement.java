package ch.romix.stackgraph.collector;

public class StackElement {
	private String packageName;
	private String ClassName;
	private String methodSignature;

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return ClassName;
	}

	public void setClassName(String className) {
		ClassName = className;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}

	public String asString() {
		return packageName + '.' + ClassName + '.' + methodSignature;
	}

	@Override
	public String toString() {
		return asString();
	}
}
