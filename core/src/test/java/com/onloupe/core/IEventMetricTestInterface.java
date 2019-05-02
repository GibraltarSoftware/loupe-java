package com.onloupe.core;

/**
 * This is an interface that is used for testing different objects with
 * reflection event monitoring
 */
// ToDo: Compile turned off, but left around in case we want to port these test cases to Agent.Test.
//[EventMetric("EventMetricTests", "Gibraltar.Monitor.Test", "IEventMetricTestInterface", "Interface for simple event metrics, designed to allow testing of implementations using just interfaces")]
public interface IEventMetricTestInterface {
	// [EventMetricInstanceName]
	int getInstanceNum();

	// [EventMetricValue("stringproperty", "String Property", "This is a property
	// that returns a string")]
	String getStringProperty();

	// [EventMetricValue("stringmethod", "String Method", "This is a method that
	// returns a string")]
	String stringMethod();

	// [EventMetricValue("intproperty", "Integer Property", "This is a property that
	// returns an integer", IsDefaultValue = true)]
	int getIntProperty();
}