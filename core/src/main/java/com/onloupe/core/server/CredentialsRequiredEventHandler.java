package com.onloupe.core.server;

/**
 * The delegate for handling the Credentials Required event.
 * 
 * @param source
 * @param e
 */
@FunctionalInterface
public interface CredentialsRequiredEventHandler {
	void invoke(Object source, CredentialsRequiredEventArgs e);
}