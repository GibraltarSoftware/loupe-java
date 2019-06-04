package com.onloupe.core.server;

// TODO: Auto-generated Javadoc
/**
 * The delegate for handling the Credentials Required event.
 */
@FunctionalInterface
public interface CredentialsRequiredEventHandler {
	
	/**
	 * Invoke.
	 *
	 * @param source the source
	 * @param e the e
	 */
	void invoke(Object source, CredentialsRequiredEventArgs e);
}