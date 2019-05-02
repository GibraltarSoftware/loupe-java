package com.onloupe.core;

// ToDo: Compile turned off, but left around in case we want to port these test cases to Agent.Test.
public class UserNonMetricDataObject extends UserDataObject implements IEventMetricTestInterface {
	private int _InstanceNum;

	public UserNonMetricDataObject(String instanceName, int instanceNum) {
		super(instanceName);
		this._InstanceNum = instanceNum;
	}

	/**
	 * Our numeric instance num (so that inheritors can use it for ther
	 * IEventMetricTestInterface implementation)
	 */
	@Override
	public int getInstanceNum() {
		return this._InstanceNum;
	}

	@Override
	public String getStringProperty() {
		return super.getString();
	}

	@Override
	public String stringMethod() {
		return "Method Version: " + super.getString();
	}

	@Override
	public int getIntProperty() {
		return super.getInt();
	}
}