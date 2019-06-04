package com.onloupe.core.messaging;

import com.onloupe.configuration.IMessengerConfiguration;

// TODO: Auto-generated Javadoc
/**
 * Implement this interface to be a packet sink for the messaging system.
 */
public interface IMessenger {
	
	/**
	 * A display caption for this messenger
	 * 
	 * End-user display caption for this messenger. Captions are typically not
	 * unique to a given instance of a messenger.
	 *
	 * @return the caption
	 */
	String getCaption();

	/**
	 * A display description for this messenger.
	 *
	 * @return the description
	 */
	String getDescription();

	/**
	 * Called by the publisher every time the configuration has been updated.
	 * 
	 * @param configuration The configuration block for this messenger
	 */
	void configurationUpdated(IMessengerConfiguration configuration);

	/**
	 * Initialize the messenger so it is ready to accept packets.
	 * 
	 * @param publisher     The publisher that owns the messenger
	 * @param configuration The configuration block for this messenger
	 */
	void initialize(Publisher publisher, IMessengerConfiguration configuration);

	/**
	 * A name for this messenger
	 * 
	 * The name is unique and specified by the publisher during initialization.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Write the provided packet to this messenger.
	 * 
	 * The packet may depend on other packets. If the messenger needs those packets
	 * they are available from the publisher's packet cache.
	 *
	 * @param packet       The packet to write through the messenger.
	 * @param writeThrough True if the information contained in packet should be
	 *                     committed synchronously, false if the messenger should
	 *                     use write caching (if available).
	 * @throws InterruptedException the interrupted exception
	 */
	void write(IMessengerPacket packet, boolean writeThrough) throws InterruptedException;
}