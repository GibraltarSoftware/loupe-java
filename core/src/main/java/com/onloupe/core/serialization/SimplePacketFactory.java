package com.onloupe.core.serialization;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * SimplePacketFactory is the IPacketFactory used when an IPacket implementation
 * knows how to use when a type
 */
public class SimplePacketFactory implements IPacketFactory {
	private java.lang.reflect.Constructor constructor;

	/**
	 * Creates an IPacketFactory wrappering a type that implements IPacket.
	 * 
	 * @param type The type must implement IPacket and provide a default constructor
	 */
	@SuppressWarnings("rawtypes")
	public SimplePacketFactory(java.lang.Class type) {
		if (!IPacket.class.isAssignableFrom(type)) {
			return;
		}

		// the type must provide a default constructor, but this constructor can be
		// private if it should not be called directly (other than during
		// deserialization)
		this.constructor = Arrays.asList(type.getConstructors()).stream()
				.filter(constructor -> constructor.getParameterCount() < 1).findFirst().orElse(null);
		if (this.constructor == null) {
			return;
		}
	}

	/**
	 * This method is used by caller to detect if the constructor failed. This is
	 * necessary because we suppress exceptions in release builds.
	 */
	public final boolean isValid() {
		return this.constructor != null;
	}

	/**
	 * This is the method that is invoked on an IPacketFactory to create an IPacket
	 * from the data in an IFieldReader given a specified PacketDefinition.
	 * 
	 * @param definition Definition of the fields expected in the next packet
	 * @param reader     Data stream to be read
	 * @return An IPacket corresponding to the PacketDefinition and the stream data
	 */
	@Override
	public final IPacket createPacket(PacketDefinition definition, IFieldReader reader) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		IPacket packet = (IPacket) this.constructor.newInstance(new Object[0]);
		definition.readFields(packet, reader);
		return packet;
	}
}