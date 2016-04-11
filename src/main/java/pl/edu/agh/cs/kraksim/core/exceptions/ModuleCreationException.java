package pl.edu.agh.cs.kraksim.core.exceptions;

import pl.edu.agh.cs.kraksim.KraksimException;

@SuppressWarnings("serial")
public class ModuleCreationException extends KraksimException {

	public ModuleCreationException() {
	}

	public ModuleCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModuleCreationException(String message) {
		super(message);
	}

	public ModuleCreationException(Throwable cause) {
		super(cause);
	}
}
