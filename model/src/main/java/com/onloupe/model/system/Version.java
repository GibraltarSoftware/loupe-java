package com.onloupe.model.system;

import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
	
	private int major = -1;
	private int minor = -1;
	private int build = -1;
	private int revision = -1;
	
	public Version() {
		// TODO Auto-generated constructor stub
	}
	
	public Version(int major) {
		super();
		this.major = major;
	}

	public Version(int major, int minor) {
		super();
		this.major = major;
		this.minor = minor;
	}

	public Version(int major, int minor, int build) {
		super();
		this.major = major;
		this.minor = minor;
		this.build = build;
	}

	public Version(int major, int minor, int build, int revision) {
		super();
		this.major = major;
		this.minor = minor;
		this.build = build;
		this.revision = revision;
	}

	public Version(String version) {
		super();
		String[] parts = version.split(Pattern.quote("."));
		
		try {
			this.major = Integer.valueOf(parts[0]);
			this.minor = Integer.valueOf(parts[1]);
			this.build = Integer.valueOf(parts[2]);
			this.revision = Integer.valueOf(parts[3]);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {

		}
	}
	
	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getBuild() {
		return build;
	}

	public int getRevision() {
		return revision;
	}

	@Override
	public int compareTo(Version o) {
		if (o == null)
			throw new NullPointerException("other version is null");
		
		int eval = Integer.compare(major, o.getMajor());
		if (eval != 0) {
			return eval;
		}
		
		eval = Integer.compare(minor, o.getMinor());
		if (eval != 0) {
			return eval;
		}
		
		eval = Integer.compare(build, o.getBuild());
		if (eval != 0) {
			return eval;
		}
		
		return Integer.compare(revision, o.getRevision());
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		if (major != -1) {
			builder.append(String.valueOf(major));
		}

		if (minor != -1) {
			builder.append(".");
			builder.append(String.valueOf(minor));
		}

		if (build != -1) {
			builder.append(".");
			builder.append(String.valueOf(build));
		}
		
		if (revision != -1) {
			builder.append(".");
			builder.append(String.valueOf(revision));
		}
		
		return builder.toString();
	}

}
