package com.onloupe.core.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Consolidates storage summary information for all packet types and fragments
 * in a session.
 */
public class FileStorageSummary {
	
	/** List of records providing storage summary info about each packet type in the session fragments Note that PacketSize is calculated by scaling the uncompressed packet sizes to their portion of TotalRawFileSize. */
	public final List<PacketTypeStorageSummary> packetList = new ArrayList<PacketTypeStorageSummary>();

	/**
	 * List of records providing storage summary info about each fragment associated
	 * with this session.
	 */
	public final List<FragmentStorageSummary> fragmentList = new ArrayList<FragmentStorageSummary>();

	/** Returns the total number of bytes for all fragments. */
	private long totalRawFileSize;

	/**
	 * Gets the total raw file size.
	 *
	 * @return the total raw file size
	 */
	public final long getTotalRawFileSize() {
		return this.totalRawFileSize;
	}

	/**
	 * Sets the total raw file size.
	 *
	 * @param value the new total raw file size
	 */
	private void setTotalRawFileSize(long value) {
		this.totalRawFileSize = value;
	}

	/** Returns the total number of bytes for all uncompressed packets. */
	private long totalPacketSize;

	/**
	 * Gets the total packet size.
	 *
	 * @return the total packet size
	 */
	public final long getTotalPacketSize() {
		return this.totalPacketSize;
	}

	/**
	 * Sets the total packet size.
	 *
	 * @param value the new total packet size
	 */
	private void setTotalPacketSize(long value) {
		this.totalPacketSize = value;
	}

	/**
	 * Merge data from one session fragment.
	 *
	 * @param packetTypes the packet types
	 * @param fragment the fragment
	 */
	public final void merge(ArrayList<PacketTypeStorageSummary> packetTypes, FragmentStorageSummary fragment) {
		this.fragmentList.add(fragment);

		for (PacketTypeStorageSummary item : packetTypes) {
			boolean found = false;
			for (PacketTypeStorageSummary summary : this.packetList) {
				if (summary.getQualifiedTypeName().equals(item.getQualifiedTypeName())) {
					summary.setPacketCount(summary.getPacketCount() + item.getPacketCount());
					summary.setPacketSize(summary.getPacketSize() + item.getPacketSize());
					found = true;
					break;
				}
			}

			if (!found) {
				this.packetList.add(item);
			}
		}
	}

	/**
	 * Summarize the data about fragments and packet types.
	 */
	public final void summarize() {
		Collections.sort(this.packetList);
		Collections.sort(this.fragmentList);

		setTotalRawFileSize(0);
		for (FragmentStorageSummary fragment : this.fragmentList) {
			setTotalRawFileSize(getTotalRawFileSize() + fragment.getFragmentSize());
		}

		setTotalPacketSize(0);
		for (PacketTypeStorageSummary packetType : this.packetList) {
			setTotalPacketSize(getTotalPacketSize() + packetType.getPacketSize());
		}

		// Normalize all the packet sizes to apportion every byte of the raw file sizes
		// to packets.
		long remainingBytes = getTotalRawFileSize();
		for (int i = this.packetList.size() - 1; i >= 1; i--) {
			PacketTypeStorageSummary currentPacket = this.packetList.get(i);
			double percentage = (double) currentPacket.getPacketSize() / getTotalPacketSize();
			currentPacket.setPacketSize(Math.round(getTotalRawFileSize() * percentage));
			remainingBytes -= currentPacket.getPacketSize();
		}

		// This last little bit of logic deals with ensuring we don't lose any bytes to
		// round-off error.
		// We apply this to the largest value so the adjustment will be the least
		// significant
		if (!this.packetList.isEmpty()) {
			this.packetList.get(0).setPacketSize(remainingBytes);
		}
	}
}