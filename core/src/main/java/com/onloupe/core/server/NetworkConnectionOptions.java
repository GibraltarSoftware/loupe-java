package com.onloupe.core.server;

import java.util.Locale;

/**
 * Connection options used to establish a socket from the local system to an
 * endpoint
 */
public class NetworkConnectionOptions {
	private volatile int hashCode;

	public NetworkConnectionOptions(int port, String hostName, boolean useSsl) {
		super();
		this.port = port;
		this.hostName = hostName;
		this.useSsl = useSsl;
	}

	public NetworkConnectionOptions(int _HashCode, int port, String hostName, boolean useSsl) {
		super();
		this.hashCode = _HashCode;
		this.port = port;
		this.hostName = hostName;
		this.useSsl = useSsl;
	}

	/**
	 * The TCP Port to connect to
	 */
	private int port;

	public final int getPort() {
		return this.port;
	}

	public final void setPort(int value) {
		this.port = value;
	}

	/**
	 * The host name or IP Address to connect to
	 */
	private String hostName;

	public final String getHostName() {
		return this.hostName;
	}

	public final void setHostName(String value) {
		this.hostName = value;
	}

	/**
	 * Indicates if the connection should be encrypted using Ssl or not.
	 */
	private boolean useSsl;

	public final boolean getUseSsl() {
		return this.useSsl;
	}

	public final void setUseSsl(boolean value) {
		this.useSsl = value;
	}

	/**
	 * Create a copy of this set of connection options
	 * 
	 * @return
	 */
	@Override
	public final NetworkConnectionOptions clone() {
		return new NetworkConnectionOptions(getPort(), getHostName(), getUseSsl());
	}

	/**
	 * Returns a <see cref="T:System.String"/> that represents the current
	 * <see cref="T:System.Object"/>.
	 * 
	 * @return A <see cref="T:System.String"/> that represents the current
	 *         <see cref="T:System.Object"/>.
	 * 
	 *         <filterpriority>2</filterpriority>
	 */
	@Override
	public String toString() {
		return String.format("%1$s:%2$s UseSsl: %3$s", getHostName(), getPort(), getUseSsl());
	}

	/**
	 * Compares this ThreadInfo object to another to determine sorting order.
	 * 
	 * ThreadInfo instances are sorted by their ThreadId property.
	 * 
	 * @param other The other ThreadInfo object to compare this object to.
	 * @return An int which is less than zero, equal to zero, or greater than zero
	 *         to reflect whether this ThreadInfo should sort as being less-than,
	 *         equal to, or greater-than the other ThreadInfo, respectively.
	 */
	public final int compareTo(NetworkConnectionOptions other) {
		if (other == null) {
			return 1; // We're not null, so we're greater than anything that is null.
		}

		if (this == other) {
			return 0; // Refers to the same instance, so obviously we're equal.
		}

		// the most important comparison is hostname, which unfortunately can be null.
		int compare = 0;
		if (!getHostName().equals(other.getHostName())) {
			// they aren't the same so dig into it...
			if (getHostName() == null) {
				// then the other can't be null, so it's greater than us.
				compare = -1;
			} else if (other.getHostName() == null) {
				// we can't be null, so we're greater than anything that is null.
				compare = 1;
			} else {
				// neither of us are null so now we can do a normal string compare.
				compare = getHostName().compareToIgnoreCase(other.getHostName());
			}
		}

		// Unfortunately, ThreadId isn't as unique as we thought, so do some follow-up
		// compares.
		if (compare == 0) {
			compare = (new Integer(getPort())).compareTo(other.getPort());
		}

		if (compare == 0) {
			compare = (new Boolean(getUseSsl())).compareTo(other.getUseSsl());
		}

		return compare;
	}

	/**
	 * Determines whether the specified <see cref="T:System.Object"/> is equal to
	 * the current <see cref="T:System.Object"/>.
	 * 
	 * @return true if the specified <see cref="T:System.Object"/> is equal to the
	 *         current <see cref="T:System.Object"/>; otherwise, false.
	 * 
	 * @param obj The <see cref="T:System.Object"/> to compare with the current
	 *            <see cref="T:System.Object"/>.
	 * @exception T:System.NullReferenceException The <paramref name="obj"/>
	 *                                            parameter is null.
	 *                                            <filterpriority>2</filterpriority>
	 */
	@Override
	public boolean equals(Object obj) {
		NetworkConnectionOptions otherObject = obj instanceof NetworkConnectionOptions ? (NetworkConnectionOptions) obj
				: null;

		return equals(otherObject); // Just have type-specific Equals do the check (it even handles null)
	}

	/**
	 * Determines if the provided NetworkConnectionOptions object is identical to
	 * this object.
	 * 
	 * @param other The NetworkConnectionOptions object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(NetworkConnectionOptions other) {
		if (compareTo(other) == 0) {
			return true;
		}

		return false;
	}

	/**
	 * Provides a representative hash code for objects of this type to spread out
	 * distribution in hash tables.
	 * 
	 * Objects which consider themselves to be Equal (a.Equals(b) returns true) are
	 * expected to have the same hash code. Objects which are not Equal may have the
	 * same hash code, but minimizing such overlaps helps with efficient operation
	 * of hash tables.
	 * 
	 * @return An int representing the hash code calculated for the contents of this
	 *         object.
	 * 
	 */
	@Override
	public int hashCode() {
		if (this.hashCode == 0) {
			calculateHash();
		}
		return this.hashCode;
	}

	private void calculateHash() {
		int myHash = (new Boolean(getUseSsl())).hashCode();
		myHash ^= (new Integer(getPort())).hashCode();

		// since we are comparing without case we need to get rid of hash code
		// variations by case.
		if (getHostName() != null) {
			myHash ^= getHostName().toLowerCase(Locale.ROOT).hashCode(); // Fold in hash code for string
		}

		this.hashCode = myHash;
	}
}