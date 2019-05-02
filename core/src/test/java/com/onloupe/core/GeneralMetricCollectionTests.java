package com.onloupe.core;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.onloupe.agent.metrics.EventMetric;
import com.onloupe.agent.metrics.EventMetricDefinition;
import com.onloupe.agent.metrics.EventMetricValueDefinitionCollection;
import com.onloupe.core.logging.Log;
import com.onloupe.core.metrics.IMetricDefinition;
import com.onloupe.core.util.OutObject;
import com.onloupe.model.data.EventMetricValueTrend;

public class GeneralMetricCollectionTests {
	
	private static final String TEST_NAME = "testValue";
	private UUID TEST_UUID = UUID.randomUUID();
	
	private static EventMetricDefinition getTestMetricDefinition() {
		return (EventMetricDefinition)Log.getMetrics().get("GeneralMetricCollectionTests", "Gibraltar.Monitor.Test", "Manual");
	}

	private static EventMetric getTestMetric() {
		return getTestMetricDefinition().getMetrics().get((String)null);
	}

	/**
	 * Create an event metric for some specific tests we run that test looking for a
	 * metric.
	 */
	@BeforeEach
	public final void setup() {
		EventMetricDefinition newEventMetricDefinition;

		OutObject<IMetricDefinition> out = new OutObject<>();
		Log.getMetrics().tryGetValue("GeneralMetricCollectionTests", "Gibraltar.Monitor.Test", "Manual", out);

		EventMetricDefinition newMetricDefinition = (EventMetricDefinition)out.argValue;
		
		// See if we already created the event metric we need
		if (newMetricDefinition == null) {
			// Didn't find it, so define an event metric manually (the hard way)
			newEventMetricDefinition = EventMetricDefinition.builder("GeneralMetricCollectionTests",
					"Gibraltar.Monitor.Test", "Manual").build();
			newMetricDefinition = newEventMetricDefinition; // cast it as the base type, too

			// we now have a minimal definition, but we probably want to add a few
			// attributes to make it useful
			// NOTE: This is designed to exactly match UserDataObject for convenience in
			// analzing results.
			EventMetricValueDefinitionCollection valueDefinitions = newEventMetricDefinition.getValues();
			valueDefinitions.add("short_average", Short.class, "Short Average", "Data of type Short")
					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
			valueDefinitions.add("short_sum", Short.class, "Short Sum", "Data of type Short")
					.setDefaultTrend(EventMetricValueTrend.SUM);
			valueDefinitions.add("int_average", Integer.class, "Int Average", "Data of type Int")
					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
			valueDefinitions.add("int_sum", Integer.class, "Int Sum", "Data of type Int")
					.setDefaultTrend(EventMetricValueTrend.SUM);
			valueDefinitions.add("long_average", Long.class, "Long Average", "Data of type Long")
					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
			valueDefinitions.add("long_sum", Long.class, "Long Sum", "Data of type Long")
					.setDefaultTrend(EventMetricValueTrend.SUM);
			valueDefinitions.add("decimal_average", BigDecimal.class, "Decimal Average", "Data of type Decimal")
					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
			valueDefinitions.add("decimal_sum", BigDecimal.class, "Decimal Sum", "Data of type Decimal")
					.setDefaultTrend(EventMetricValueTrend.SUM);
			valueDefinitions.add("double_average", Double.class, "Double Average", "Data of type Double")
					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
			valueDefinitions.add("double_sum", Double.class, "Double Sum", "Data of type Double")
					.setDefaultTrend(EventMetricValueTrend.SUM);
			valueDefinitions.add("float_average", Float.class, "Float Average", "Data of type Float")
					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
			valueDefinitions.add("float_sum", Float.class, "Float Sum", "Data of type Float")
					.setDefaultTrend(EventMetricValueTrend.SUM);
			valueDefinitions.add("string", String.class, "String", "Data of type String");
			valueDefinitions.add("system.enum", Enum.class, "System.Enum", "Data of type System.Enum");

			newEventMetricDefinition.setDefaultValue(newEventMetricDefinition.getValues().get("int_average"));
			newEventMetricDefinition = newEventMetricDefinition.register(); // Register it with the collection.
		} else {
			// Found one, try to cast it to the expected EventMetricDefinition type (raise
			// exception if fails to match)
			newEventMetricDefinition = (EventMetricDefinition) newMetricDefinition;
		}

		String nullStr = null;
		EventMetric newMetric = newEventMetricDefinition.addOrGet(nullStr); // add the default metric.
		EventMetric newNamedMetric = newEventMetricDefinition.addOrGet(TEST_NAME);
		TEST_UUID = newNamedMetric.getId();

		Assertions.assertNotNull(newMetricDefinition, "Null. Expected non-null.");
		Assertions.assertNotNull(newEventMetricDefinition, "Null. Expected non-null.");
		Assertions.assertNotNull(newMetric, "Null. Expected non-null.");
		Assertions.assertNotNull(newNamedMetric, "Null. Expected non-null.");

	}

	/**
	 * Make sure we can look up definitions by the range of ways it's possible
	 */
	@Test
	public final void testMetricDefinitionObjectLookup() {
		IMetricDefinition lookupMetricDefinition = getTestMetricDefinition();

		// look it up by GUID
		Assertions.assertSame(lookupMetricDefinition, Log.getMetrics().get(lookupMetricDefinition.getId()),
				"Failed to find same object when looking by Id");
		Assertions.assertSame(lookupMetricDefinition, Log.getMetrics().get(lookupMetricDefinition.getName()),
				"Failed to find same object when looking by Name");
		Assertions.assertSame(lookupMetricDefinition,
				Log.getMetrics().get(lookupMetricDefinition.getMetricsSystem(),
						lookupMetricDefinition.getCategoryName(), lookupMetricDefinition.getCounterName()),
				"Failed to find same object when looking by Key Components");
	}

