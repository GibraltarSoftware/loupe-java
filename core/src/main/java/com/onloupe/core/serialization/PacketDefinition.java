package com.onloupe.core.serialization;

import com.onloupe.model.exception.GibraltarException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Holds the metadata needed to correctly interpret the stream of fields
 * associated with a serialized packet
 */
public final class PacketDefinition implements java.lang.Iterable<FieldDefinition> {
	/**
	 * Create a PacketDefinition describing the fields and serialization version
	 * information for the IPacket object passed by the caller.
	 * 
	 * @param packet IPacket object to generate a PacketDefinition for
	 * @return PacketDefinition describing fields to be serialized, including nested
	 *         types
	 */
	public static PacketDefinition createPacketDefinition(IPacket packet) {
		//first we have to create a matching linked set of definitions for all of the types
		//from the outermost down to the lowest that implements IPacket.
		PacketDefinition definition = calculateDefinitions(packet);

		//unlike the .NET implementation which explicitly invokes getPackageDefinition
		//at each level of the object hierarchy we will use more traditional inheritance
		PacketDefinition outermostIPacketDefinition = (definition.writeMethod == null) ? definition.getParentIPacket() : definition;
		packet.writePacketDefinition(outermostIPacketDefinition);

		// Check if this is a DynamicPacket. If so, it should have a unique dynamic
		// type.
		// If the DynamicType field has not been assigned, assign a unique string.
		IDynamicPacket dynamicPacket = packet instanceof IDynamicPacket ? (IDynamicPacket) packet : null;
		if (dynamicPacket != null) {
			if (dynamicPacket.getDynamicTypeName() == null) {
				dynamicPacket.setDynamicTypeName(UUID.randomUUID().toString());
			}
			definition.setDynamicTypeName(dynamicPacket.getDynamicTypeName());
		}

		// Record whether or not this is a cachable packet.
		definition.setIsCacheable(packet instanceof ICachedPacket);

		return definition;
	}

	private static PacketDefinition calculateDefinitions(IPacket packet) {
		// We iterate from the type we are passed down the object hierarchy looking for
		// IPacket implementations. Then, on the way back up, we link together
		// the _BasePacket fields to that the PacketDefinition we return includes
		// a description of all the nested types.
		Stack<PacketDefinition> stack = new Stack<PacketDefinition>();
		Class<? extends IPacket> type = packet.getClass();

		// walk down the hierarchy till we get to a base object that no longer
		// implements IPacket
		while (IPacket.class.isAssignableFrom(type)) {
			// We push one PacketDefinition on the stack for each level in the hierarchy
			PacketDefinition definition = null;

			// Even though the current type implements IPacket, it may not have a
			// writePacketDefinition at this level
			Method method = getIPacketMethod(type, "writePacketDefinition", new Class[]{PacketDefinition.class});
			if (method != null) {
				definition = new PacketDefinition(type.getSimpleName());
				definition.implementsIPacket = true;
				definition.writeMethod = getIPacketMethod(type, "writeFields",
						new java.lang.Class[] { PacketDefinition.class, SerializedPacket.class });
				if (definition.writeMethod == null) {
					throw new GibraltarSerializationException(
							"The current packet implements part but not all of the IPacket interface.  No Write Method could be found.  Did you implement IPacket explicitly?");
				}
			} else {
				// If GetPacketDefinition isn't defined at this level,
				// push an empty PacketDefinition on the stack as a placeholder
				definition = new PacketDefinition(type.getSimpleName(), -1);
			}

			// Push the PacketDefinition for this level on the stack
			// then iterate down to the next deeper level in the object hierarchy
			stack.push(definition);
			type = (Class<? extends IPacket>) type.getSuperclass();
		}

		// At this point the top of the stack contains the mostly deeply nested base
		// type.
		// While there are 2 or more elements on the stack, the deeper of the two
		// should reference the top element as a base type
		while (stack.size() >= 2) {
			// Pop off the deepest base type still in the stack
			PacketDefinition basePacket = stack.pop();

			// The next element is now visible, so let's peek at it
			PacketDefinition derivedPacket = stack.peek();

			// link the base type with its derived class
			derivedPacket.parentPacket = basePacket;
		}

		// At this point there should be exactly one element in the stack
		// which contains the return value for this method.
		PacketDefinition packetDefinition = stack.pop();
		return packetDefinition;
	}

