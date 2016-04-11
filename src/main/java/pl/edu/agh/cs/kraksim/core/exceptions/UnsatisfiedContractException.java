package pl.edu.agh.cs.kraksim.core.exceptions;

@SuppressWarnings("serial")
public class UnsatisfiedContractException extends ModuleCreationException {
	public UnsatisfiedContractException() {
	}

	public UnsatisfiedContractException(String message) {
		super(message);
	}

	public UnsatisfiedContractException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsatisfiedContractException(Throwable cause) {
		super(cause);
	}
}
