package pl.edu.agh.cs.kraksim.core.visitors;

import pl.edu.agh.cs.kraksim.KraksimException;

@SuppressWarnings("serial")
public class VisitingException extends KraksimException {
	public VisitingException() {
	}

	public VisitingException(String message) {
		super(message);
	}

	public VisitingException(String message, Throwable cause) {
		super(message, cause);
	}

	public VisitingException(Throwable cause) {
		super(cause);
	}
}