	/**
	 * Returns a PacketDefinition from the stream (including nested PacketDefinition
	 * objects for cases in which an IPacket is subclassed and has serialized state
	 * at multiple levels).
	 * 
	 * @param reader Stream to read data from
	 * @return PacketDefinition (including nested definitions for subclassed
	 *         packets)
	 * @throws IOException
	 */
	public static PacketDefinition readPacketDefinition(IFieldReader reader) throws IOException {
		boolean cachedPacket = reader.readBool();
		int nestingDepth = reader.readInt();
		if (nestingDepth < 1) {
			throw new GibraltarException(String.format(
					"While reading the definition of the next packet, the number of types in the definition was read as %1$s which is less than 1.",
					nestingDepth));
		}

		String dynamicTypeName = reader.readString();
		PacketDefinition[] definitions = new PacketDefinition[nestingDepth];
		for (int i = 0; i < nestingDepth; i++) {
			definitions[i] = new PacketDefinition(reader);
			if (i > 0) {
				definitions[i].parentPacket = definitions[i - 1];
			}
		}

		PacketDefinition topLevelDefinition = definitions[nestingDepth - 1];
		topLevelDefinition.cachable = cachedPacket;
		topLevelDefinition.dynamicTypeName = dynamicTypeName;
		return topLevelDefinition;
	}

	private boolean implementsIPacket;
	private boolean cachable;
	private String typeName;
	private int version;
	private String dynamicTypeName;
	private final FieldDefinitionCollection fields = new FieldDefinitionCollection();
	private PacketDefinition parentPacket;
	private java.lang.reflect.Method writeMethod;
	private java.lang.reflect.Method readMethod;
	private boolean readMethodAssigned;
	private List<PacketDefinition> subPackets;
	private int packetCount;

	public void setDynamicTypeName(String value) { this.dynamicTypeName = value; }

	public int getPacketCount() {
		return this.packetCount;
	}

	public void setPacketCount(int value) {
		this.packetCount = value;
	}

	private long packetSize;

	public long getPacketSize() {
		return this.packetSize;
	}

	public void setPacketSize(long value) {
		this.packetSize = value;
	}

	private PacketDefinition(IFieldReader reader) throws IOException {
		this.typeName = reader.readString();
		this.version = reader.readInt();
		int fieldCount = reader.readInt();
		for (int i = 0; i < fieldCount; i++) {
			String fieldName = reader.readString();
			FieldType fieldType = FieldType.forValue(reader.readInt());
			this.fields.add(new FieldDefinition(fieldName, fieldType));
		}

		// Handle the possibility that a Packet aggregates lower level packets
		int subPacketCount = reader.readInt();
		this.subPackets = new ArrayList<PacketDefinition>();
		for (int i = 0; i < subPacketCount; i++) {
			// We need to call the static ReadPacketDefinition(reader) in order to
			// read and process the cacheable, version, and dynamic name fields which
			// also exist for each subPacket definition. Fixed as part of Case #165
			PacketDefinition subPacket = readPacketDefinition(reader);
			this.subPackets.add(subPacket);
		}

		//we detect if we implement IPacket the first time we analyze a specific packet to deserialize so we have the type information.
	}

	/**
	 * Create a packet definition, initialized for a type that doesn't support writing any fields.
	 * @param typeName
	 */
	public PacketDefinition(String typeName) {
		this(typeName, -1,  null);
	}

	public PacketDefinition(String typeName, int version) {
		this(typeName, version, null);
	}

	public PacketDefinition(String typeName, int version, PacketDefinition baseDefinition) {
		this.typeName = typeName;
		this.version = version;
		this.parentPacket = baseDefinition;
		this.subPackets = new ArrayList<PacketDefinition>();
	}


	public boolean getImplementsIPacket() { return this.implementsIPacket; }

