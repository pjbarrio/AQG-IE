package plot.selector.impl;

import java.util.ArrayList;
import java.util.List;

import plot.selector.ExecutionSelector;
import plot.selector.Selector;
import utils.persistence.persistentWriter;
import exploration.model.enumerations.AlgorithmEnum;

public class AlgorithmSelector extends ExecutionSelector{

	protected AlgorithmEnum algorithm;
	protected persistentWriter pW;

	public AlgorithmSelector(AlgorithmEnum algorithm, persistentWriter pW){
		this.algorithm = algorithm;
		this.pW = pW;
	}
	
	@Override
	public List<Integer> getExecutions() {
	
		List<Integer> combinationsId = pW.getCombinations(algorithm.name());

		List<Integer> ret = new ArrayList<Integer>();
		
		for (Integer combinationId : combinationsId) {
			
			ret.addAll(pW.getExecutions(combinationId));
		
		}
			
		return ret;
	}

	@Override
	public String getName() {
		return algorithm.name();
	}

}
