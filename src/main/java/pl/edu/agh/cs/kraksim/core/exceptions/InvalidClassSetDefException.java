package pl.edu.agh.cs.kraksim.core.exceptions;

import pl.edu.agh.cs.kraksim.KraksimException;

@SuppressWarnings("serial")
public class InvalidClassSetDefException extends KraksimException {

	public InvalidClassSetDefException() {
	}

	public InvalidClassSetDefException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidClassSetDefException(String message) {
		super(message);
	}

	public InvalidClassSetDefException(Throwable cause) {
		super(cause);
	}
}
