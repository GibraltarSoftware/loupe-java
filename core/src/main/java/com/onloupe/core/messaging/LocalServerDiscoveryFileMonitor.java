package com.onloupe.core.messaging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import com.onloupe.core.data.PathManager;
import com.onloupe.core.data.PathType;
import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.log.LogMessageSeverity;


/**
 * Monitors the discovery directory of the local file system for discovery file
 * changes.
 */
public class LocalServerDiscoveryFileMonitor extends Observable {
	
	/** The lock. */
	private final Object lock = new Object();
	
	/** The discovery files. */
	private final Map<String, LocalServerDiscoveryFile> discoveryFiles = new HashMap<String, LocalServerDiscoveryFile>();

	/** The active thread. */
	private volatile boolean activeThread = false; // indicates if we have an active thread pool request

	/** The discovery path. */
	private Path discoveryPath;
	
	/** The watch service. */
	private WatchService watchService;

	/**
	 * Begin monitoring for file changes.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void start() throws IOException {
		synchronized (this.lock) {
			if (this.watchService == null) {
				this.discoveryPath = Paths.get(PathManager.findBestPath(PathType.DISCOVERY));

				this.watchService = this.discoveryPath.getFileSystem().newWatchService();

				this.discoveryPath.register(this.watchService, StandardWatchEventKinds.ENTRY_MODIFY,
						StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);

				// we need to *force* an event for every existing file.
				File[] fileSystemEntries = this.discoveryPath.toFile().listFiles(LocalServerDiscoveryFile.FILE_FILTER);
				for (File fileSystemEntry : fileSystemEntries) {
					checkRaiseChangedEvent(fileSystemEntry.getAbsolutePath(), StandardWatchEventKinds.ENTRY_CREATE);
				}

				this.activeThread = true;

				// poll watcher in an infinite loop on its own thread.
				Thread thread = new Thread() {
					@Override
					public void run() {
						doMonitor();
					}
				};
				
				thread.setName("Loupe local server discovery file monitor");
				thread.start();
			}
		}
	}

	/**
	 * Stop monitoring for file changes.
	 */
	public final void stop() {
		synchronized (this.lock) {
			this.activeThread = false;

			if (this.watchService != null) {
				IOUtils.closeQuietly(this.watchService);
				this.watchService = null;
			}
		}
	}

	/**
	 * Do monitor.
	 */
	// warnings shouldn't matter, because we *know* this watcher generates type path
	@SuppressWarnings("unchecked")
	private void doMonitor() {
		try {
			WatchKey key;
			while (this.activeThread) {
				key = this.watchService.poll(1, TimeUnit.SECONDS);
				if (key != null) {
					for (WatchEvent<?> event : key.pollEvents()) {
						Kind<Path> eventKind = (Kind<Path>) event.kind();
						Path relativePath = ((WatchEvent<Path>) event).context();

						if (relativePath != null && TypeUtils.endsWithIgnoreCase(relativePath.toString(), ".gpd")) {
							Path absolutePath = this.discoveryPath.resolve(relativePath);
							if (eventKind == StandardWatchEventKinds.ENTRY_MODIFY
									|| eventKind == StandardWatchEventKinds.ENTRY_CREATE) {
								checkRaiseChangedEvent(absolutePath.toAbsolutePath().toString(), eventKind);
							} else if (eventKind == StandardWatchEventKinds.ENTRY_DELETE) {
								checkRaiseDeletedEvent(absolutePath.toAbsolutePath().toString());
							}
						}
					}
					key.reset();
				}
			}
		} catch (Exception e) {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, e, true, NetworkMessenger.LOG_CATEGORY,
						"local server discovery file monitor queue event threw an exception, queue processing will pause",
						"While we were dequeuing items or raising events an exception was thrown.  Queue processing will be interrupted until the next request comes in "
								+ "and the request that caused the exception will be dropped.\r\n%s exception thrown:\r\n%s",
						e.getClass().getName(), e.getMessage());
			}
		}
	}

	/**
	 * Raises the FileChanged event.
	 *
	 * @param e the e
	 */
	protected void onFileChanged(LocalServerDiscoveryFileEventArgs e) {
		setChanged();
		notifyObservers(e);
	}

	/**
	 * Raises the FileDeleted event.
	 *
	 * @param e the e
	 */
	protected void onFileDeleted(LocalServerDiscoveryFileEventArgs e) {
		setChanged();
		notifyObservers(e);
	}

	/**
	 * Check raise changed event.
	 *
	 * @param fullPath the full path
	 * @param kind the kind
	 */
	private void checkRaiseChangedEvent(String fullPath, Kind<Path> kind) {
		LocalServerDiscoveryFileEventArgs eventArgs = null;
		synchronized (this.lock) {
			LocalServerDiscoveryFile newItem = null;
			if (!this.discoveryFiles.containsKey(fullPath)) {
				// we don't actually process change events, only adds.
				try {
					newItem = new LocalServerDiscoveryFile(fullPath);
					if (newItem.isAlive()) {
						this.discoveryFiles.put(fullPath, newItem);
					}
				} catch (Exception ex) {
					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.INFORMATION, NetworkMessenger.LOG_CATEGORY,
								"Unable to load local server discovery file due to " + ex.getClass() + " exception",
								"While attempting to load a local server discovery file an exception was thrown.  If this is because the file wasn't found "
										+ "or was incomplete it can be ignored.  An incomplete file will raise another event when complete that will cause it to be re-processed.\r\n"
										+ "File: %s\r\nException: %s",
								fullPath, ex.getMessage());
					}
				}

				if (newItem != null) {
					eventArgs = new LocalServerDiscoveryFileEventArgs(newItem, kind);
				}
			}
		}

		// raise the event outside of our lock
		if (eventArgs != null) {
			onFileChanged(eventArgs);
		}
	}

	/**
	 * Check raise deleted event.
	 *
	 * @param fullPath the full path
	 */
	private void checkRaiseDeletedEvent(String fullPath) {
		LocalServerDiscoveryFileEventArgs eventArgs = null;
		synchronized (this.lock) {
			LocalServerDiscoveryFile victim = this.discoveryFiles.get(fullPath);
			if (victim != null) {
				// indeed it existed so we want to raise the event.
				this.discoveryFiles.remove(fullPath);
				eventArgs = new LocalServerDiscoveryFileEventArgs(victim, StandardWatchEventKinds.ENTRY_DELETE);
			}
		}

		// raise the event outside of the lock.
		if (eventArgs != null) {
			onFileDeleted(eventArgs);
		}
	}

	/**
	 * Checks if is active thread.
	 *
	 * @return true, if is active thread
	 */
	public boolean isActiveThread() {
		return this.activeThread;
	}
}