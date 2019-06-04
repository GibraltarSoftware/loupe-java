package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.model.data.ProcessorArchitecture;

// TODO: Auto-generated Javadoc
/**
 * The Class AssemblyInfoPacket.
 */
public class AssemblyInfoPacket extends GibraltarCachedPacket implements IPacket {

	/** The Constant SERIALIZATION_VERSION. */
	private static final int SERIALIZATION_VERSION = 1;

    /** The culture name. */
    private String cultureName;
    
    /** The full name. */
    private String fullName;
    
    /** The image runtime version. */
    private String imageRuntimeVersion;
    
    /** The global assembly cache. */
    private boolean globalAssemblyCache;
    
    /** The location. */
    private String location;
    
    /** The name. */
    private String name;
    
    /** The processor architecture. */
    private ProcessorArchitecture processorArchitecture = ProcessorArchitecture.UNKNOWN;
    
    /** The version. */
    private String version;
    
    /** The file version. */
    private String fileVersion;
    
    /**
     * Instantiates a new assembly info packet.
     *
     * @param path the path
     * @param includeLocation the include location
     * @param nativeLibrary the native library
     */
    public AssemblyInfoPacket(Path path, boolean includeLocation, boolean nativeLibrary) {
    	super(true);
    	this.fullName = path.toAbsolutePath().toString();
    	this.location = includeLocation ? path.getParent().toAbsolutePath().toString() : "<private>";
    	this.name = path.toFile().isDirectory() ? "<directory>" : path.getFileName().toString();
    	this.cultureName = Locale.getDefault().getDisplayName();
    	this.globalAssemblyCache = nativeLibrary;
    }
    
	/**
	 * Instantiates a new assembly info packet.
	 */
	protected AssemblyInfoPacket() {
		super(true);
	}
	
	/**
	 * Gets the culture name.
	 *
	 * @return the culture name
	 */
	public String getCultureName() {
		return cultureName;
	}

	/**
	 * Sets the culture name.
	 *
	 * @param cultureName the new culture name
	 */
	public void setCultureName(String cultureName) {
		this.cultureName = cultureName;
	}

	/**
	 * Gets the full name.
	 *
	 * @return the full name
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Sets the full name.
	 *
	 * @param fullName the new full name
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * Gets the image runtime version.
	 *
	 * @return the image runtime version
	 */
	public String getImageRuntimeVersion() {
		return imageRuntimeVersion;
	}

	/**
	 * Sets the image runtime version.
	 *
	 * @param imageRuntimeVersion the new image runtime version
	 */
	public void setImageRuntimeVersion(String imageRuntimeVersion) {
		this.imageRuntimeVersion = imageRuntimeVersion;
	}

	/**
	 * Checks if is global assembly cache.
	 *
	 * @return true, if is global assembly cache
	 */
	public boolean isGlobalAssemblyCache() {
		return globalAssemblyCache;
	}

	/**
	 * Sets the global assembly cache.
	 *
	 * @param globalAssemblyCache the new global assembly cache
	 */
	public void setGlobalAssemblyCache(boolean globalAssemblyCache) {
		this.globalAssemblyCache = globalAssemblyCache;
	}

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Sets the location.
	 *
	 * @param location the new location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the processor architecture.
	 *
	 * @return the processor architecture
	 */
	public ProcessorArchitecture getProcessorArchitecture() {
		return processorArchitecture;
	}

	/**
	 * Sets the processor architecture.
	 *
	 * @param processorArchitecture the new processor architecture
	 */
	public void setProcessorArchitecture(ProcessorArchitecture processorArchitecture) {
		this.processorArchitecture = processorArchitecture;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version.
	 *
	 * @param version the new version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Gets the file version.
	 *
	 * @return the file version
	 */
	public String getFileVersion() {
		return fileVersion;
	}

	/**
	 * Sets the file version.
	 *
	 * @param fileVersion the new file version
	 */
	public void setFileVersion(String fileVersion) {
		this.fileVersion = fileVersion;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#getRequiredPackets()
	 */
	@Override
	public List<IPacket> getRequiredPackets() {
		return super.getRequiredPackets();
	}
	
	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());
		definition.setVersion(SERIALIZATION_VERSION);
		
        definition.getFields().add("FullName", FieldType.STRING);
        definition.getFields().add("Name", FieldType.STRING);
        definition.getFields().add("Version", FieldType.STRING);
        definition.getFields().add("CultureName", FieldType.STRING);
        definition.getFields().add("ProcessorArchitecture", FieldType.INT);
        definition.getFields().add("GlobalAssemblyCache", FieldType.BOOL);
        definition.getFields().add("Location", FieldType.STRING);
        definition.getFields().add("FileVersion", FieldType.STRING);
        definition.getFields().add("ImageRuntimeVersion", FieldType.STRING);
	}
	
	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());
        packet.setField("FullName", fullName);
        packet.setField("Name", name);
        packet.setField("Version", version);
        packet.setField("CultureName", cultureName);
        packet.setField("ProcessorArchitecture", processorArchitecture != null ? processorArchitecture.getValue() : null);
        packet.setField("GlobalAssemblyCache", globalAssemblyCache);
        packet.setField("Location", location);
        packet.setField("FileVersion", fileVersion);
        packet.setField("ImageRuntimeVersion", imageRuntimeVersion);
   	}

}
