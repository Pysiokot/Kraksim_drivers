package pl.edu.agh.cs.kraksim.main;

@SuppressWarnings("serial")
class AlgorithmConfigurationException extends Exception {
	public AlgorithmConfigurationException(String message) {
		super(message);
	}

	public AlgorithmConfigurationException(String message, Exception e) {
		super(message, e);

	}
}
