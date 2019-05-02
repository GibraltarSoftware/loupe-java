package com.onloupe.model.exception;

public class UnauthorizedAccessException extends GibraltarException {

	private static final long serialVersionUID = 1L;

	public UnauthorizedAccessException() {
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedAccessException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedAccessException(String message, RuntimeException innerException) {
		super(message, innerException);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedAccessException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedAccessException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
