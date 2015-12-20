package plot.selector.impl;

import java.util.List;

import exploration.model.enumerations.AlgorithmEnum;

import plot.selector.Selector;
import utils.persistence.persistentWriter;

public class ExecutionAlternativeFilteredAlgorithmSelector extends
		AlgorithmSelector {

	private List<Integer> executionAlternatives;
	private String name;

	public ExecutionAlternativeFilteredAlgorithmSelector(
			String name, AlgorithmEnum algorithm, List<Integer> executionAlternatives,
			persistentWriter pW) {
		super(algorithm,pW);

		this.executionAlternatives = executionAlternatives;
		this.name = name;
	}

	@Override
	public List<Integer> getExecutions() {
		
		return pW.getExecutions(executionAlternatives,algorithm.name());
		
	}

	@Override
	public String getName() {
		return name;
	}

}
