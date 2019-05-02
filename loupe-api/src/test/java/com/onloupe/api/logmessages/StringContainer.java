package com.onloupe.api.logmessages;

import java.time.OffsetDateTime;

public class StringContainer
{
	private String _ReadOnlyString;
	private String _UpdatableString;

	public StringContainer()
	{
		this(0);
	}

	public StringContainer(int stringIndex)
	{
		//set all of our values to their defaults.
		String baseline = String.format("%1$s %2$s - %3$s", "String Container", stringIndex, OffsetDateTime.now());
		_ReadOnlyString = baseline;
		_UpdatableString = baseline;
		setImplicitProperty(baseline);
	}

	private String _ImplicitProperty;
	public final String getImplicitProperty()
	{
		return _ImplicitProperty;
	}
	public final void setImplicitProperty(String value)
	{
		_ImplicitProperty = value;
	}

	public final String getExplicitProperty()
	{
		return _UpdatableString;
	}
	public final void setExplicitProperty(String value)
	{
		_UpdatableString = value;
	}

	public final String getReadOnlyExplicitProperty()
	{
		return _ReadOnlyString;
	}
}