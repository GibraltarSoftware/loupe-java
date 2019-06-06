package com.onloupe.core.serialization;

import java.time.OffsetDateTime;


/**
 * Records a summary of size for one specific session fragment.
 */
public class FragmentStorageSummary implements java.lang.Comparable<FragmentStorageSummary> {
	
	/** Start time of fragment. */
	private OffsetDateTime startTime;

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public final OffsetDateTime getStartTime() {
		return this.startTime;
	}

	/**
	 * Sets the start time.
	 *
	 * @param value the new start time
	 */
	private void setStartTime(OffsetDateTime value) {
		this.startTime = value;
	}

	/** End time of fragment. */
	private OffsetDateTime endTime;

	/**
	 * Gets the end time.
	 *
	 * @return the end time
	 */
	public final OffsetDateTime getEndTime() {
		return this.endTime;
	}

	/**
	 * Sets the end time.
	 *
	 * @param value the new end time
	 */
	private void setEndTime(OffsetDateTime value) {
		this.endTime = value;
	}

	/** Number of bytes in the fragment. */
	private long fragmentSize;

	/**
	 * Gets the fragment size.
	 *
	 * @return the fragment size
	 */
	public final long getFragmentSize() {
		return this.fragmentSize;
	}

	/**
	 * Sets the fragment size.
	 *
	 * @param value the new fragment size
	 */
	private void setFragmentSize(long value) {
		this.fragmentSize = value;
	}

	/**
	 * Create a storage summary instance for a particular session fragment.
	 *
	 * @param startTime the start time
	 * @param endTime the end time
	 * @param size the size
	 */
	public FragmentStorageSummary(OffsetDateTime startTime, OffsetDateTime endTime, long size) {
		setStartTime(startTime);
		setEndTime(endTime);
		setFragmentSize(size);
	}

	/**
	 * Compare two FragmentStorageSummary for sorting purposes.
	 *
	 * @param other the other
	 * @return the int
	 */
	@Override
	public final int compareTo(FragmentStorageSummary other) {
		return (getEndTime().compareTo(other.getEndTime()));
	}
}