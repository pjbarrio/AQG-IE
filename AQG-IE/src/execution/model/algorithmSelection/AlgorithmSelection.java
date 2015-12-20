package execution.model.algorithmSelection;

import java.util.List;

import exploration.model.Combination;
import exploration.model.enumerations.AlgorithmEnum;

public class AlgorithmSelection {

	private List<AlgorithmEnum> accepted;

	public AlgorithmSelection(List<AlgorithmEnum> accepted){
		this.accepted = accepted;
	}
	
	public boolean accepts(Combination combination) {
		
		return accepted.contains(combination.getAlgorithm().getEnum());
		
	}

}
