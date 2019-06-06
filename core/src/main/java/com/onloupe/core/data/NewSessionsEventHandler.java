package com.onloupe.core.data;


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