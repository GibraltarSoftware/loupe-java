package com.onloupe.api.packaging;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.function.Executable;

import com.onloupe.agent.Packager;
import com.onloupe.agent.SessionCriteria;
import com.onloupe.agent.SessionSummary;
import com.onloupe.api.Loupe;
import com.onloupe.core.data.SessionHeader;
import com.onloupe.core.logging.Log;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.util.FileUtils;
import com.onloupe.model.exception.DirectoryNotFoundException;
import com.onloupe.model.exception.UnauthorizedAccessException;
import com.onloupe.model.session.ISessionSummary;

@TestInstance(Lifecycle.PER_CLASS)
public class PackagerTests
{
	private static final String SERVER_OVERRIDE_CUSTOMER_NAME = "ConfigurationTest";
	private static final String SERVER_OVERRIDE_SERVER_NAME = "us.onloupe.com";
	private static final String SERVER_OVERRIDE_BASE_DIRECTORY = "";

	private static final String SERVER_OVERRIDE_REPOSITORY = "GibraltarSoftware";

	private Path outputFilePath;

	@BeforeAll
	public final void init() throws IOException
	{
		if (outputFilePath != null)
		{
			//we're re-initing:  delete any existing temp file.
			FileUtils.safeDeleteFile(outputFilePath.toFile());
		}

		outputFilePath = File.createTempFile("test-", "." + Log.PACKAGE_EXTENSION).toPath();

		//we have to smack on a GLP or the extension will get replaced.
		FileUtils.safeDeleteFile(outputFilePath.toFile()); //get temp file name creates the file as part of allocating the name.

		Loupe.endFile("Rolling over file to be sure we have something to package");
	}

	@BeforeEach
	public final void setup()
	{
		try {
			Files.delete(outputFilePath);
		} catch (IOException e) {
			// do nothing
		}
	}

	@Test
	public final void testDateTimeFormat(TestReporter reporter)
	{
		reporter.publishEntry(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX")));
	}
	
	@Test
	public final void createEmptyPackage() throws IOException
	{
		FileUtils.safeDeleteFile(outputFilePath.toFile());
		
		Packager newPackager = new Packager();
		newPackager.sendToFile(SessionCriteria.NONE, false, outputFilePath.toString());

		//because this is the none package it won't have any content, but won't thrown an error.
		Assertions.assertFalse(Files.isRegularFile(outputFilePath));
		FileUtils.safeDeleteFile(outputFilePath.toFile());
	}

	@Test
	public final void createNonePackage() throws IOException
	{
		FileUtils.safeDeleteFile(outputFilePath.toFile());

		Packager newPackager = new Packager();
		newPackager.sendToFile(SessionCriteria.NONE, false, outputFilePath.toString());

		//because this is the none package it won't have any content, but won't thrown an error.
		Assertions.assertFalse(Files.isRegularFile(outputFilePath));
		FileUtils.safeDeleteFile(outputFilePath.toFile());
	}

	@Test
	public final void createNewSessionsPackage() throws IOException
	{
		Packager newPackager = new Packager();
		newPackager.sendToFile(SessionCriteria.NEW, false, outputFilePath.toString());

		//we don't do the assert on this one because there may be no new sessions package.
		FileUtils.safeDeleteFile(outputFilePath.toFile());
	}

	@Test
	public final void createAllSessionsPackage() throws IOException
	{
		Loupe.start("Just making sure we're up and logging for this test.");

		Packager newPackager = new Packager();
		newPackager.sendToFile(SessionCriteria.ALL_SESSIONS, false, outputFilePath.toString());

		Assertions.assertTrue(Files.isRegularFile(outputFilePath), "There was no output file from the package, most likely because there is no local data to package (like when unit tests are run the first time)");
	}

	@SuppressWarnings("unused")
	@Test
	public final void testFileHandleRollover() throws IOException
	{
		//we need to guarantee that there is something being logged for this test to work.
		Loupe.start("Just making sure we're up and logging for this test.");
		UUID id = Log.getSessionSummary().getId();
		Log.endFile("It is a test");
		
		UUID equals = null;
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Log.getRepository().getRepositoryPath(),
				"*." + Log.LOG_EXTENSION)) {
			// even though we just pre-checked it may have been deleted between there and
			// here.
			for (Path path : dirStream) {
				File file = path.toFile();
				if (file.isFile()) {
					SessionHeader sessionHeader = LocalRepository.loadSessionHeader(file.getPath());
					UUID sid = sessionHeader != null ? sessionHeader.getId() : null;
					//UUID id = Log.getSessionSummary().getId();
					if (id.equals(sid))
						equals = sid;
				}
			}
		} catch (DirectoryNotFoundException e) {
			return;
		} catch (UnauthorizedAccessException e2) // if we are in-flight deleting the directory we'll get this.
		{
			return;
		} catch (IOException e1) {
			return;
		}
		
