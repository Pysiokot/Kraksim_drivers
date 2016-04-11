package pl.edu.agh.cs.kraksim.core.exceptions;

import pl.edu.agh.cs.kraksim.KraksimException;

@SuppressWarnings("serial")
public class InvalidExtensionClassException extends KraksimException {
	public InvalidExtensionClassException(String message) {
		super(message);
	}
}
