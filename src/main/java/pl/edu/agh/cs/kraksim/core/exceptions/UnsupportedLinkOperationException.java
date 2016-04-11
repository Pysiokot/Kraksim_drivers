package pl.edu.agh.cs.kraksim.core.exceptions;

import pl.edu.agh.cs.kraksim.KraksimRuntimeException;

@SuppressWarnings("serial")
public class UnsupportedLinkOperationException extends KraksimRuntimeException {
	public UnsupportedLinkOperationException(String message) {
		super(message);
	}
}
