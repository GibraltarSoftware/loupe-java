package com.onloupe.model.exception;

public class DivideByZeroException extends GibraltarException {

	private static final long serialVersionUID = 1L;

	public DivideByZeroException() {
		// TODO Auto-generated constructor stub
	}

	public DivideByZeroException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public DivideByZeroException(String message, RuntimeException innerException) {
		super(message, innerException);
		// TODO Auto-generated constructor stub
	}

	public DivideByZeroException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public DivideByZeroException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
