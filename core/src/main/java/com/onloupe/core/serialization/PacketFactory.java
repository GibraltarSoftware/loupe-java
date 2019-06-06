package com.onloupe.core.serialization;

import java.util.HashMap;


/**
 * This helper class is used by PacketReader to manage the list of
 * IPacketFactory classes used to deserialize a stream of packets.
 */
public class PacketFactory {
	
	/** The packet factories. */
	private HashMap<String, IPacketFactory> packetFactories;
	
	/** The generic factory. */
	private GenericPacketFactory genericFactory;

	/**
	 * Creates an empty list of IPacketFactory objects.
	 */
	public PacketFactory() {
		this.packetFactories = new HashMap<String, IPacketFactory>();
		this.genericFactory = new GenericPacketFactory();
	}

	/**
	 * Registers a SimplePacketFactory wrappering the specified type.
	 * 
	 * @param type Type must implement IPacket.
	 */
	public final void registerType(java.lang.Class type) {
		SimplePacketFactory factory = new SimplePacketFactory(type);
		if (factory.isValid()) {
			registerFactory(type.getSimpleName(), factory);
		}
	}

	/**
	 * Associates the specified IPacketFactory with a type name.
	 *
	 * @param typeName Should refer to a type that implements IPacket
	 * @param factory  IPacketFactory class used to
	 */
	public final void registerFactory(String typeName, IPacketFactory factory) {
		this.packetFactories.put(typeName, factory);
	}

	/**
	 * Gets the packet factory.
	 *
	 * @param typeName the type name
	 * @return the packet factory
	 */
	public final IPacketFactory getPacketFactory(String typeName) {
		if (this.packetFactories.containsKey(typeName)) {
			return this.packetFactories.get(typeName);
		}

		return this.genericFactory;
	}
}