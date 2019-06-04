package com.onloupe.core.data;

// TODO: Auto-generated Javadoc
/**
 * An event handler for the New Sessions Event Arguments.
 */
@FunctionalInterface
public interface NewSessionsEventHandler {
	
	/**
	 * Invoke.
	 *
	 * @param state the state
	 * @param e the e
	 */
	void invoke(Object state, NewSessionsEventArgs e);
}