	public boolean isCachable() {
		return this.cachable;
	}

	public void setIsCacheable(boolean value){ this.cachable = value; }

	public String getTypeName() {
		return this.typeName;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int value) { this.version = value; }

	public String getQualifiedTypeName() {
		if (this.dynamicTypeName == null) {
			return this.typeName;
		} else {
			return this.typeName + "+" + this.dynamicTypeName;
		}
	}

	public FieldDefinitionCollection getFields() {
		return this.fields;
	}

	/**
	 * This list allows for the possiblity of a Packet that aggregates other
	 * sub-packets
	 */
	public List<PacketDefinition> getSubPackets() {
		return this.subPackets;
	}

	public void writeDefinition(IFieldWriter writer) throws IOException {
		writer.write(this.cachable);
		int nestingDepth = getNestingDepth();
		writer.write(nestingDepth);
		writer.write(this.dynamicTypeName);
		writeDefinitionForThisLevel(writer);
	}

	public int getNestingDepth() {
		if (this.parentPacket == null) {
			return 1;
		} else {
			return 1 + this.parentPacket.getNestingDepth();
		}
	}

	/**
	 * Get the definition of the next super type that implements IPacket
	 * @return
	 */
	public PacketDefinition getParentIPacket() {
		PacketDefinition parentIPacket = parentPacket;
		while (parentIPacket != null && !parentIPacket.getImplementsIPacket()) {
			parentIPacket = parentIPacket.parentPacket;
		}

		return parentIPacket;
	}

	/**
	 * Get the definition of the next super type, regardless of whether it implements IPacket or not.
	 * @return
	 */
	public PacketDefinition getParentPacket() {
		return this.parentPacket;
	}

	private void writeDefinitionForThisLevel(IFieldWriter writer) throws IOException {
		if (this.parentPacket != null) {
			this.parentPacket.writeDefinitionForThisLevel(writer);
		}

		writer.write(this.typeName);
		writer.write(this.version);
		writer.write(this.fields.size());

		Iterator<FieldDefinition> fieldDefinitions = this.fields.iterator();
		while (fieldDefinitions.hasNext()) {
			FieldDefinition definition = fieldDefinitions.next();
			writer.write(definition.getName());
			writer.write(definition.getFieldType().getValue());
		}

		// Writer out any associated sub-packets
		writer.write(this.subPackets.size());
		for (int i = 0; i < this.subPackets.size(); i++) {
			this.subPackets.get(i).writeDefinition(writer);
		}
	}

	/**
	 * Get the lossless equivalent type for serialization
	 * 
	 * @param type A .NET type to serialize
	 * @return The Field Type that will provide lossless serialization If no
	 *         lossless type is found, an exception will be thrown.
	 */
	public static FieldType getSerializableType(java.lang.Class type) {
		FieldType bestType = tryGetSerializableType(type);
		if (bestType == FieldType.UNKNOWN) {
			throw new IndexOutOfBoundsException("The provided type isn't supported for lossless serialization.");
		}
		return bestType;
	}

	/**
	 * Get the lossless equivalent type for serialization
	 * 
	 * @param type     A .NET type to serialize
	 * @return The optimal field type for the provided type, or UNKNOWN if none could
	 *         be determined.
	 */
	public static FieldType tryGetSerializableType(java.lang.Class type) {
		FieldType bestType = FieldType.UNKNOWN;

		if (type == LocalDateTime.class) {
			bestType = FieldType.DATE_TIME;
		} else if (type == OffsetDateTime.class) {
			bestType = FieldType.DATE_TIME_OFFSET;
		} else if (type == Duration.class) {
			bestType = FieldType.DURATION;
		} else if (type == String.class) {
			bestType = FieldType.STRING;
		} else if (type == String[].class) {
			bestType = FieldType.STRING_ARRAY;
		} else if (type == Long.class) {
			bestType = FieldType.LONG;
		} else if (type == Integer.class) {
			bestType = FieldType.INT;
		} else if (type == Short.class) {
			bestType = FieldType.INT;
		} else if (type == Double.class) {
			bestType = FieldType.DOUBLE;
		} else if (type == BigDecimal.class) // Note: Does this cast to a double without loss?
		{
			bestType = FieldType.DOUBLE;
		} else if (type == Float.class) {
			bestType = FieldType.DOUBLE;
		} else if (type == Boolean.class) {
			bestType = FieldType.BOOL;
		} else if (type == UUID.class) {
			bestType = FieldType.GUID;
		} else {
			Class baseType = type.getSuperclass();
			if (baseType.isEnum() || baseType == Enum[].class) {
				bestType = tryGetSerializableType(baseType);
			}
		}

		return bestType;
	}

