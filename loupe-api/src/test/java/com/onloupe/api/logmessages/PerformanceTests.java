package com.onloupe.api.logmessages;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import com.onloupe.api.Loupe;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.model.log.LogMessageSeverity;

public class PerformanceTests
{
	private static final int DEFAULT_MESSAGES_PER_TEST = 10000;
	private static Duration _TextDumpBaseline;
	private static Duration _TextDumpByLineBaseline;
	private static int messagesPerTest;

	@BeforeAll
	public static final void setup(TestReporter reporter) throws IOException
	{
		messagesPerTest = DEFAULT_MESSAGES_PER_TEST;
		//calculate a baseline performance
		File tempFile = File.createTempFile("test-", ".txt");

	//Time a single file dump (file held open) scenario
		tempFile.delete();
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);
		OffsetDateTime messageEndTime, endTime;
		try (FileWriter logWriter = new FileWriter(tempFile))
		{
			for (int curMessage = 0; curMessage < messagesPerTest; curMessage++)
			{
				String caption = String.format("Test Message %1$s Caption", curMessage);
				String description = String.format("Test Message %1$s Description, with some content added to it's at least the size you'd expect a normal description to be of a message", curMessage);
				logWriter.write(caption + ": " + description);
			}
			messageEndTime = OffsetDateTime.now(ZoneOffset.UTC);

			//flush the buffer...
			logWriter.flush();

			//and store off our time
			endTime = OffsetDateTime.now(ZoneOffset.UTC);
		}

		_TextDumpBaseline = Duration.between(startTime, endTime);
		reporter.publishEntry(String.format(
				"Text Dump Baseline completed in %dms.  %d messages were written at an average duration of %dms per message.  The flush took %dms.",
				_TextDumpBaseline.toMillis(), messagesPerTest,
				(_TextDumpBaseline.toMillis()) / messagesPerTest,
				Duration.between(messageEndTime, endTime).toMillis()));

	//Time a file write per line scenario
		tempFile.delete();
		DumbFileLogger fileLogger = new DumbFileLogger(tempFile.getAbsolutePath());
		startTime = OffsetDateTime.now(ZoneOffset.UTC);
		for (int curMessage = 0; curMessage < messagesPerTest; curMessage++)
		{
			String caption = String.format("Test Message %1$s Caption", curMessage);
			String description = String.format("Test Message %1$s Description, with some content added to it's at least the size you'd expect a normal description to be of a message", curMessage);
			fileLogger.writeToLog(caption + ": " + description);
		}
		messageEndTime = OffsetDateTime.now(ZoneOffset.UTC);

		//and store off our time
		endTime = OffsetDateTime.now(ZoneOffset.UTC);

		_TextDumpByLineBaseline = Duration.between(startTime, endTime);
		reporter.publishEntry(String.format(
				"Text Dump By Line Baseline completed in %dms.  %d messages were written at an average duration of %dms per message.",
				_TextDumpByLineBaseline.toMillis(), messagesPerTest,
				(_TextDumpByLineBaseline.toMillis()) / messagesPerTest));

