package com.onloupe.core.data;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.EnumSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.Executable;

import com.onloupe.agent.Packager;
import com.onloupe.agent.SessionCriteria;
import com.onloupe.core.logging.Log;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.data.ISessionSummaryCollection;
import com.onloupe.model.exception.GibraltarException;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.session.ISessionSummary;

import junit.framework.Assert;

@TestInstance(Lifecycle.PER_CLASS)
public class PackagerTests {
	private static final String REPOSITORY_NAME = "ConfigurationTest";

	private String _OutputFileNamePath;

	@BeforeAll
	public final void init() throws IOException {
		if (TypeUtils.isNotBlank(this._OutputFileNamePath)) {
			// we're re-initing: delete any existing temp file.
			(new File(this._OutputFileNamePath)).delete();
		}

		File tempFile = null;
		try {
			tempFile = Files.createTempFile("packager-", "." + Log.PACKAGE_EXTENSION).toFile();
		} catch (IOException e) {
			Assertions.fail(e);
		}

		this._OutputFileNamePath = tempFile.getAbsolutePath();
		tempFile.delete(); // just trying to be clean
		Log.start(null, 1, null);
		Log.write(LogMessageSeverity.INFORMATION, "Tests.Packager", "Initialized Packager Tests", "If we are running in isolation then we just started logging.");
		Log.endFile("Rolling over log file to ensure we have something to package");
	}

	@BeforeEach
	public final void setup() {
		(new File(this._OutputFileNamePath)).delete();
		assert !(new File(this._OutputFileNamePath)).isFile();
	}

	@Test
	public final void testCreateNonePackage() {
		// get this out of try block
		try {
			Packager newPackager = new Packager();
			newPackager.sendToFile(SessionCriteria.NONE, false, this._OutputFileNamePath);
		} catch (IOException e) {
			Assertions.fail(e);
		}

		// because this is the none package it won't have any content, but won't thrown
		// an error.
		Assertions.assertFalse((new File(this._OutputFileNamePath)).isFile());
	}

	@Test
	public final void testCreateNewSessionsPackage() {
		try {
			ISessionSummaryCollection newSessions = getNewSessions();
			if (newSessions.size() == 0) {
				Log.write(LogMessageSeverity.VERBOSE, "", "Unit test createNewSessionsPackage()",
						"newSessions.size() == 0");
				return;
			}

			Packager newPackager = new Packager();
			newPackager.sendToFile(SessionCriteria.NEW, false, this._OutputFileNamePath);

			Assert.assertTrue(
					"There was no output file from the package, most likely because there is no local data to package (like when unit tests are run the first time)",
					(new File(this._OutputFileNamePath)).isFile());
		} catch (IOException e) {
			Assertions.fail(e);
		}
	}

	@Test
	public final void testCreateAllSessionsPackage() {
		try {
			Packager newPackager = new Packager();
			newPackager.sendToFile(SessionCriteria.ALL_SESSIONS, false, this._OutputFileNamePath);
		} catch (IOException e) {
			Assertions.fail(e);
		}

		Assert.assertTrue(
				"There was no output file from the package, most likely because there is no local data to package (like when unit tests are run the first time)",
				(new File(this._OutputFileNamePath)).isFile());
	}

	@Test
	public final void testCreateActiveSessionPackage() {
		try {
			Packager newPackager = new Packager();
			newPackager.sendToFile(SessionCriteria.ACTIVE, false, this._OutputFileNamePath);
		} catch (IOException e) {
			Assertions.fail(e);
		}

		Assertions.assertTrue((new File(this._OutputFileNamePath)).isFile(),
				"There was no output file from the package, most likely because there is no local data to package (like when unit tests are run the first time)");
	}

	@Test
	public final void testMarkSessionsAsRead() {
		try {
			// find all of the "new" sessions
			ISessionSummaryCollection newSessions = getNewSessions();

			if (newSessions.size() == 0) {
				Log.write(LogMessageSeverity.VERBOSE, "", "Unit test markSessionsAsRead()", "newSessions.size() == 0");
				return;
			}

			Packager newPackager = new Packager(Log.getSessionSummary().getProduct());
			// create the package
			newPackager.sendToFile(SessionCriteria.NEW, true, this._OutputFileNamePath);

			Assertions.assertTrue((new File(this._OutputFileNamePath)).isFile(),
					"There was no output file from the package, most likely because there is no local data to package (like when unit tests are run the first time)");

			// Now find out what new sessions there are to compare.
			ISessionSummaryCollection newSessionsPost = getNewSessions();

			// and compare the two.
			if (newSessionsPost.size() == 0) {
				// we HAVE to be good - there are no new.
			} else {
				// we MIGHT still be good - if none of our new sessions are still there.
				for (ISessionSummary newSession : newSessions) {
					// this session Id better not be in the new list....
					Assertions.assertNull(
							newSessionsPost.find((ISessionSummary obj) -> obj.getId() == newSession.getId()),
							String.format(
									"The session %s is still in the index as new and should have been marked sent.",
									newSession.getId())); // null is not found
				}
			}
		} catch (UnknownHostException e) {
			Assertions.fail(e);
		} catch (IOException e) {
			Assertions.fail(e);
		}
	}

	@Test
	public final void testSendPackageViaWebNoConfigFail() {
		Assertions.assertThrows(IllegalStateException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Packager newPackager = new Packager();
				// we are saying don't override config,then basically sending default values for
				// all of the config overrides (there is no overload that skips them)
				newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, false, false, false, null, null, 0, false,
						null, null);
			}
		});
	}

	@Test
	public final void testSendPackageViaWebOverrideCustomer() {
		try {
			Packager newPackager = new Packager();
			newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, false, true, true, REPOSITORY_NAME, null, 0,
					false, null, null);
		} catch (IOException e) {
			Assertions.fail(e);
		} catch (Exception e) {
			Assertions.fail(e);
		}
	}

	@Test
	public final void testSendPackageViaWebOverrideServerAndRepository() {
		try {
			Packager newPackager = new Packager();
			newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, false, true, false, null, "loupe-test.onloupe.com", 0,
					true, null, "GibraltarSoftware");
		} catch (IOException e) {
			Assertions.fail(e);
		} catch (Exception e) {
			Assertions.fail(e);
		}
	}

	@Test
	public final void testSendPackageViaWebFail() {
		Assertions.assertThrows(GibraltarException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Packager newPackager = new Packager();
				newPackager.sendToServer(SessionCriteria.ALL_SESSIONS, false, false, true, true,
						"BogusNonexistantCustomer", null, 0, false, null, null);
			}
		});
	}

	@AfterAll
	public final void cleanup() {
		if (TypeUtils.isNotBlank(this._OutputFileNamePath) && (new File(this._OutputFileNamePath)).isFile()) {
			(new File(this._OutputFileNamePath)).delete();
		}
	}

	private static ISessionSummaryCollection getNewSessions() throws IOException {
		LocalRepository localRepository = new LocalRepository(Log.getSessionSummary().getProduct());
		return localRepository
				.find((new SessionCriteriaPredicate(Log.getSessionSummary().getProduct(), null, EnumSet.of(SessionCriteria.NEW))));
	}
}