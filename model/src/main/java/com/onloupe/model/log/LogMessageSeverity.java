package com.onloupe.model.log;

public enum LogMessageSeverity {
	
	UNKNOWN(0), CRITICAL(1), ERROR(2), WARNING(4), INFORMATION(8), VERBOSE(16);
	
	private final int severity;
	
	private LogMessageSeverity(int severity) {
		this.severity = severity;
	}

	public int getSeverity() {
		return severity;
	}
	
	public static LogMessageSeverity forInt(int severity) {
		for (LogMessageSeverity logMessageSeverity : LogMessageSeverity.values()) {
			if (logMessageSeverity.getSeverity() == severity) {
				return logMessageSeverity;
			}
		}
		return UNKNOWN;
	}

}
