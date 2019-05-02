package com.onloupe.core;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.onloupe.agent.IMessageSourceProvider;
import com.onloupe.agent.logging.MessageSourceProvider;
import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.messaging.IMessengerPacket;
import com.onloupe.core.monitor.ApplicationUser;
import com.onloupe.core.monitor.ResolveUserEventArgs;
import com.onloupe.model.exception.GibraltarException;
import com.onloupe.model.log.LogMessageSeverity;

/**
 * Test the Log class's logging capabilities. Metrics are not verified in this
 * class
 */
public class LogTests {
	/**
	 * Write a log message using each different trace log statement on the Log
	 * object
	 */
	@Test
	public final void testWriteTrace() {
		Log.trace("This is a call to Log.Trace with no arguments");
		Log.trace("This is a call to Log.Trace with two arguments #1:%s, #2:%s", 1, 2);

		Log.write(LogMessageSeverity.INFORMATION, "Unit Tests", null, "This is a call to Log.Write with no arguments");
		Log.write(LogMessageSeverity.INFORMATION, "Unit Tests", null,
				"This is a call to Log.Write with two arguments #1:%s, #2:%s", 1, 2);

		RuntimeException exception = new GibraltarException("This is a dummy exception to test API calls.");

		Log.trace(exception, "This is a call to Log.Trace with an exception and no arguments");
		Log.trace(exception, "This is a call to Log.Trace with an exception and two arguments #1:%s, #2:%s", 1, 2);

		Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, exception, "Unit Tests", null,
				"This is a call to Log.Write with an exception and no arguments");
		Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, exception, "Unit Tests", null,
				"This is a call to Log.Write with an exception and two arguments #1:%s, #2:%s", 1, 2);

		Log.write(LogMessageSeverity.VERBOSE, "Unit Tests",
				"This is a call to Log.Write with a caption and null description", null);
		Log.write(LogMessageSeverity.VERBOSE, "Unit Tests",
				"This is a call to Log.Write with a caption and description", "with no formatting arguments");
		Log.write(LogMessageSeverity.VERBOSE, "Unit Tests",
				"This is a call to Log.Write with a caption and description",
				"formatted with two arguments #1:%s, #2:%s", 1, 2);

		Log.write(LogMessageSeverity.INFORMATION, LogWriteMode.WAIT_FOR_COMMIT, null, "Unit Tests",
				"This is a call to Log.Write with WaitForCommit and null exception and with a caption and null description",
				null);
	}

	/**
	 * Write a log message with an attached exception object.
	 */
	@Test
	public final void testWriteException() {
		Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED,
				new RuntimeException("This is our test assertion exception"), "Unit Tests",
				"Test of logging exception attachment.", null);

		Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED,
				new RuntimeException("This is our top exception",
						new RuntimeException("This is our middle exception",
								new RuntimeException("This is our bottom exception"))),
				"Unit Tests", "Test of logging exception attachment with nested exceptions.", null);
	}

	/**
	 * Write many messages so we can verify order in the viewer. The goal is to
	 * write many messages so that the order has to be controlled by sequence
	 * number, not timestamp.
	 */
	@Test
	public final void testWriteMessagesForOrderTesting() {
		for (int curLogMessage = 1; curLogMessage < 3000; curLogMessage++) {
			Log.trace("This is log message #%s", curLogMessage);
		}
	}

	/**
	 * Write a log message using the full trace message entrance point
	 */
	@Test
	public final void testWriteTraceFullFormat() {
		// do one that should be pinned on US
		Log.writeMessage(LogMessageSeverity.VERBOSE, LogWriteMode.QUEUED, 0, null, null,
				"This message should be verbose and ascribed to the LogTests class.", null);
		Log.writeMessage(LogMessageSeverity.CRITICAL, LogWriteMode.QUEUED, 1, null, null,
				"This message should be critical and ascribed to whatever is calling our test class.", null);
		Log.writeMessage(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, -1, null, null,
				"This message should be error and also ascribed to the LogTests class.", null);
	}

	/**
	 * Test our handling of errors in formatted log message calls.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public final void testWriteBadFormat() throws InterruptedException {
		String[] messageArray = new String[5];
		String fullMessage = null;

		// We aren't checking the actual returns, just making sure these don't result in
		// exceptions.
		// ToDo: Add logic to check that the result contains each item of data?
		fullMessage = CommonCentralLogic.safeFormat(null, "This is a test\tof a bad format call\n%s, %s, {2}", "zero",
				1);
		messageArray[0] = fullMessage;
		fullMessage = CommonCentralLogic.safeFormat(null, "This is a test\r\nof a legal format call\t%s,\t%s,\t{2}",
				0, null, "two");
		messageArray[1] = fullMessage;
		fullMessage = CommonCentralLogic.safeFormat(null,
				"This is a test\n\rof a bad format call\t%s,\t%s,\n{2}\t{3}", null, "one", "\"two\"");
		messageArray[2] = fullMessage;
		fullMessage = CommonCentralLogic.safeFormat(null, null, 0, "\"null format test\"", 2);
		messageArray[3] = fullMessage;
		fullMessage = CommonCentralLogic.safeFormat(null, "", "empty\rformat\ntest", 1);
		messageArray[4] = fullMessage;

		Log.trace("This is a test\tof a bad format call to Log.Trace()\n%s, %s, {2}", "zero", 1);
		Log.trace("This is a test\r\nof a legal format call to Log.Trace()\t%s,\t%s,\t{2}", 0, null, "two");
		Log.trace("This is a test\n\rof a bad format call to Log.Trace()\t%s,\t%s,\n{2}\t{3}", null, "one",
				"\"two\"");
		Log.trace((String) null, 0, "null format test", 2);
		Log.trace("", "empty format test", 1);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			throw e;
		} // Give it time to put stuff in the log.

		return;
	}

	/**
	 * Test our performance flushing a large message queue.
	 */
	@Test
	public final void testMessageQueueFlushTest() {
		final int count = 10000;
		IMessageSourceProvider source = new MessageSourceProvider();
		IMessengerPacket[] batch = new IMessengerPacket[count];
		for (int i = 0; i < count; i++) {
			batch[i] =Log.makeLogPacket(LogMessageSeverity.VERBOSE, "GibraltarTest",
					"Test.Core.LogMessage.Performance.Flush", source, null, null, null, null, null, "Batch message #%s of %s", i, count);
		}

		Log.write(LogMessageSeverity.INFORMATION, LogWriteMode.WAIT_FOR_COMMIT, null, "Unit Tests",
				"Clearing message queue", null);

		OffsetDateTime startWrite = OffsetDateTime.now();

		Log.write(batch, LogWriteMode.QUEUED);

		OffsetDateTime startFlush = OffsetDateTime.now();

		Log.write(LogMessageSeverity.INFORMATION, LogWriteMode.WAIT_FOR_COMMIT, null, "Unit Tests",
				"Message batch flushed", "All %s messages have been flushed.", count);

		OffsetDateTime endFlush = OffsetDateTime.now();

		Duration writeDuration = Duration.between(startWrite, startFlush);
		Duration flushDuration = Duration.between(startFlush, endFlush);

		Log.write(LogMessageSeverity.VERBOSE, "", "Unit test messageQueueFlushTest()",
				"Write of {0:N0} messages to queue took {1:F3} ms and flush took {2:F3} ms.", count,
				writeDuration.toMillis(), flushDuration.toMillis());

		for (int i = 0; i < count; i++) {
			batch[i] = Log.makeLogPacket(LogMessageSeverity.VERBOSE, "GibraltarTest",
					"Test.Core.LogMessage.Performance.Flush", source, null, null, null, null, null,
					"Batch message #%s of %s", i, count);
		}

		OffsetDateTime startWriteThrough = OffsetDateTime.now();

		Log.write(batch, LogWriteMode.WAIT_FOR_COMMIT);

		OffsetDateTime endWriteThrough = OffsetDateTime.now();

		Duration writeThroughDuration = Duration.between(startWriteThrough, endWriteThrough);

		Log.write(LogMessageSeverity.VERBOSE, "", "Unit test messageQueueFlushTest()",
				"Write of {0:N0} messages as WaitForCommit took {1:F3} ms.", count, writeThroughDuration.toMillis());

	}

	private void onResolveUserJustOnce(Object sender, ResolveUserEventArgs e) {
		this._UserResolutionRequests++;

		// all I have to do to lock in the user is get it..
		ApplicationUser newUser = e.getUser();
	}

	private int _UserResolutionRequests;
}