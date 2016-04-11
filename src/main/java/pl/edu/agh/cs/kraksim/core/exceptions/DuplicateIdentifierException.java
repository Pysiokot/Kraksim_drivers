package pl.edu.agh.cs.kraksim.core.exceptions;

import pl.edu.agh.cs.kraksim.KraksimException;

@SuppressWarnings("serial")
public class DuplicateIdentifierException extends KraksimException {
	public DuplicateIdentifierException(String message) {
		super(message);
	}
}