	/**
	 * Make sure we can look up metrics by the range of ways that's possible.
	 */
	@Test
	public final void testMetricObjectLookup() {
		EventMetricDefinition lookupMetricDefinition = getTestMetricDefinition();
		EventMetric lookupMetric = getTestMetric();

		// these are definitions, not metrics
//		Assertions.assertSame(lookupMetric, Log.getMetrics().get(lookupMetric.getId()),
//				"Failed to find metric in Log.Metrics Metric cache");
		
		Assertions.assertSame(lookupMetric, lookupMetricDefinition.getMetrics().get(lookupMetric.getId()),
				"Failed to find same object when looking by Id");
		Assertions.assertSame(lookupMetric, lookupMetricDefinition.getMetrics().get(lookupMetric.getInstanceName()),
				"Failed to find same object when looking by Instance Name");

		// no "name" field, did we whack it?
		//		Assertions.assertSame(lookupMetric, lookupMetricDefinition.getMetrics().get(lookupMetric.getName()),
//				"Failed to find same object when looking by Full Name");
	}

	@Test
	public final void testMetricDefinitionCollectionUnderrun() {
		Assertions.assertThrows(IndexOutOfBoundsException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Log.getMetrics().get(-1);
			}
		});
	}

	@Test
	public final void testMetricDefinitionCollectionOverrun() {
		Assertions.assertThrows(IndexOutOfBoundsException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Log.getMetrics().get(Log.getMetrics().size());
			}
		});
	}

	@Test
	public final void testMetricCollectionFindByStringFullName() {
		EventMetricDefinition testMetricDefinition = getTestMetricDefinition();
		EventMetric testMetric = testMetricDefinition.getMetrics().get(TEST_NAME);
		Assertions.assertNotNull(testMetric, "Null. Expected non-null.");
	}

	@Test
	public final void testMetricCollectionFindDefault() {
		EventMetricDefinition testMetricDefinition = getTestMetricDefinition();
		
		UUID nullId = null;
		EventMetric testMetric = testMetricDefinition.getMetrics().get(nullId);

		// I don't think a null id is possible
		//		Assertions.assertNotNull(testMetric, "Null. Expected non-null.");

		// an empty string should also be treated as null
		testMetric = testMetricDefinition.getMetrics().get("");
		Assertions.assertNotNull(testMetric, "Null. Expected non-null.");

		// a string variable with nothing but whitespace should be treated as null
		String instanceName = "   ";
		EventMetric trimTestMetric = testMetricDefinition.getMetrics().get(instanceName);
		Assertions.assertNotNull(trimTestMetric, "Null. Expected non-null.");
		Assertions.assertSame(testMetric, trimTestMetric); // we should have gotten the same object despite our
															// nefarious input
	}

	@Test
	public final void testMetricCollectionFindByGuidKey() {
		EventMetricDefinition testMetricDefinition = getTestMetricDefinition();
		EventMetric testMetric = testMetricDefinition.getMetrics().get(TEST_UUID);
		Assertions.assertNotNull(testMetric, "Null. Expected non-null.");
	}

	/**
	 * Verify that we can be lazy and have leading & trailing white space on key
	 * elements without it causing a problem.
	 */
	@Test
	public final void testMetricDefinitionStringKeyTrimming() {
		EventMetricDefinition lookupMetricDefinition = getTestMetricDefinition();

		// Now try to get it using each key element with extra white space.
		EventMetricDefinition testMetricDefinition = (EventMetricDefinition)Log.getMetrics()
				.get(String.format(Locale.ROOT, "  %1$s  ", lookupMetricDefinition.getName()));
		Assertions.assertNotNull(testMetricDefinition, "Null. Expected non-null.");

		testMetricDefinition = (EventMetricDefinition)Log.getMetrics().get(
				String.format(Locale.ROOT, "  %1$s  ", lookupMetricDefinition.getMetricsSystem()),
				String.format(Locale.ROOT, "  %1$s  ", lookupMetricDefinition.getCategoryName()),
				String.format(Locale.ROOT, "  %1$s  ", lookupMetricDefinition.getCounterName()));
		Assertions.assertNotNull(testMetricDefinition, "Null. Expected non-null.");
	}

	/**
	 * Verify that we can be lazy and have leading & trailing white space on key
	 * elements without it causing a problem.
	 */
	@Test
	public final void testMetricStringKeyTrimming() {
		EventMetricDefinition testMetricDefinition = (EventMetricDefinition)getTestMetricDefinition();
		EventMetric lookupMetric = EventMetric.addOrGet(testMetricDefinition,
				"MetricStringKeyTrimming"); // this test we already passed, so now we use it to do the rest of our
											// tests.

		// Now try to get it using each key element with extra white space.
		EventMetric testMetric = testMetricDefinition.getMetrics()
				.get(String.format(Locale.ROOT, "  %s  ", lookupMetric.getInstanceName()));
		Assertions.assertNotNull(testMetric, "Null. Expected non-null.");
		Assertions.assertSame(lookupMetric, testMetric);

		// no more name field on EventMetric
//		testMetric = testMetricDefinition.getMetrics()
//				.get(String.format(Locale.ROOT, "  %1$s  ", lookupMetric.getName()));
//		Assertions.assertNotNull(testMetric, "Null. Expected non-null.");
//		Assertions.assertSame(lookupMetric, testMetric);
	}

}