		//finally, don't leave that temp file around...
		tempFile.delete();
	}

	@Test
	public final void asyncPassThrough(TestReporter reporter)
	{
		//first, lets get everything to flush so we have our best initial state.
		Loupe.endFile("Preparing for Performance Test");
		Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.LogMessages.Performance", "Preparing for performance test", null);

		DummyMessageSourceProvider ourMessageSource = new DummyMessageSourceProvider("Gibraltar.Agent.Test.LogMessages.PerformanceTests", "DefaultConfiguration", null, 0);

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);
		for (int curMessage = 0; curMessage < messagesPerTest; curMessage++)
		{
			String caption = String.format("Test Message %1$s Caption", curMessage);
			String description = String.format("Test Message %1$s Description, with some content added to it's at least the size you'd expect a normal description to be of a message", curMessage);
			Loupe.write(LogMessageSeverity.VERBOSE, "NUnit", ourMessageSource, "UnitTest", null, LogWriteMode.QUEUED, null, "Test.Agent.LogMessages.Performance", caption, description);
		}
		OffsetDateTime messageEndTime = OffsetDateTime.now(ZoneOffset.UTC);

		//one wait for commit message to force the buffer to flush.
		Loupe.write(LogMessageSeverity.VERBOSE, "NUnit", ourMessageSource, "UnitTest", null, LogWriteMode.WAIT_FOR_COMMIT, null, "Test.Agent.LogMessages.Performance", "Committing performance test", null);

		//and store off our time
		OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);

		Duration duration = Duration.between(startTime, endTime);

		reporter.publishEntry(String.format(
				"Async WriteMessage Test Completed in %dms (%d%% of baseline).  %d messages were written at an average duration of %dms per message.  The flush took %dms.",
				duration.toMillis(), duration.toMillis() / _TextDumpByLineBaseline.toMillis(), 
				messagesPerTest, (duration.toMillis()) / messagesPerTest,
				Duration.between(messageEndTime, endTime).toMillis()));
	}

	@Test
	public final void synchronousPassThrough(TestReporter reporter)
	{
		//first, lets get everything to flush so we have our best initial state.
		Loupe.endFile("Preparing for Performance Test");
		Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.LogMessages.Performance", "Preparing for performance test", null);

		DummyMessageSourceProvider ourMessageSource = new DummyMessageSourceProvider("Gibraltar.Agent.Test.LogMessages.PerformanceTests", "DefaultConfiguration", null, 0);

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);

		for (int curMessage = 0; curMessage < messagesPerTest; curMessage++)
		{
			String caption = String.format("Test Message %1$s Caption", curMessage);
			String description = String.format("Test Message %1$s Description, with some content added to it's at least the size you'd expect a normal description to be of a message", curMessage);
			Loupe.write(LogMessageSeverity.VERBOSE, "NUnit", ourMessageSource, "UnitTest", null, LogWriteMode.WAIT_FOR_COMMIT, null, "Test.Agent.LogMessages.Performance", caption, description);
		}

		//one more message to match our async case.
		Loupe.write(LogMessageSeverity.VERBOSE, "NUnit", ourMessageSource, "UnitTest", null, LogWriteMode.WAIT_FOR_COMMIT, null, "Test.Agent.LogMessages.Performance", "Committing performance test", null);

		//and store off our time
		Duration duration = Duration.between(startTime, OffsetDateTime.now(ZoneOffset.UTC));

		reporter.publishEntry(String.format(
				"Sync WriteMessage Test Completed in %dms (%d%% of baseline).  %d messages were written at an average duration of %dms per message.",
				duration.toMillis(), duration.toMillis() / _TextDumpByLineBaseline.toMillis(), messagesPerTest, (duration.toMillis()) / messagesPerTest));
	}

	@Test
	public final void asyncMessage(TestReporter reporter)
	{
		//first, lets get everything to flush so we have our best initial state.
		Loupe.endFile("Preparing for Performance Test");
		Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.LogMessages.Performance", "Preparing for performance test", null);

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);
		for (int curMessage = 0; curMessage < messagesPerTest; curMessage++)
		{
			String caption = String.format("Test Message %1$s Caption", curMessage);
			String description = String.format("Test Message %1$s Description, with some content added to it's at least the size you'd expect a normal description to be of a message", curMessage);
			Loupe.verbose(LogWriteMode.QUEUED, "Test.Agent.LogMessages.Performance", caption, description);
		}
		OffsetDateTime messageEndTime = OffsetDateTime.now(ZoneOffset.UTC);

		//one wait for commit message to force the buffer to flush.
		Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.LogMessages.Performance", "Committing performance test", null);

		//and store off our time
		OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);

		Duration duration = Duration.between(startTime, endTime);

		reporter.publishEntry(String.format(
				"Async Write Test Completed in %dms (%d%% of baseline).  %d messages were written at an average duration of %dms per message.  The flush took %dms.",
				duration.toMillis(), duration.toMillis() / _TextDumpByLineBaseline.toMillis(), messagesPerTest, (duration.toMillis()) / messagesPerTest,
				Duration.between(messageEndTime, endTime).toMillis()));
	}

	@Test
	public final void syncMessage(TestReporter reporter)
	{
		//first, lets get everything to flush so we have our best initial state.
		Loupe.endFile("Preparing for Performance Test");
		Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.LogMessages.Performance", "Preparing for performance test", null);

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);
		for (int curMessage = 0; curMessage < messagesPerTest; curMessage++)
		{
			String caption = String.format("Test Message %1$s Caption", curMessage);
			String description = String.format("Test Message %1$s Description, with some content added to it's at least the size you'd expect a normal description to be of a message", curMessage);
			Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.LogMessages.Performance", caption, description);
		}

		Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.LogMessages.Performance", "Committing performance test", null);

		//and store off our time
		Duration duration = Duration.between(startTime, OffsetDateTime.now(ZoneOffset.UTC));

		reporter.publishEntry(String.format(
				"Sync Write Test Completed in %dms (%d%% of baseline).  %d messages were written at an average duration of %dms per message.",
				duration.toMillis(), duration.toMillis() / _TextDumpByLineBaseline.toMillis(), messagesPerTest, (duration.toMillis()) / messagesPerTest));
	}


	@Test
	public final void traceDirectCaptionDescription(TestReporter reporter)
	{
		//first, lets get everything to flush so we have our best initial state.
		Loupe.endFile("Preparing for Performance Test");
		Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.LogMessages.Performance", "Preparing for performance test", null);

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);

		for (int curMessage = 0; curMessage < messagesPerTest; curMessage++)
		{
			Loupe.traceInformation("Test Message %s Caption\r\nTest Message %s Description, with some content added to it's at least the size you'd expect a normal description to be of a message", curMessage);
		}

		//and store off our time
		Duration duration = Duration.between(startTime, OffsetDateTime.now(ZoneOffset.UTC));

		reporter.publishEntry(String.format(
				"Trace Direct Caption Description Test Completed in %dms (%d%% of baseline).  %d messages were written at an average duration of %dms per message.",
				duration.toMillis(), duration.toMillis() / _TextDumpByLineBaseline.toMillis(), messagesPerTest, (duration.toMillis()) / messagesPerTest));
	}

	@Test
	public final void traceDirectCaption(TestReporter reporter)
	{
		//first, lets get everything to flush so we have our best initial state.
		Loupe.endFile("Preparing for Performance Test");
		Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.LogMessages.Performance", "Preparing for performance test", null);

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);

		for (int curMessage = 0; curMessage < messagesPerTest; curMessage++)
		{
			Loupe.traceInformation("Test Message %s Caption and Description, with some content added to it's at least the size you'd expect a normal description to be of a message", curMessage);
		}

		//and store off our time
		Duration duration = Duration.between(startTime, OffsetDateTime.now(ZoneOffset.UTC));

		reporter.publishEntry(String.format(
				"Trace Direct Message Test Completed in %dms (%d%% of baseline).  %d messages were written at an average duration of %dms per message.",
				duration.toMillis(), duration.toMillis() / _TextDumpByLineBaseline.toMillis(), messagesPerTest, (duration.toMillis()) / messagesPerTest));
	}
}