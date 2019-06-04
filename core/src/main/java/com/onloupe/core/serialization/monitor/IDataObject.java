package com.onloupe.core.serialization.monitor;

import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * Implemented by any extensible data object to connect to its unique Id which
 * it shares with its extension object.
 */
public interface IDataObject {
	
	/**
	 * The unique Id of the data object which it shares with its extension object.
	 *
	 * @return the id
	 */
	UUID getId();
}