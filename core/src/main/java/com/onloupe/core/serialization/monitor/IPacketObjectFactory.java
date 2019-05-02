package com.onloupe.core.serialization.monitor;

/**
 * Implement to support derived object creation from serialized packets
 * 
 * 
 * Some objects, such as metrics, have abstract base classes that need to be
 * derived from to create useful features. To support third party developers
 * deriving new objects, this interface is used to allow a raw persistable
 * packet to specify the correct derived type of its associated data object.
 * 
 * <typeparam name="DataObjectType">The base object</typeparam>
 * <typeparam name="ParentObjectType">The base type of object that collects this
 * base object</typeparam>
 */
public interface IPacketObjectFactory<DataObjectType, ParentObjectType> {
	/**
	 * Called to create the wrapping data object for a packet object.
	 * 
	 * 
	 * For collected objects, the parent collection owner is provided in the
	 * optional parent section. Review specific usage documentation to know which
	 * format of this interface to implement for a given base data object. For
	 * example, when overriding MetricPacket you will have to implement one form,
	 * for MetricSamplePacket a different one.
	 * 
	 * @param optionalParent The object that will own the newly created data object
	 * @return
	 */
	DataObjectType getDataObject(ParentObjectType optionalParent);
}