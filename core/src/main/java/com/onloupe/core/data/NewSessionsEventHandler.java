package com.onloupe.core.data;

/**
 * An event handler for the New Sessions Event Arguments
 * 
 * @param state
 * @param e
 */
@FunctionalInterface
public interface NewSessionsEventHandler {
	void invoke(Object state, NewSessionsEventArgs e);
}