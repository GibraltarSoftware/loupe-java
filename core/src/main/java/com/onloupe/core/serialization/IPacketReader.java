package com.onloupe.core.serialization;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface IPacketReader {
	/**
	 * Read and return the next IPacket from the stream
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IOException
	 */
	IPacket read() throws NoSuchMethodException, SecurityException, IOException, IllegalAccessException, InstantiationException, InvocationTargetException;

	void registerType(java.lang.Class type);

	void registerFactory(String typeName, IPacketFactory factory);
}