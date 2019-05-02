package com.onloupe.core.monitor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import com.onloupe.core.serialization.monitor.ApplicationUserPacket;
import com.onloupe.core.util.TypeUtils;

/**
 * Extended information for a single application user
 */
public class ApplicationUser implements Comparable<ApplicationUser> {
	private ApplicationUserPacket packet;

	public ApplicationUser(String userName, OffsetDateTime timestamp, long sequence) {
		this.packet = new ApplicationUserPacket();
		this.packet.setTimestamp(timestamp);
		this.packet.setSequence(sequence);
		this.packet.setFullyQualifiedUserName(userName);
	}

	public ApplicationUser(ApplicationUserPacket packet) {
		this.packet = packet;
	}

	public final ApplicationUserPacket getPacket() {
		return this.packet;
	}

	/**
	 * The unique id of this application user in this session
	 */
	public final UUID getId() {
		return this.packet.getID();
	}

	/**
	 * Optional. An absolute, unique key for the user to use as a primary match
	 */
	public final String getKey() {
		return this.packet.getKey();
	}

	public final void setKey(String value) {
		this.packet.setKey(value);
	}

	/**
	 * The fully qualified user name
	 * 
	 * If Key isn't specified this value is used as the alternate key
	 */
	public final String getFullyQualifiedUserName() {
		return this.packet.getFullyQualifiedUserName();
	}

	/**
	 * A display label for the user (such as their full name)
	 */
	public final String getCaption() {
		return this.packet.getCaption();
	}

	public final void setCaption(String value) {
		this.packet.setCaption(value);
	}

	/**
	 * Optional. A title for the user (e.g. job title)
	 */
	public final String getTitle() {
		return this.packet.getTitle();
	}

	public final void setTitle(String value) {
		this.packet.setTitle(value);
	}

	/**
	 * Optional. A primary email address for the user
	 */
	public final String getEmailAddress() {
		return this.packet.getEmailAddress();
	}

	public final void setEmailAddress(String value) {
		this.packet.setEmailAddress(value);
	}

	/**
	 * Optional. A phone number or other telecommunication alias
	 */
	public final String getPhone() {
		return this.packet.getPhone();
	}

	public final void setPhone(String value) {
		this.packet.setPhone(value);
	}

	/**
	 * Optional. A label for the organization this user is a part of
	 */
	public final String getOrganization() {
		return this.packet.getOrganization();
	}

	public final void setOrganization(String value) {
		this.packet.setOrganization(value);
	}

	/**
	 * Optional. A primary role for this user with respect to this application
	 */
	public final String getRole() {
		return this.packet.getRole();
	}

	public final void setRole(String value) {
		this.packet.setRole(value);
	}

	/**
	 * Optional. The primary tenant this user is a part of.
	 */
	public final String getTenant() {
		return this.packet.getTenant();
	}

	public final void setTenant(String value) {
		this.packet.setTenant(value);
	}

	/**
	 * Optional. The time zone the user is associated with
	 */
	public final String getTimeZoneCode() {
		return this.packet.getTimeZoneCode();
	}

	public final void setTimeZoneCode(String value) {
		this.packet.setTimeZoneCode(value);
	}

	/**
	 * Application provided properties
	 */
	public final Map<String, String> getProperties() {
		return this.packet.getProperties();
	}

	/**
	 * Compares this ApplicationUser object to another to determine sorting order.
	 * 
	 * ApplicationUser instances are sorted by their Domain then User Name
	 * properties.
	 * 
	 * @param other The other ApplicationUser object to compare this object to.
	 * @return An int which is less than zero, equal to zero, or greater than zero
	 *         to reflect whether this ApplicationUser should sort as being
	 *         less-than, equal to, or greater-than the other ApplicationUser,
	 *         respectively.
	 */
	@Override
	public final int compareTo(ApplicationUser other) {
		if (other == null) {
			return 1; // We're not null, so we're greater than anything that is null.
		}

		if (this == other) {
			return 0; // Refers to the same instance, so obviously we're equal.
		}

		// we want to sort by the domain and user name, but we don't want to let things
		// be considered equal if they have a key missmatch..
		int compare = getFullyQualifiedUserName().compareToIgnoreCase(other.getFullyQualifiedUserName());

		if ((compare == 0) && (TypeUtils.isBlank(getKey()))) {
			compare = getKey().compareToIgnoreCase(other.getKey());
		}

		return compare;
	}

	/**
	 * Determines if the provided ApplicationUser object is identical to this
	 * object.
	 * 
	 * @param other The ApplicationUser object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(ApplicationUser other) {
		if (compareTo(other) == 0) {
			return true;
		}

		return false;
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param obj The object to compare this object to
	 * @return True if the other object is also a ApplicationUser and represents the
	 *         same data.
	 */
	@Override
	public boolean equals(Object obj) {
		ApplicationUser otherUser = obj instanceof ApplicationUser ? (ApplicationUser) obj : null;

		return compareTo(otherUser) == 0; // Just have type-specific Equals do the check (it even handles null)
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
		if (TypeUtils.isBlank(getKey())) {
			return getKey().hashCode();
		}

		int myHash = getFullyQualifiedUserName().hashCode();

		return myHash;
	}

}