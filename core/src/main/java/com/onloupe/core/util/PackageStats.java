package com.onloupe.core.util;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageStats.
 */
public class PackageStats {

	/** The sessions. */
	private Integer sessions;
	
	/** The problem sessions. */
	private Integer problemSessions;
	
	/** The files. */
	private Integer files;
	
	/** The bytes. */
	private Long bytes;

	/**
	 * Instantiates a new package stats.
	 */
	public PackageStats() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new package stats.
	 *
	 * @param sessions the sessions
	 * @param problemSessions the problem sessions
	 * @param files the files
	 * @param bytes the bytes
	 */
	public PackageStats(Integer sessions, Integer problemSessions, Integer files, Long bytes) {
		super();
		this.sessions = sessions;
		this.problemSessions = problemSessions;
		this.files = files;
		this.bytes = bytes;
	}

	/**
	 * Gets the sessions.
	 *
	 * @return the sessions
	 */
	public Integer getSessions() {
		return this.sessions;
	}

	/**
	 * Sets the sessions.
	 *
	 * @param sessions the new sessions
	 */
	public void setSessions(Integer sessions) {
		this.sessions = sessions;
	}

	/**
	 * Gets the problem sessions.
	 *
	 * @return the problem sessions
	 */
	public Integer getProblemSessions() {
		return this.problemSessions;
	}

	/**
	 * Sets the problem sessions.
	 *
	 * @param problemSessions the new problem sessions
	 */
	public void setProblemSessions(Integer problemSessions) {
		this.problemSessions = problemSessions;
	}

	/**
	 * Gets the files.
	 *
	 * @return the files
	 */
	public Integer getFiles() {
		return this.files;
	}

	/**
	 * Sets the files.
	 *
	 * @param files the new files
	 */
	public void setFiles(Integer files) {
		this.files = files;
	}

	/**
	 * Gets the bytes.
	 *
	 * @return the bytes
	 */
	public Long getBytes() {
		return this.bytes;
	}

	/**
	 * Sets the bytes.
	 *
	 * @param bytes the new bytes
	 */
	public void setBytes(Long bytes) {
		this.bytes = bytes;
	}

}
