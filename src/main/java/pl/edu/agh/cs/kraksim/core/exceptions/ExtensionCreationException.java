package pl.edu.agh.cs.kraksim.core.exceptions;

import pl.edu.agh.cs.kraksim.KraksimException;

@SuppressWarnings("serial")
public class ExtensionCreationException extends KraksimException {

	public ExtensionCreationException() {
	}

	public ExtensionCreationException(String message) {
		super(message);
	}

	public ExtensionCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtensionCreationException(Throwable cause) {
		super(cause);
	}
}
