package com.onloupe.core.messaging;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

// TODO: Auto-generated Javadoc
/**
 * Event arguments for LocalServerDiscoveryFile events.
 */
public class LocalServerDiscoveryFileEventArgs {
	
	/** The file that was affected. */
	private LocalServerDiscoveryFile file;
	
	/** The kind. */
	private Kind<Path> kind;

	/**
	 * Create a new event argument.
	 *
	 * @param file the file
	 * @param kind the kind
	 */
	public LocalServerDiscoveryFileEventArgs(LocalServerDiscoveryFile file, Kind<Path> kind) {
		this.kind = kind;
		this.file = file;
	}

	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	public final LocalServerDiscoveryFile getFile() {
		return this.file;
	}

	/**
	 * Gets the kind.
	 *
	 * @return the kind
	 */
	public Kind<Path> getKind() {
		return this.kind;
	}
}