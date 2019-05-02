package com.onloupe.core.serialization;

import java.time.OffsetDateTime;

/**
 * Records a summary of size for one specific session fragment
 */
public class FragmentStorageSummary implements java.lang.Comparable<FragmentStorageSummary> {
	/**
	 * Start time of fragment
	 */
	private OffsetDateTime startTime;

	public final OffsetDateTime getStartTime() {
		return this.startTime;
	}

	private void setStartTime(OffsetDateTime value) {
		this.startTime = value;
	}

	/**
	 * End time of fragment
	 */
	private OffsetDateTime endTime;

	public final OffsetDateTime getEndTime() {
		return this.endTime;
	}

	private void setEndTime(OffsetDateTime value) {
		this.endTime = value;
	}

	/**
	 * Number of bytes in the fragment
	 */
	private long fragmentSize;

	public final long getFragmentSize() {
		return this.fragmentSize;
	}

	private void setFragmentSize(long value) {
		this.fragmentSize = value;
	}

	/**
	 * Create a storage summary instance for a particular session fragment
	 */
	public FragmentStorageSummary(OffsetDateTime startTime, OffsetDateTime endTime, long size) {
		setStartTime(startTime);
		setEndTime(endTime);
		setFragmentSize(size);
	}

	/**
	 * Compare two FragmentStorageSummary for sorting purposes
	 */
	@Override
	public final int compareTo(FragmentStorageSummary other) {
		return (getEndTime().compareTo(other.getEndTime()));
	}
}