package com.onloupe.core.serialization.monitor;

import java.util.UUID;

/**
 * Implemented by any extensible data object to connect to its unique Id which
 * it shares with its extension object.
 */
public interface IDataObject {
	/**
	 * The unique Id of the data object which it shares with its extension object.
	 */
	UUID getId();
}