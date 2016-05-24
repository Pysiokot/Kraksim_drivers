package pl.edu.agh.cs.kraksim.weka.data;

import java.util.List;

public class Transaction {
	private final List<Double> transaction;

	public Transaction(List<Double> transaction) {
		this.transaction = transaction;
	}

	public List<Double> getTransaction() {
		return transaction;
	}
}
