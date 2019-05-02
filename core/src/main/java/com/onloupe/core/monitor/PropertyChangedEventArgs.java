package com.onloupe.core.monitor;

public class PropertyChangedEventArgs {

	private String propertyName;

	public PropertyChangedEventArgs(String propertyName) {
		super();
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return this.propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

}
