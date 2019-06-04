package com.onloupe.agent.metrics;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.metrics.IMetricDefinition;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.log.LogMessageSeverity;

// TODO: Auto-generated Javadoc
/**
 * Record an event metric for a single execution of a data operation
 * 
 * This class is optimized to be used in a using statement. It will
 * automatically time the duration of the command and record an event metric
 * when disposed. It will also record trace messages for the start and end of
 * each command so that it is unnecessary to add redundant trace messages in
 * your method invocation to denote the boundaries of a command. If not
 * explicitly Dispose'd (automatically done for you by a using statement) the
 * metric will not be generated.
 */
public class OperationMetric implements Closeable {
	// constants we use in place of what was previously on the attribute for the
	// class
	/** The metric type name. */
	public static final String METRIC_TYPE_NAME = "Gibraltar Software";

	/** The metric counter name. */
	public static final String METRIC_COUNTER_NAME = "Operation";

	/** The metric counter description. */
	public static final String METRIC_DEFINITION_DESCRIPTION = "Information about each time a data operation is performed";

	/** The init time. */
	private LocalDateTime initTime;
	
	/** The duration. */
	private Duration duration = Duration.ZERO;
	
	/** The category. */
	private String category;
	
	/** The operation name. */
	private String operationName;
	
	/** The end message. */
	private String endMessage;
	
	/** The args. */
	private Object[] args;
	
	/** The closed. */
	private boolean closed;

	/**
	 * Create a new operation metric monitoring object to record a single operation.
	 * 
	 * All event metrics are recorded under the same metric counter in
	 * Gibraltar.Data called Repository Operation.
	 * 
	 * @param category      The category to use for the metric
	 * @param operationName The name of the operation for tracking purposes
	 */
	public OperationMetric(String category, String operationName) {
		initialize(category, operationName, null, null, new Object[] {});
	}

	/**
	 * Create a new operation metric monitoring object to record a single operation.
	 * 
	 * All event metrics are recorded under the same metric counter in
	 * Gibraltar.Data called Repository Operation.
	 * 
	 * @param category      The category to use for the metric
	 * @param operationName The name of the operation for tracking purposes
	 * @param startMessage  A trace message to add at the start of the operation.
	 */
	public OperationMetric(String category, String operationName, String startMessage) {
		initialize(category, operationName, startMessage, null, new Object[] {});
	}

	/**
	 * Create a new operation metric monitoring object to record a single operation.
	 * 
	 * All event metrics are recorded under the same metric counter in
	 * Gibraltar.Data called Repository Operation.
	 * 
	 * @param category      The category to use for the metric
	 * @param operationName The name of the operation for tracking purposes
	 * @param startMessage  A trace message to add at the start of the operation.
	 * @param endMessage    A trace message to add at the end of the operation.
	 */
	public OperationMetric(String category, String operationName, String startMessage, String endMessage) {
		initialize(category, operationName, startMessage, endMessage, new Object[] {});
	}

	/**
	 * Create a new operation metric monitoring object to record a single operation.
	 * 
	 * All event metrics are recorded under the same metric counter in
	 * Gibraltar.Data called Repository Operation.
	 * 
	 * @param category      The category to use for the metric
	 * @param operationName The name of the operation for tracking purposes
	 * @param startMessage  A trace message to add at the start of the operation.
	 *                      Any args provided will be inserted.
	 * @param endMessage    A trace message to add at the end of the operation. Any
	 *                      args provided will be inserted.
	 * @param args          A variable number of arguments to insert into the start
	 *                      and end messages
	 */
	public OperationMetric(String category, String operationName, String startMessage, String endMessage,
			Object... args) {
		initialize(category, operationName, startMessage, endMessage, args);
	}

	/**
	 * The operation that was executed.
	 *
	 * @return the operation name
	 */
	public final String getOperationName() {
		return this.operationName;
	}

