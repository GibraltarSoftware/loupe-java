package com.onloupe.core.serialization;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

// TODO: Auto-generated Javadoc
/**
 * The Interface IPacketReader.
 */
public interface IPacketReader {
	
	/**
	 * Read and return the next IPacket from the stream.
	 *
	 * @return the i packet
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InstantiationException the instantiation exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	IPacket read() throws NoSuchMethodException, SecurityException, IOException, IllegalAccessException, InstantiationException, InvocationTargetException;

	/**
	 * Register type.
	 *
	 * @param type the type
	 */
	void registerType(java.lang.Class type);

	/**
	 * Register factory.
	 *
	 * @param typeName the type name
	 * @param factory the factory
	 */
	void registerFactory(String typeName, IPacketFactory factory);
}