package pl.edu.agh.cs.kraksim.core.exceptions;

import pl.edu.agh.cs.kraksim.KraksimException;

/* See assumptions about action in Action.java file */
@SuppressWarnings("serial")
public class InvalidActionException extends KraksimException {
	public InvalidActionException(String message) {
		super(message);
	}
}