	/**
	 * Request the packet object write out all of its fields.
	 *
	 * @param packet
	 * @param writer
	 * @throws Exception
	 */
	public void writeFields(IPacket packet, IFieldWriter writer) throws Exception {
		if (packet instanceof GenericPacket) {
			// TODO: Update generic packet handling
			// packet.WriteFields(writer);
		} else {
			//Get all of the values we want to write out at each layer of the inheritance model into
			//a set of SerializedPacket objects.
			PacketDefinition outermostIPacketDefinition = (implementsIPacket) ? this : getParentIPacket();
			SerializedPacket serializedPacket = new SerializedPacket(outermostIPacketDefinition); //no point in starting on the most derived type if it isn't an IPacket.

			packet.writeFields(outermostIPacketDefinition, serializedPacket);

			outermostIPacketDefinition.writeToOutput(serializedPacket, writer);
		}
	}

	/**
	 * Write the serialized packet (and its super packets) to the provided field writer.
	 * @param serializedPacket
	 * @param writer
	 */
	protected void writeToOutput(SerializedPacket serializedPacket, IFieldWriter writer) throws Exception {
		// We need all of our base classes to write out before us
		SerializedPacket superPacket = serializedPacket.getParentIPacket();
		if (superPacket != null) {
			superPacket.getPacketDefinition().writeToOutput(superPacket, writer);
		}

		// and now write out our fields to serialization.
		for (int curFieldIndex = 0; curFieldIndex < fields.size(); curFieldIndex++) {
			FieldDefinition fieldDefinition = fields.get(curFieldIndex);
			writer.write(serializedPacket.getValues().get(curFieldIndex), fieldDefinition.getFieldType());
		}
	}

	public List<IPacket> getRequiredPackets(IPacket packet) throws Exception {
		List<IPacket> requiredPackets = packet.getRequiredPackets();

		//KM: Here we should dedupe the list of required packets if we're feeling pure, but it wont matter as a packet
		//will only be written out once per stream.

		//RK: deduplicate!
		return requiredPackets.stream().distinct().collect(Collectors.toList());
	}

	public void readFields(IPacket packet, IFieldReader reader) {
		IDynamicPacket dynamicPacket = packet instanceof IDynamicPacket ? (IDynamicPacket) packet : null;
		if (dynamicPacket != null) {
			dynamicPacket.setDynamicTypeName(this.dynamicTypeName);
		}

		//get the linked list of serialized packets for the data
		SerializedPacket serializedPacket = readSerializedPackets(packet.getClass(), this, packet, reader);

		//and then let the objects read through the data..
		PacketDefinition outermostIPacketDefinition = (implementsIPacket) ? this : getParentIPacket();
		packet.readFields(outermostIPacketDefinition, serializedPacket);
	}

