package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.model.data.ProcessorArchitecture;

public class AssemblyInfoPacket extends GibraltarCachedPacket implements IPacket {

	private static final int SERIALIZATION_VERSION = 1;

    private String cultureName;
    private String fullName;
    private String imageRuntimeVersion;
    private boolean globalAssemblyCache;
    private String location;
    private String name;
    private ProcessorArchitecture processorArchitecture = ProcessorArchitecture.UNKNOWN;
    private String version;
    private String fileVersion;
    
    public AssemblyInfoPacket(Path path, boolean includeLocation, boolean nativeLibrary) {
    	super(true);
    	this.fullName = path.toAbsolutePath().toString();
    	this.location = includeLocation ? path.getParent().toAbsolutePath().toString() : "<private>";
    	this.name = path.toFile().isDirectory() ? "<directory>" : path.getFileName().toString();
    	this.cultureName = Locale.getDefault().getDisplayName();
    	this.globalAssemblyCache = nativeLibrary;
    }
    
	protected AssemblyInfoPacket() {
		super(true);
	}
	
	public String getCultureName() {
		return cultureName;
	}

	public void setCultureName(String cultureName) {
		this.cultureName = cultureName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getImageRuntimeVersion() {
		return imageRuntimeVersion;
	}

	public void setImageRuntimeVersion(String imageRuntimeVersion) {
		this.imageRuntimeVersion = imageRuntimeVersion;
	}

	public boolean isGlobalAssemblyCache() {
		return globalAssemblyCache;
	}

	public void setGlobalAssemblyCache(boolean globalAssemblyCache) {
		this.globalAssemblyCache = globalAssemblyCache;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProcessorArchitecture getProcessorArchitecture() {
		return processorArchitecture;
	}

	public void setProcessorArchitecture(ProcessorArchitecture processorArchitecture) {
		this.processorArchitecture = processorArchitecture;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFileVersion() {
		return fileVersion;
	}

	public void setFileVersion(String fileVersion) {
		this.fileVersion = fileVersion;
	}

	@Override
	public List<IPacket> getRequiredPackets() {
		return super.getRequiredPackets();
	}
	
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
