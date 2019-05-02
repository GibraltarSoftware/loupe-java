package com.onloupe.core.data;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.onloupe.core.logging.Log;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.util.FileUtils;
import com.onloupe.core.util.TypeUtils;

@TestInstance(Lifecycle.PER_CLASS)
public class SimplePackageTests {
	private String _OutputFileNamePath;

	@BeforeAll
	public final void init() throws IOException {
		if (TypeUtils.isNotBlank(this._OutputFileNamePath)) {
			// we're re-initing: delete any existing temp file.
			(new File(this._OutputFileNamePath)).delete();
		}

		File tempFile = Files.createTempFile("package-", "." + Log.PACKAGE_EXTENSION).toFile();
		this._OutputFileNamePath = tempFile.getAbsolutePath();
		tempFile.delete(); // get temp file name creates the file as part of allocating the name.
	}

	@Test
	public final void testCreateEmptyPackage() throws IOException {
		try (SimplePackage package_Renamed = new SimplePackage(this._OutputFileNamePath)) {
			Assertions.assertTrue((new File(this._OutputFileNamePath)).isFile(), "Package was not created");
			(new File(this._OutputFileNamePath)).delete();
		}
	}

	@Test
	public final void testCreateLargePackage() throws IOException {
		File file = new File(this._OutputFileNamePath);
		try (SimplePackage package_Renamed = new SimplePackage(file.getPath())) {
			File repository = new File(
					LocalRepository.calculateRepositoryPath(Log.getSessionSummary().getProduct(), null));

			File[] allExistingFileFragments = repository.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile() && FileUtils.getFileExtension(file.getName()).equalsIgnoreCase("." + Log.LOG_EXTENSION);
				}
			});

			for (File fileFragment : allExistingFileFragments) {
				// change to random access file
				try (RandomAccessFile sourceFile = FileUtils.openRandomAccessFile(fileFragment.getAbsolutePath(), "rwd")) {
					if (sourceFile != null) {
						package_Renamed.addSession(sourceFile);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		Assertions.assertTrue(file.isFile(), "Package was not created");
		Assertions.assertTrue(file.length() > 100,
				"The package was likely empty but should have contained multiple sessions.");

		(new File(this._OutputFileNamePath)).delete();
	}
}