	private static SerializedPacket readSerializedPackets(java.lang.Class type, PacketDefinition definition, IPacket packet, IFieldReader reader) {
		SerializedPacket superPacket = null;
		Exception basePacketException = null;
		if (definition.parentPacket != null) {
			try {
				superPacket = definition.parentPacket.readSerializedPackets(type.getSuperclass(), definition.getParentPacket(), packet, reader);
			} catch (Exception ex) {
				basePacketException = ex; // Remember this to wrap it in a new exception.
			}
		}

		if (!definition.readMethodAssigned) {
			if (IPacket.class.isAssignableFrom(type)) {
				// Even though the current type implements IPacket, it may not have a ReadFields
				// at this level
				definition.readMethod = getIPacketMethod(type, "readFields",
						new java.lang.Class[] { PacketDefinition.class, SerializedPacket.class });

				definition.implementsIPacket = (definition.readMethod != null);
			} else {
				definition.implementsIPacket = false;
			}

			definition.readMethodAssigned = true;
		}

		SerializedPacket serializedPacket;
		Exception firstException = null;
		FieldType firstFailedFieldType = FieldType.UNKNOWN;
		String firstFailedFieldName = null;
		if (definition.readMethod != null) {
			// we need to read back everything the definition says should be there into an
			// array and then pass that
			// to the object for handling.
			FieldDefinitionCollection fields = definition.getFields();
			Map<Integer, Object> values = new HashMap<Integer, Object>();

			for (int curFieldIndex = 0; curFieldIndex < fields.size(); curFieldIndex++) {
				FieldDefinition fieldDefinition = fields.get(curFieldIndex);
				try {
					values.put(curFieldIndex, reader.readField(fieldDefinition.getFieldType()));
				} catch (Exception ex) {
					if (basePacketException == null && firstException == null) {
						firstException = ex; // Only record the first one encountered in this packet.
						firstFailedFieldType = fieldDefinition.getFieldType();
						firstFailedFieldName = fieldDefinition.getName();
					}
				}
			}

			// Now check for exceptions we may have encountered. We had to finish reading
			// each field of each packet level
			// in order to keep the stream in sync, but now we have to throw a wrapping
			// exception if there was an error.
			String message;
			if (basePacketException != null) // This happened earlier, so it takes precedence over field exceptions.
			{
				message = String.format("Error reading base %1$s of a %2$s", definition.parentPacket.getQualifiedTypeName(),
						definition.getQualifiedTypeName());
				throw new GibraltarSerializationException(message, basePacketException);
			}
			if (firstException != null) // Otherwise we can report our first exception from reading fields.
			{
				message = String.format("Error reading (%1$s) field \"%2$s\" in a %3$s", firstFailedFieldType,
						firstFailedFieldName, definition.getQualifiedTypeName());
				throw new GibraltarSerializationException(message, firstException);
			}

			serializedPacket = new SerializedPacket(definition, values, superPacket);
		} else {
			//we have nothing at our level, just return our super level.
			serializedPacket = superPacket;
		}

		return serializedPacket;
	}

	/**
	 * Find the method only if it is explicitly declared at this level of the type
	 * hierarchy.
	 * 
	 * @param type
	 * @param methodName
	 * @param methodArgTypes
	 * @return The method found or null if it wasn't found.
	 */
	public static Method getIPacketMethod(java.lang.Class type, String methodName, java.lang.Class[] methodArgTypes) {
		try {
			return (methodArgTypes != null) ? type.getDeclaredMethod(methodName, methodArgTypes)
					: type.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	@Override
	public Iterator<FieldDefinition> iterator() {
		return this.fields.iterator();
	}

	/**
	 * Compare this PacketDefinition to another to verify that they are equivalent
	 * for purposes of order-dependant field deserialization.
	 */
	public boolean equals(PacketDefinition other) {
		// Verify that base packets are equivalent
		if (getParentPacket() == null) {
			if (other.getParentPacket() != null) {
				return false;
			}
		} else {
			if (other.getParentPacket() == null) {
				return false;
			}
			if (!getParentPacket().equals(other.getParentPacket())) {
				return false;
			}
		}

		// Verify that basic characteristics are equivalent
		if (!getTypeName().equals(other.getTypeName())) {
			return false;
		}
		if (getVersion() != other.getVersion()) {
			return false;
		}
		if (getFields().size() != other.getFields().size()) {
			return false;
		}

		// Verify that all fields are equivalent
		for (int i = 0; i < getFields().size(); i++) {
			if (getFields().get(i).getName() != other.getFields().get(i).getName()) {
				return false;
			}
			if (getFields().get(i).getFieldType() != other.getFields().get(i).getFieldType()) {
				return false;
			}
		}

		return true;
	}
}