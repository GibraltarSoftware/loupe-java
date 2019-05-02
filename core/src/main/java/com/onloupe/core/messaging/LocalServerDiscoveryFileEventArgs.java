package com.onloupe.core.messaging;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

/**
 * Event arguments for LocalServerDiscoveryFile events.
 */
public class LocalServerDiscoveryFileEventArgs {
	/**
	 * The file that was affected
	 */
	private LocalServerDiscoveryFile file;
	private Kind<Path> kind;

	/**
	 * Create a new event argument
	 * 
	 * @param file
	 */
	public LocalServerDiscoveryFileEventArgs(LocalServerDiscoveryFile file, Kind<Path> kind) {
		this.kind = kind;
		this.file = file;
	}

	public final LocalServerDiscoveryFile getFile() {
		return this.file;
	}

	public Kind<Path> getKind() {
		return this.kind;
	}
}