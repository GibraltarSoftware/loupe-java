package com.onloupe.model.exception;

public class GibraltarSerializationException extends GibraltarException {

	private static final long serialVersionUID = -1509699404629834090L;
	
	private boolean streamFailed;

	public GibraltarSerializationException() {
		// TODO Auto-generated constructor stub
	}

	public GibraltarSerializationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public GibraltarSerializationException(String message, RuntimeException innerException) {
		super(message, innerException);
		// TODO Auto-generated constructor stub
	}

	public GibraltarSerializationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public GibraltarSerializationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	public GibraltarSerializationException(String message, boolean streamFailed) {
		super(message);
		this.streamFailed = streamFailed;
	}
	
	public GibraltarSerializationException(String message, Throwable cause, boolean streamFailed) {
		super(message, cause);
		this.streamFailed = streamFailed;
	}

	public boolean isStreamFailed() {
		return streamFailed;
	}

}
