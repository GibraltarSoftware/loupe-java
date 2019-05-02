package com.onloupe.api;

import com.onloupe.api.Loupe;
import com.onloupe.configuration.AgentConfiguration;
import com.onloupe.configuration.PublisherConfiguration;
import com.onloupe.core.util.SystemUtils;
import com.onloupe.core.util.TimeConversion;
import com.onloupe.model.system.Version;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestReporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class SetUpTests
{
	private static boolean haveInitialized = false;

	@BeforeAll
	public static final void runBeforeAnyTests(TestReporter reporter) throws IOException
	{
		if (haveInitialized)
			return;

		//delete the existing local logs folder for us...
		try
		{
			Path path = Paths.get(System.getenv(SystemUtils.isWindows() ? "ProgramData" : "Home")).resolve("Gibraltar\\Local Logs\\NUnit");
			Files.deleteIfExists(path);
		}
		catch (IOException ex)
		{
			reporter.publishEntry("Unable to clean out local logs directory due to " + ex.getClass());
		}

		AgentConfiguration _Configuration = new AgentConfiguration();
		PublisherConfiguration publisher = _Configuration.getPublisher();
		publisher.setProductName("JUnit");
		publisher.setApplicationName("Loupe.Agent.Test");

		publisher.setApplicationVersion(new Version(1, 0));
		
		publisher.setApplicationDescription("JUnit tests of the Loupe Agent Library");

		_Configuration.getSessionFile().setEnableFilePruning(false);

		//force us to initialize logging
		Loupe.start(_Configuration);
		haveInitialized = true;
		reporter.publishEntry(String.format("Starting testing at %s on computer %s",
				OffsetDateTime.now(ZoneOffset.UTC).format(TimeConversion.CS_DATETIMEOFFSET_FORMAT),
				Loupe.getSessionSummary().getHostName()));
	}

	@AfterAll
	public static final void runAfterAllTests() throws IOException
	{
		if (!haveInitialized)
			return;

		//KM: Since we are called once *per test class* we can't shutdown.  We'll have to comfort
		//ourselves by just flushing data.
		Loupe.endFile("Closing out test fixture");
		//Tell our central log session we're shutting down nicely
		//AgentLog.shutdown();
	}
}