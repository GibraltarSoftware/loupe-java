package com.onloupe.core.serialization.monitor;

import com.onloupe.agent.IMessageSourceProvider;
import com.onloupe.core.messaging.Publisher;
import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.IThreadInfo;
import com.onloupe.model.data.IExceptionInfo;
import com.onloupe.model.log.ILogMessage;
import com.onloupe.model.log.LogMessageSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class LogMessagePacket.
 */
public class LogMessagePacket extends GibraltarPacket implements IPacket, ILogMessage {
	
	/** The id. */
	private UUID id;
	
	/** The severity. */
	private LogMessageSeverity severity;
	
	/** The log system. */
	private String logSystem; // The major log system it comes from, eg. "Log4net", "Trace", "Gibraltar",
								
								/** The category name. */
								// "ELF"
	private String categoryName; // The subsystem category, eg. the LoggerName from Log4Net
	
	/** The user name. */
	private String userName;
	
	/** The caption. */
	private String caption;
	
	/** The description. */
	private String description;
	
	/** The details. */
	private String details;
	
	/** The exception chain. */
	private IExceptionInfo[] exceptionChain;
	
	/** The method name. */
	private String methodName;
	
	/** The class name. */
	private String className;
	
	/** The file name. */
	private String fileName;
	
	/** The line number. */
	private int lineNumber;
	
	/** The thread index. */
	private long threadIndex; // The UNIQUE index assigned by Gibraltar.Agent to identify the thread.
	
	/** The thread id. */
	private long threadId; // The unique-at-any-one-time-but-not-for-the-whole-process-lifetime
							
							/** The suppress notification. */
							// ManagedThreadId from .NET.
	private boolean suppressNotification; // Read only, for now.

	/** The message. */
	// the following are generated fields and are not persisted
	private String message; // a concatenated caption & description for GLV.

	/**
	 * Instantiates a new log message packet.
	 */
	public LogMessagePacket() {
		// we aren't a cachable packet so we have our own GUID
		setId(UUID.randomUUID());
		this.suppressNotification = Publisher.queryThreadMustNotNotify();
	}