		Assertions.assertNotNull(equals);
	}
	
	@Test
	public final void createACTIVEPackage() throws IOException
	{
		//we need to guarantee that there is something being logged for this test to work.
		Loupe.start("Just making sure we're up and logging for this test.");

		Packager newPackager = new Packager();
		newPackager.sendToFile(SessionCriteria.ACTIVE, false, outputFilePath.toString());

		Assertions.assertTrue(Files.isRegularFile(outputFilePath), "There was no output file from the package, most likely because there is no local data to package (like when unit tests are run the first time)");
	}

	@Test
	public final void createCompletedSessionPackage() throws IOException
	{
		Packager newPackager = new Packager();
		newPackager.sendToFile(SessionCriteria.COMPLETED, false, outputFilePath.toString());

		//we don't do the assert on this one because there may be no completed sessions package.
	}

	@Test
	public final void createCriticalSessionPackage() throws IOException
	{
		Packager newPackager = new Packager();
		newPackager.sendToFile(SessionCriteria.CRITICAL, false, outputFilePath.toString());

		//we don't really know we have one of these, so no assertion.
		FileUtils.safeDeleteFile(outputFilePath.toFile());
	}

	@Test
	public final void createErrorSessionPackage() throws IOException
	{
		Packager newPackager = new Packager();
		newPackager.sendToFile(SessionCriteria.ERROR, false, outputFilePath.toString());

		//we don't do the assert on this one because there may be no error sessions package.
	}

	@Test
	public final void createWarningSessionPackage() throws IOException
	{
		Packager newPackager = new Packager();
		newPackager.sendToFile(SessionCriteria.WARNING, false, outputFilePath.toString());

		//we don't do the assert on this one because there may be no warning sessions package.
	}

	@Test
	public final void createCombinationSessionPackage() throws IOException
	{
		Packager newPackager = new Packager();
		newPackager.sendToFile(EnumSet.of(SessionCriteria.WARNING, SessionCriteria.NEW, SessionCriteria.NONE), false, outputFilePath.toString());

		//we don't do the assert on this one because there may be no sessions package.
	}

	@Test
	public final void createPredicateLambdaPackage() throws IOException
	{
		Packager newPackager = new Packager();
		newPackager.sendToFile(new Predicate<ISessionSummary>() {
			@Override
			public boolean test(ISessionSummary t) {
				try {
					return t.getHostName().equalsIgnoreCase(Log.getSessionSummary().getHostName());
				} catch (IOException e) {
					Assertions.fail(e);
				}
				return false;
			}
		}, false, outputFilePath.toString());

		Assertions.assertTrue(Files.isRegularFile(outputFilePath), "There was no output file from the package, most likely because there is no local data to package (like when unit tests are run the first time)");
	}

	@Test
	public final void createPredicateNamedMethodPackage() throws IOException
	{
		Packager newPackager = new Packager();
		_PredicateMatches = 0;
		newPackager.sendToFile(new Predicate<ISessionSummary>() {
			@Override
			public boolean test(ISessionSummary t) {
				try {
					Assertions.assertNotNull(t);

					boolean match = (t.getApplication().equals(Log.getSessionSummary().getApplication())
							&& (t.getProduct().equals(Log.getSessionSummary().getProduct())));

					if (match)
						_PredicateMatches++;

					return match;
				} catch (IOException e) {
					Assertions.fail(e);
				}
				return false;
			}
		}, false, outputFilePath.toString());

		Assertions.assertTrue(Files.isRegularFile(outputFilePath), "There was no output file from the package, most likely because there is no local data to package (like when unit tests are run the first time)");
		assert _PredicateMatches > 0;
	}

	private int _PredicateMatches;

	private boolean onPackagerNamedMethodPredicate(SessionSummary session) throws IOException
	{
		assert session != null;

		boolean match = (session.getApplication().equals(Log.getSessionSummary().getApplication())) 
				&& (session.getProduct().equals(Log.getSessionSummary().getProduct()));

		if (match)
		{
			_PredicateMatches++;
		}

		return match;
	}

	@Test
	public final void createPackageFromAlternateDirectory() throws IOException
	{
		//find our normal log directory..
		//Path loggingPath = Paths.get(SystemUtils.IS_OS_WINDOWS ? "ProgramData" : "Home").resolve("Gibraltar\\Local Logs\\Loupe");

		Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("PackagerTests");
		Path loggingPath = Paths.get("C:\\ProgramData").resolve("Gibraltar\\Local Logs\\Loupe");
		
		Files.createDirectories(tempDir);
		Files.createDirectories(loggingPath);

		try
		{
			File[] logFiles = loggingPath.toFile().listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					// TODO Auto-generated method stub
					return pathname.getAbsolutePath().endsWith(".glf");
				}
			});
			
			for (int curFileIndex = 0; curFileIndex < Math.min(10, logFiles.length); curFileIndex++)
			{
				File logFile = logFiles[curFileIndex];
				Files.copy(logFile.toPath(), tempDir.resolve(logFile.getName()), StandardCopyOption.COPY_ATTRIBUTES);
			}

			Packager newPackager = new Packager("Loupe", null, loggingPath.toString());
			newPackager.sendToFile(SessionCriteria.ALL_SESSIONS, false, outputFilePath.toString());

			Assertions.assertTrue(Files.isRegularFile(outputFilePath));
		}
		finally
		{
			FileUtils.safeDeleteFile(tempDir.toFile());
		}
	}


	@Test
	public final void sendPackageViaServer()
	{
		Assertions.assertThrows(IllegalStateException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Packager newPackager = new Packager();
				newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false);
			}
		});
	}

	@Test
	public final void sendPackageViaServerOverrideServer() throws Exception
	{
		Loupe.start("Just making sure we're up and logging for this test.");

		Packager newPackager = new Packager();
		newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, "loupe-test.onloupe.com", 0, false, null, SERVER_OVERRIDE_REPOSITORY);
	}

	@Test
	public final void sendPackageViaServerOverrideServerAndRepository() throws Exception
	{
		Loupe.start("Just making sure we're up and logging for this test.");

		Packager newPackager = new Packager();
		newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, "loupe-test.onloupe.com", 0, true, null, SERVER_OVERRIDE_REPOSITORY);
	}

	@Test
	public final void sendPackageViaServerMissingArgsCustomerNull()
	{
		Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Packager newPackager = new Packager();
				newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, null);
			}
		});
	}

	@Test
	public final void sendPackageViaServerMissingArgsCustomerEmpty()
	{
		Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Packager newPackager = new Packager();
				newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, "");
			}
		});
	}

	@Test
	public final void sendPackageViaServerMissingArgsServerNull()
	{
		Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Packager newPackager = new Packager();
				newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, null, 0, false, null, null);
			}
		});
	}

	@Test
	public final void sendPackageViaServerMissingArgsServerEmpty()
	{
		Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Packager newPackager = new Packager();
					newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, "", 0, false, null, null);
			}
		});
	}

	@Test
	public final void sendPackageViaServerBadArgsPortNegative()
	{
		Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Packager newPackager = new Packager();
				newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, SERVER_OVERRIDE_SERVER_NAME, -20, false, SERVER_OVERRIDE_BASE_DIRECTORY, null);
			}
		});
	}
	
	@Test
	public final void sendPackageViaServerOverrideCustomer() throws Exception
	{
		Loupe.start("Just making sure we're up and logging for this test.");
		
		Packager newPackager = new Packager();
		newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, SERVER_OVERRIDE_CUSTOMER_NAME);
	}

	@AfterAll
	public final void cleanup()
	{
		try {
			Files.delete(outputFilePath);
		} catch (IOException e) {
			// do nothing
		}
	}
}