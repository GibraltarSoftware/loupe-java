package com.onloupe.core.serialization;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

// TODO: Auto-generated Javadoc
/**
 * Defines the interface necessary for a packet factory to be registered with
 * IPacketReader.
 */
public interface IPacketFactory {
	
	/**
	 * This is the method that is invoked on an IPacketFactory to create an IPacket
	 * from the data in an IFieldReader given a specified PacketDefinition.
	 *
	 * @param definition Definition of the fields expected in the next packet
	 * @param reader     Data stream to be read
	 * @return An IPacket corresponding to the PacketDefinition and the stream data
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws InstantiationException the instantiation exception
	 */
	IPacket createPacket(PacketDefinition definition, IFieldReader reader) throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException;
}