	/**
	 * Performs application-defined tasks associated with freeing, releasing, or
	 * resetting unmanaged resources. Calling Dispose() (automatic when a using
	 * statement ends) will generate the metric.
	 * 
	 * 
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final void close() throws IOException {
		if (!this.closed) {
			// Free managed resources here (normal Dispose() stuff, which should itself call
			// Dispose(true))
			// Other objects may be referenced in this case

			// We'll get here when the using statement ends, so generate the metric now...

			// users do NOT expect exceptions when recording event metrics like this
			try {
				stopAndRecordMetric();

				// Use skipFrames = 2 to attribute the EndMessage to whoever called Dispose()
				// (which then called us).

				// careful, built-in stuff doesn't like nulls for args
				if (this.args == null) {
					Log.writeMessage(LogMessageSeverity.VERBOSE, LogWriteMode.QUEUED, 2, null, null, null,
							this.endMessage);
				} else {
					Log.writeMessage(LogMessageSeverity.VERBOSE, LogWriteMode.QUEUED, 2, null, null, null,
							this.endMessage, this.args);
				}
			} catch (java.lang.Exception e) {

			}
			// Free native resources here (alloc's, etc)
			// May be called from within the finalizer, so don't reference other objects
			// here

			this.closed = true; // Make sure we only do this once
		}
	}

	/**
	 * Our real constructor logic
	 * 
	 * This is in its own special method so that the number of stack frames from the
	 * caller to this method is constant regardless of constructor.
	 * 
	 * @param category      The category to use for the metric
	 * @param operationName The name of the command for tracking purposes
	 * @param startMessage  A trace message to add at the start of the command. Any
	 *                      args provided will be inserted.
	 * @param endMessage    A trace message to add at the end of the command. Any
	 *                      args provided will be inserted.
	 * @param args          A variable number of arguments to insert into the start
	 *                      and end messages
	 */
	private void initialize(String category, String operationName, String startMessage, String endMessage,
			Object... args) {
		// we start when we get called
		this.initTime = LocalDateTime.now();

		// and record off our input
		this.category = TypeUtils.isBlank(category) ? "Gibraltar.Data" : category;
		this.operationName = TypeUtils.isBlank(operationName) ? "Operation" : operationName;
		this.args = args;

		// users do NOT expect exceptions when recording metrics like this.
		try {
			// Use skipFrames = 2 to attribute the start message to whoever called the
			// constructor (which then called us).

			// behave sanely if either message argument is missing
			if (TypeUtils.isBlank(startMessage)) {
				// because it will know where we were when we started, and we want these
				// messages to be easily filterable,
				// use a static string.
				Log.writeMessage(LogMessageSeverity.VERBOSE, LogWriteMode.QUEUED, 2, null, null, null, "%s started.",
						operationName);
			} else {
				// careful, built-in stuff doesn't like nulls for args
				if (args == null || args.length < 1) {
					Log.writeMessage(LogMessageSeverity.VERBOSE, LogWriteMode.QUEUED, 2, null, null, null,
							startMessage);
				} else {
					Log.writeMessage(LogMessageSeverity.VERBOSE, LogWriteMode.QUEUED, 2, null, null, null, startMessage,
							args);
				}
			}

			if (TypeUtils.isBlank(endMessage)) {
				// because it will know where we were when we completed, and we want these
				// messages to be easily filterable,
				// use a static string.
				this.endMessage = String.format("%1$s completed.", operationName);
			} else {
				this.endMessage = endMessage;
			}
		} catch (java.lang.Exception e) {

		}
	}

	/**
	 * Stop and record metric.
	 */
	private void stopAndRecordMetric() {
		// record our end time
		if (this.initTime != null) {
			this.duration = Duration.between(initTime, LocalDateTime.now());
		}

		// Get the METRIC DEFINITION
		OutObject<IMetricDefinition> value = new OutObject<IMetricDefinition>();
		EventMetricDefinition eventDefinition;
		if (!Log.getMetrics().tryGetValue(METRIC_TYPE_NAME, this.category,
				METRIC_COUNTER_NAME, value)) {
			// it doesn't exist yet - add it
			eventDefinition = EventMetricDefinition.builder(METRIC_TYPE_NAME, this.category, METRIC_COUNTER_NAME).build();
			eventDefinition.setDescription(METRIC_DEFINITION_DESCRIPTION);

			EventMetricValueDefinitionCollection valueDefinitionCollection = eventDefinition.getValues();
			valueDefinitionCollection.add("operationname", String.class, "Operation Name",
					"The operation that was executed.");

			valueDefinitionCollection.add("duration", Duration.class, "Duration",
					"The duration the operation executed.");
			eventDefinition.getValues().get("duration").setUnitCaption("Milliseconds");
			eventDefinition.setDefaultValue(eventDefinition.getValues().get("duration"));

			// and don't forget to register it!
			eventDefinition = eventDefinition.register();
		} else {
			eventDefinition = (EventMetricDefinition)value.argValue;
		}

		// Get the METRIC
		EventMetric eventMetric;
		String key = null;
		OutObject<EventMetric> temp = new OutObject<EventMetric>();
		if (!eventDefinition.getMetrics().tryGetValue(key, temp)) {
			eventMetric = new EventMetric(eventDefinition, (String) null);
		} else {
			eventMetric = (EventMetric)temp.argValue;
		}

		// and finally we can RECORD THE SAMPLE.
		EventMetricSample metricSample = eventMetric.createSample();
		metricSample.setValue("operationname", getOperationName());
		metricSample.setValue("duration", duration);
		metricSample.write();
	}
}