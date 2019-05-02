package com.onloupe.core.serialization;

/**
 * Most packets have a static structure of fields that is the same for all
 * packet instances. But some packets are dynamic in that the number and type of
 * fields can vary across different packet instances. A great example of this is
 * EventMetricDefinitionPacket. Each event metric has a different set of fields.
 * So, in terms of caching PacketDefinition objects, each instance can be
 * thought of as a dynamic type. On the other hand, only a single PacketFactory
 * need be registered that should be invoked for all dynamic packets of that
 * base type.
 * 
 */
public interface IDynamicPacket extends IPacket {
	/**
	 * The consistent, unique type name for the packet
	 */
	String getDynamicTypeName();

	void setDynamicTypeName(String value);
}