	/**
	 * Sets the source info.
	 *
	 * @param sourceProvider the new source info
	 */
	public final void setSourceInfo(IMessageSourceProvider sourceProvider) {
		if (sourceProvider != null) {
			// Note: Should we map null strings to empty strings here?
			setMethodName(sourceProvider.getMethodName());
			setClassName(sourceProvider.getClassName());
			setFileName(sourceProvider.getFileName());
			setLineNumber(sourceProvider.getLineNumber());
		}
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getId()
	 */
	@Override
	public final UUID getId() {
		return this.id;
	}

	/**
	 * Sets the id.
	 *
	 * @param value the new id
	 */
	private void setId(UUID value) {
		this.id = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getSeverity()
	 */
	@Override
	public final LogMessageSeverity getSeverity() {
		return this.severity;
	}

	/**
	 * Sets the severity.
	 *
	 * @param value the new severity
	 */
	public final void setSeverity(LogMessageSeverity value) {
		this.severity = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getLogSystem()
	 */
	@Override
	public final String getLogSystem() {
		return this.logSystem;
	}

	/**
	 * Sets the log system.
	 *
	 * @param value the new log system
	 */
	public final void setLogSystem(String value) {
		this.logSystem = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getCategoryName()
	 */
	@Override
	public final String getCategoryName() {
		return this.categoryName;
	}

	/**
	 * Sets the category name.
	 *
	 * @param value the new category name
	 */
	public final void setCategoryName(String value) {
		this.categoryName = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getUserName()
	 */
	@Override
	public final String getUserName() {
		return this.userName;
	}

	/**
	 * Sets the user name.
	 *
	 * @param value the new user name
	 */
	public final void setUserName(String value) {
		this.userName = value;
	}

	/**
	 * Optional. Extended user information related to this message
	 */
	private ApplicationUserPacket userPacket;

	/**
	 * Gets the user packet.
	 *
	 * @return the user packet
	 */
	public final ApplicationUserPacket getUserPacket() {
		return this.userPacket;
	}

	/**
	 * Sets the user packet.
	 *
	 * @param value the new user packet
	 */
	public final void setUserPacket(ApplicationUserPacket value) {
		this.userPacket = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getMethodName()
	 */
	@Override
	public final String getMethodName() {
		return this.methodName;
	}

	/**
	 * Sets the method name.
	 *
	 * @param value the new method name
	 */
	public final void setMethodName(String value) {
		this.methodName = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getClassName()
	 */
	@Override
	public final String getClassName() {
		return this.className;
	}

	/**
	 * Sets the class name.
	 *
	 * @param value the new class name
	 */
	public final void setClassName(String value) {
		this.className = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getFileName()
	 */
	@Override
	public final String getFileName() {
		return this.fileName;
	}

	/**
	 * Sets the file name.
	 *
	 * @param value the new file name
	 */
	public final void setFileName(String value) {
		this.fileName = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getLineNumber()
	 */
	@Override
	public final int getLineNumber() {
		return this.lineNumber;
	}

	/**
	 * Sets the line number.
	 *
	 * @param value the new line number
	 */
	public final void setLineNumber(int value) {
		this.lineNumber = value;
	}

	/**
	 * Gets the thread index.
	 *
	 * @return the thread index
	 */
	public long getThreadIndex() {
		return this.threadIndex;
	}

	/**
	 * Sets the thread index.
	 *
	 * @param _ThreadIndex the new thread index
	 */
	public void setThreadIndex(long _ThreadIndex) {
		this.threadIndex = _ThreadIndex;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getThreadId()
	 */
	@Override
	public long getThreadId() {
		return this.threadId;
	}

	/**
	 * Sets the thread id.
	 *
	 * @param _ThreadId the new thread id
	 */
	public void setThreadId(long _ThreadId) {
		this.threadId = _ThreadId;
	}

	/**
	 * The thread info packet for our Thread Id. Must be set for the packet to be
	 * written to a stream.
	 */
	private ThreadInfoPacket threadInfoPacket;

	/**
	 * Gets the thread info packet.
	 *
	 * @return the thread info packet
	 */
	public final ThreadInfoPacket getThreadInfoPacket() {
		return this.threadInfoPacket;
	}

	/**
	 * Sets the thread info packet.
	 *
	 * @param value the new thread info packet
	 */
	public final void setThreadInfoPacket(ThreadInfoPacket value) {
		this.threadInfoPacket = value;
	}

	/**
	 * Gets the thread info.
	 *
	 * @return the thread info
	 */
	public final IThreadInfo getThreadInfo() {
		return getThreadInfoPacket();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getDomainId()
	 */
	@Override
	public final int getDomainId() {
		return getThreadInfo().getDomainId();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getDomainName()
	 */
	@Override
	public final String getDomainName() {
		return getThreadInfo().getDomainName();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#isThreadPoolThread()
	 */
	@Override
	public final boolean isThreadPoolThread() {
		return getThreadInfo().isThreadPoolThread();
	}

	/**
	 * Indicates if the log message has related thread information. If false, some
	 * calls to thread information may throw exceptions.
	 *
	 * @return the checks for thread info
	 */
	@Override
	public final boolean getHasThreadInfo() {
		return getThreadInfoPacket() != null;
	}


	/**
	 * A combined caption &amp; description Added for GLV support.
	 *
	 * @return the message
	 */
	public final String getMessage() {
		if (this.message == null) // that's deliberate - null means not calculated, empty string means calculated
									// as empty.
		{
			boolean haveCaption = (TypeUtils.isNotBlank(this.caption));
			boolean haveDescription = (TypeUtils.isNotBlank(this.caption));

			if (haveCaption && haveDescription) {
				this.message = this.caption + "\r\n" + this.description;
			} else if (haveCaption) {
				this.message = this.caption;
			} else if (haveDescription) {
				this.message = this.description;
			} else {
				// use an empty string - it's empty. then we won't do this property check again.
				this.message = "";
			}
		}

		return this.message;
	}

	/**
	 * A display name for the thread, returning the thread Id if no name is
	 * available.
	 * 
	 * Added for GLV support
	 *
	 * @return the thread name
	 */
	@Override
	public final String getThreadName() {
		return (getThreadInfoPacket() != null) && (TypeUtils.isNotBlank(getThreadInfoPacket().getThreadName()))
				? getThreadInfoPacket().getThreadName()
				: String.valueOf(getThreadId());
	}

	/**
	 * Captures the provided exception immediately.
	 *
	 * @param throwable the new exception
	 */
	public final void setException(Throwable throwable) {
		this.exceptionChain = exceptionToArray(throwable); // this handles a null Exception, never returns null
	}

	/**
	 * Whether or not this log message includes attached Exception information.
	 *
	 * @return the checks for exception
	 */
	@Override
	public final boolean getHasException() {
		IExceptionInfo[] exceptionInfo = getExceptions();
		return ((exceptionInfo != null) && (exceptionInfo.length > 0));
	}

	/**
	 * Indicates if the class name and method name are available.
	 *
	 * @return the checks for method info
	 */
	@Override
	public final boolean getHasMethodInfo() {
		return !TypeUtils.isBlank(getClassName());
	}

	/**
	 * Indicates if the file name and line number are available.
	 *
	 * @return the checks for source location
	 */
	@Override
	public final boolean getHasSourceLocation() {
		return !TypeUtils.isBlank(getFileName());
	}

	/**
	 * Gets the exceptions.
	 *
	 * @return the exceptions
	 */
	public final IExceptionInfo[] getExceptions() {
		return this.exceptionChain;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.log.ILogMessage#getException()
	 */
	@Override
	public final IExceptionInfo getException() {
		IExceptionInfo[] exceptionInfo = getExceptions();
		if ((exceptionInfo == null) || (exceptionInfo.length == 0)) {
			return null;
		}

		return exceptionInfo[0];
	}

	/**
	 * Normalize the exception pointers to a single list.
	 *
	 * @param exception the exception
	 * @return the list
	 */
	public static List<IExceptionInfo> exceptionsList(IExceptionInfo exception) {
		ArrayList<IExceptionInfo> exceptions = new ArrayList<>();

		IExceptionInfo innerException = exception;
		while (innerException != null) {
			exceptions.add(innerException);
			innerException = innerException.getInnerException();
		}

		return exceptions;
	}

	/**
	 * A single line caption.
	 *
	 * @return the caption
	 */
	@Override
	public final String getCaption() {
		return this.caption;
	}

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	public final void setCaption(String value) {
		this.caption = value;

		// and clear our message so it'll get recalculated.
		this.message = null;
	}

	/**
	 * A multi line description.
	 *
	 * @return the description
	 */
	@Override
	public final String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description.
	 *
	 * @param value the new description
	 */
	public final void setDescription(String value) {
		this.description = value;

		// and clear our message so it'll get recalculated.
		this.message = null;
	}

	/**
	 * XML details for this log message.
	 *
	 * @return the details
	 */
	@Override
	public String getDetails() {
		return this.details;
	}

	/**
	 * Sets the details.
	 *
	 * @param value the new details
	 */
	public void setDetails(String value) {
		this.details = value;
	}

	/**
	 * True if the message was issued from a Notifier thread which needs to suppress
	 * notification about this message.
	 *
	 * @return the suppress notification
	 */
	public final boolean getSuppressNotification() {
		return this.suppressNotification;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%1$tc: %2$s", getTimestamp(), getCaption());
	}

	/**
	 * Compare to.
	 *
	 * @param other the other
	 * @return the int
	 */
	public final int compareTo(ILogMessage other) {
		// First do a quick match on Guid. this is the only case we want to return zero
		// (an exact match)
		if (getId().equals(other.getId())) {
			return 0;
		}

		// now we want to sort by our nice increasing sequence #
		int compareResult = Long.compare (getSequence(), other.getSequence());

		assert compareResult != 0; // no way we should ever get an equal at this point.

		return compareResult;
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(other instanceof LogMessagePacket ? (LogMessagePacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(LogMessagePacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return equals((ILogMessage) other);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(ILogMessage other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getId().equals(other.getId())) && (getCaption().equals(other.getCaption()))
				&& (getDescription().equals(other.getDescription())) && (getDetails().equals(other.getDetails()))
				&& (getSeverity() == other.getSeverity()) && (getLogSystem().equals(other.getLogSystem()))
				&& (getCategoryName().equals(other.getCategoryName())) && (getUserName().equals(other.getUserName()))
				&& (getMethodName().equals(other.getMethodName())) && (getClassName().equals(other.getClassName()))
				&& (getFileName().equals(other.getFileName())) && (getLineNumber() == other.getLineNumber())
				&& (getThreadId() == other.getThreadId()) && (super.equals(other)));
	}

	/**
	 * Provides a representative hash code for objects of this type to spread out
	 * distribution in hash tables.
	 * 
	 * Objects which consider themselves to be Equal (a.Equals(b) returns true) are
	 * expected to have the same hash code. Objects which are not Equal may have the
	 * same hash code, but minimizing such overlaps helps with efficient operation
	 * of hash tables.
	 * 
	 * @return an int representing the hash code calculated for the contents of this
	 *         object
	 * 
	 */
	@Override
	public int hashCode() {
		int myHash = super.hashCode(); // Fold in hash code for inherited base type

		myHash ^= getSeverity().getSeverity(); // Fold in Severity (enum) as an int as its own hash code
		myHash ^= getId().hashCode(); // Fold in hash code for GUID
		if (getCaption() != null) {
			myHash ^= getCaption().hashCode(); // Fold in hash code for string Caption
		}
		if (getDescription() != null) {
			myHash ^= getDescription().hashCode(); // Fold in hash code for string Caption
		}
		if (getDetails() != null) {
			myHash ^= getDetails().hashCode(); // Fold in hash code for string Caption
		}
		if (getLogSystem() != null) {
			myHash ^= getLogSystem().hashCode(); // Fold in hash code for string LogSystem
		}
		if (getCategoryName() != null) {
			myHash ^= getCategoryName().hashCode(); // Fold in hash code for string CategoryName
		}
		if (getUserName() != null) {
			myHash ^= getUserName().hashCode(); // Fold in hash code for string UserName
		}
		if (getMethodName() != null) {
			myHash ^= getMethodName().hashCode(); // Fold in hash code for string MethodName
		}
		if (getClassName() != null) {
			myHash ^= getClassName().hashCode(); // Fold in hash code for string ClassName
		}
		if (getFileName() != null) {
			myHash ^= getFileName().hashCode(); // Fold in hash code for string FileName
		}
		myHash ^= getLineNumber(); // Fold in LineNumber int as its own hash code
		myHash ^= getThreadId(); // Fold in ThreadId int as its own hash code

		return myHash;
	}

	/**
	 * The current serialization version
	 * 
	 * 
	 * <p>
	 * Version 2: Added Description and Details string fields.
	 * </p>
	 * <p>
	 * Added ThreadIndex field without bumping the version because old code would
	 * simply fail to accept data from new code.
	 * </p>
	 * 
	 */
	private static final int SERIALIZATION_VERSION = 3;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public final List<IPacket> getRequiredPackets() {
		List<IPacket> requiredPackets = super.getRequiredPackets();

		// we always depend on the Thread Info Packet;
		requiredPackets.add(getThreadInfoPacket());

		// we depend on the Application User packet if it's set (may not be)
		IPacket userPacket = getUserPacket();
		if (userPacket != null) requiredPackets.add(userPacket);

		return requiredPackets;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarPacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("ID", FieldType.GUID);
		definition.getFields().add("Caption", FieldType.STRING);
		definition.getFields().add("Severity", FieldType.INT);
		definition.getFields().add("LogSystem", FieldType.STRING);
		definition.getFields().add("CategoryName", FieldType.STRING);
		definition.getFields().add("UserName", FieldType.STRING);
		definition.getFields().add("Description", FieldType.STRING); // Added in version 2.
		definition.getFields().add("Details", FieldType.STRING); // Added in version 2.

		// ManagedThreadId isn't unique, so we need to add one that actually is (but not
		// bumping version).
		definition.getFields().add("ThreadIndex", FieldType.INT);

		//this is a long in java. The agent needs to be updated.
		definition.getFields().add("ThreadId", FieldType.INT);
		definition.getFields().add("MethodName", FieldType.STRING);
		definition.getFields().add("ClassName", FieldType.STRING);
		definition.getFields().add("FileName", FieldType.STRING);
		definition.getFields().add("LineNumber", FieldType.INT);

		// Now the Exception info, split into four arrays of strings to serialize
		// better.
		definition.getFields().add("TypeNames", FieldType.STRING_ARRAY);
		definition.getFields().add("Messages", FieldType.STRING_ARRAY);
		definition.getFields().add("Sources", FieldType.STRING_ARRAY);
		definition.getFields().add("StackTraces", FieldType.STRING_ARRAY);

		// Added in version 3
		definition.getFields().add("ApplicationUserId", FieldType.GUID);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarPacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		// We depend on the ThreadInfoPacket!
		assert getThreadInfoPacket() != null;
		assert getThreadInfoPacket().getThreadId() == getThreadId();

		packet.setField("ID", this.id);
		packet.setField("Caption", this.caption);
		packet.setField("Severity", this.severity.getSeverity());
		packet.setField("LogSystem", this.logSystem);
		packet.setField("CategoryName", this.categoryName);
		packet.setField("UserName", this.userName);
		packet.setField("Description", this.description);
		packet.setField("Details", this.details);

		packet.setField("ThreadIndex", Math.toIntExact(this.threadIndex));

		// These have been fully integrated here from the former CallInfoPacket
		packet.setField("ThreadId", Math.toIntExact(this.threadId));
		packet.setField("MethodName", this.methodName);
		packet.setField("ClassName", this.className);
		packet.setField("FileName", this.fileName);
		packet.setField("LineNumber", this.lineNumber);

		// Now the Exception info...

		// Because serialization supports single type arrays, it's most convenient
		// to reorganize our exceptions into parallel arrays of their base types
		IExceptionInfo[] exceptions = getExceptions(); // Get the array of ExceptionInfo
		int arrayLength = exceptions == null ? 0 : exceptions.length;
		String[] typeNames = new String[arrayLength];
		String[] messages = new String[arrayLength];
		String[] sources = new String[arrayLength];
		String[] stackTraces = new String[arrayLength];

		if (exceptions != null) {
			for (int i = 0; i < arrayLength; i++) {
				typeNames[i] = exceptions[i].getTypeName();
				messages[i] = exceptions[i].getMessage();
				sources[i] = exceptions[i].getSource();
				stackTraces[i] = exceptions[i].getStackTrace();
			}
		}

		packet.setField("TypeNames", typeNames);
		packet.setField("Messages", messages);
		packet.setField("Sources", sources);
		packet.setField("StackTraces", stackTraces);

		packet.setField("ApplicationUserId", (getUserPacket() == null) ? null : getUserPacket().getID());
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarPacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

	/**
	 * Exception to array.
	 *
	 * @param throwable the throwable
	 * @return the i exception info[]
	 */
	private static IExceptionInfo[] exceptionToArray(Throwable throwable) {
		// This must accept a null Exception and never return a null, use empty array;
		if (throwable == null) {
			return new IExceptionInfo[0];
		}

		int count = 1; // Otherwise, we have at least one
		Throwable innerException = throwable.getCause();
		while (innerException != null) // Count up how big to make the array
		{
			count++;
			innerException = innerException.getCause();
		}

		IExceptionInfo[] exceptions = new IExceptionInfo[count];

		// now start serializing them into the array...
		exceptions[0] = new ExceptionInfoPacket(throwable);

		innerException = throwable.getCause();
		int index = 0;
		while (innerException != null) {
			index++;
			exceptions[index] = new ExceptionInfoPacket(innerException);
			((ExceptionInfoPacket) exceptions[index - 1]).setInnerException(exceptions[index]); // we are the inner
																								// exception to the
																								// previous one.
			innerException = innerException.getCause();
		}
		return exceptions;
	}
}