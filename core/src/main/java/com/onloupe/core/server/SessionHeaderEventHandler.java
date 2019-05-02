package com.onloupe.core.server;

/**
 * Delegate for handling session header events
 * 
 * @param sender
 * @param e
 */
@FunctionalInterface
public interface SessionHeaderEventHandler {
	void invoke(Object sender, SessionHeaderEventArgs e);
}