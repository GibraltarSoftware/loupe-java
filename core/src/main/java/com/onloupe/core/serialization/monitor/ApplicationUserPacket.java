package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.core.util.TypeUtils;

import java.util.HashMap;


/**
 * The Class ApplicationUserPacket.
 */
public class ApplicationUserPacket extends GibraltarCachedPacket implements IPacket {
	
	/** The key. */
	private String key;
	
	/** The fully qualified user name. */
	private String fullyQualifiedUserName;
	
	/** The caption. */
	private String caption;
	
	/** The title. */
	private String title;
	
	/** The organization. */
	private String organization;
	
	/** The role. */
	private String role;
	
	/** The tenant. */
	private String tenant;
	
	/** The time zone code. */
	private String timeZoneCode;
	
	/** The email address. */
	private String emailAddress;
	
	/** The phone. */
	private String phone;

	/** The Properties. */
	private final HashMap<String, String> _Properties = new HashMap<>();

	/**
	 * Instantiates a new application user packet.
	 */
	public ApplicationUserPacket() {
		super(false);
	}

	/**
	 * Optional. An absolute, unique key for the user to use as a primary match
	 *
	 * @return the key
	 */
	public final String getKey() {
		return this.key;
	}

	/**
	 * Sets the key.
	 *
	 * @param value the new key
	 */
	public final void setKey(String value) {
		this.key = value;
	}

	/**
	 * The fully qualified user name, composed from the Domain and Name as
	 * originally specified.
	 *
	 * @return the fully qualified user name
	 */
	public final String getFullyQualifiedUserName() {
		return this.fullyQualifiedUserName;
	}

	/**
	 * Sets the fully qualified user name.
	 *
	 * @param value the new fully qualified user name
	 */
	public final void setFullyQualifiedUserName(String value) {
		this.fullyQualifiedUserName = value;
	}

	/**
	 * A display label for the user (such as their full name).
	 *
	 * @return the caption
	 */
	public final String getCaption() {
		return this.caption;
	}

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	public final void setCaption(String value) {
		this.caption = value;
	}

	/**
	 * Optional. A primary email address for the user
	 *
	 * @return the email address
	 */
	public final String getEmailAddress() {
		return this.emailAddress;
	}

	/**
	 * Sets the email address.
	 *
	 * @param value the new email address
	 */
	public final void setEmailAddress(String value) {
		this.emailAddress = value;
	}

	/**
	 * Optional. A phone number or other telecommunication alias
	 *
	 * @return the phone
	 */
	public final String getPhone() {
		return this.phone;
	}

	/**
	 * Sets the phone.
	 *
	 * @param value the new phone
	 */
	public final void setPhone(String value) {
		this.phone = value;
	}

	/**
	 * Optional. A label for the organization this user is a part of
	 *
	 * @return the organization
	 */
	public final String getOrganization() {
		return this.organization;
	}

	/**
	 * Sets the organization.
	 *
	 * @param value the new organization
	 */
	public final void setOrganization(String value) {
		this.organization = value;
	}

	/**
	 * Optional. The primary time zone the user is associated with.
	 *
	 * @return the time zone code
	 */
	public final String getTimeZoneCode() {
		return this.timeZoneCode;
	}

	/**
	 * Sets the time zone code.
	 *
	 * @param value the new time zone code
	 */
	public final void setTimeZoneCode(String value) {
		this.timeZoneCode = value;
	}

	/**
	 * Optional. A title to display for the user
	 *
	 * @return the title
	 */
	public final String getTitle() {
		return this.title;
	}

	/**
	 * Sets the title.
	 *
	 * @param value the new title
	 */
	public final void setTitle(String value) {
		this.title = value;
	}

	/**
	 * Optional. A primary role for this user with respect to this application
	 *
	 * @return the role
	 */
	public final String getRole() {
		return this.role;
	}

	/**
	 * Sets the role.
	 *
	 * @param value the new role
	 */
	public final void setRole(String value) {
		this.role = value;
	}

	/**
	 * Optional. The primary tenant this user is a part of.
	 *
	 * @return the tenant
	 */
	public final String getTenant() {
		return this.tenant;
	}

	/**
	 * Sets the tenant.
	 *
	 * @param value the new tenant
	 */
	public final void setTenant(String value) {
		this.tenant = value;
	}

	/**
	 * Application provided properties.
	 *
	 * @return the properties
	 */
	public final HashMap<String, String> getProperties() {
		return this._Properties;
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the 
	 *         parameter; otherwise, false.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(other instanceof ThreadInfoPacket ? (ThreadInfoPacket) other : null);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the 
	 *         parameter; otherwise, false.
	 */
	public final boolean equals(ApplicationUserPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		if (TypeUtils.isNotBlank(getKey()) && getKey().equalsIgnoreCase(other.getKey())) {
			return true;
		}

		if (getFullyQualifiedUserName().equalsIgnoreCase(other.getFullyQualifiedUserName())) {
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
	 * @return an int representing the hash code calculated for the contents of this
	 *         object
	 * 
	 */
	@Override
	public int hashCode() {
		// we're being a bit more strict about hash code to match our equality compare
		// exactly

		if (TypeUtils.isNotBlank(getKey())) {
			return getKey().hashCode();
		}

		if (TypeUtils.isNotBlank(getFullyQualifiedUserName())) {
			return getFullyQualifiedUserName().hashCode();
		}

		return super.hashCode();
	}

	/** The Constant SERIALIZATION_VERSION. */
	private static final int SERIALIZATION_VERSION = 1;

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("Key", FieldType.STRING);
		definition.getFields().add("UserName", FieldType.STRING);
		definition.getFields().add("Caption", FieldType.STRING);
		definition.getFields().add("Title", FieldType.STRING);
		definition.getFields().add("Organization", FieldType.STRING);
		definition.getFields().add("Role", FieldType.STRING);
		definition.getFields().add("Tenant", FieldType.STRING);
		definition.getFields().add("TimeZoneCode", FieldType.STRING);
		definition.getFields().add("EmailAddress", FieldType.STRING);
		definition.getFields().add("Phone", FieldType.STRING);

		// serialize our name/value pairs as parallel arrays
		definition.getFields().add("PropertyNames", FieldType.STRING_ARRAY);
		definition.getFields().add("PropertyValues", FieldType.STRING_ARRAY);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("Key", getKey());
		packet.setField("UserName", getFullyQualifiedUserName());
		packet.setField("Caption", getCaption());
		packet.setField("Title", getTitle());
		packet.setField("Organization", getOrganization());
		packet.setField("Role", getRole());
		packet.setField("Tenant", getTenant());
		packet.setField("TimeZoneCode", getTimeZoneCode());
		packet.setField("EmailAddress", getEmailAddress());
		packet.setField("Phone", getPhone());
		packet.setField("PropertyNames", this._Properties.keySet());
		packet.setField("PropertyValues", this._Properties.values());
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}
}