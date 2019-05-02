package com.onloupe.core;

// ToDo: Compile turned off, but left around in case we want to port these test cases to Agent.Test.
//[EventMetric("EventMetricTests", "Gibraltar.Monitor.Test", "UserSecondaryMetricDataObject")]
public class UserSecondaryMetricDataObject extends UserNonMetricDataObject implements IEventMetricTestInterface {
	public UserSecondaryMetricDataObject(String instanceName, int instanceNum) {
		super(instanceName, instanceNum);
	}

	@Override
	public final String getStringProperty() {
		return "implicit: " + getString();
	}

	@Override
	public final String stringMethod() {
		return "Implicit Method Version: " + getString();
	}

	@Override
	public final int getIntProperty() {
		return getInt();
	}
}