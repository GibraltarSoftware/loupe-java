package com.onloupe.core.beans;

import java.io.Closeable;
import java.io.IOException;

import com.onloupe.configuration.AgentConfiguration;
import com.onloupe.core.logging.Log;


/**
 * The Class LoupeManagementBean.
 */
public class LoupeManagementBean implements Closeable {

	/**
	 * Start.
	 *
	 * @param configuration the configuration
	 */
	public void start(AgentConfiguration configuration) {
		try {
			Log.start(configuration);
		} catch (IOException e) {
			// deal with this later
		}
	}
	
	/**
	 * Start.
	 */
	public void start() {
		try {
			Log.start();
		} catch (IOException e) {
			// deal with this later
		}
	}
	
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		Log.shutdown();
	}

}
