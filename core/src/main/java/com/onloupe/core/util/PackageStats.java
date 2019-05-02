package com.onloupe.core.util;

public class PackageStats {

	private Integer sessions;
	private Integer problemSessions;
	private Integer files;
	private Long bytes;

	public PackageStats() {
		// TODO Auto-generated constructor stub
	}

	public PackageStats(Integer sessions, Integer problemSessions, Integer files, Long bytes) {
		super();
		this.sessions = sessions;
		this.problemSessions = problemSessions;
		this.files = files;
		this.bytes = bytes;
	}

	public Integer getSessions() {
		return this.sessions;
	}

	public void setSessions(Integer sessions) {
		this.sessions = sessions;
	}

	public Integer getProblemSessions() {
		return this.problemSessions;
	}

	public void setProblemSessions(Integer problemSessions) {
		this.problemSessions = problemSessions;
	}

	public Integer getFiles() {
		return this.files;
	}

	public void setFiles(Integer files) {
		this.files = files;
	}

	public Long getBytes() {
		return this.bytes;
	}

	public void setBytes(Long bytes) {
		this.bytes = bytes;
	}

}
