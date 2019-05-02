package com.onloupe.core.beans;

import java.io.Closeable;
import java.io.IOException;

import com.onloupe.configuration.AgentConfiguration;
import com.onloupe.core.logging.Log;

public class LoupeManagementBean implements Closeable {

	public void start(AgentConfiguration configuration) {
		try {
			Log.start(configuration);
		} catch (IOException e) {
			// deal with this later
		}
	}
	
	public void start() {
		try {
			Log.start();
		} catch (IOException e) {
			// deal with this later
		}
	}
	
	@Override
	public void close() throws IOException {
		Log.shutdown();
	}

}
