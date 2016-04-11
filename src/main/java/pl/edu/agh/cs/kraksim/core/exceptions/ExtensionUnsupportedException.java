package pl.edu.agh.cs.kraksim.core.exceptions;

import pl.edu.agh.cs.kraksim.KraksimException;

@SuppressWarnings("serial")
public class ExtensionUnsupportedException extends KraksimException {
	public ExtensionUnsupportedException(String message) {
		super(message);
	}
}
