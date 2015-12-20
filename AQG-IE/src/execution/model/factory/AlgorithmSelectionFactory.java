package execution.model.factory;

import java.util.ArrayList;

import execution.model.algorithmSelection.AlgorithmSelection;
import execution.model.algorithmSelection.AnyAlgorithmSelection;
import execution.model.algorithmSelection.FixedAlgorithmSelection;
import execution.model.parameters.Parametrizable;
import exploration.model.enumerations.AlgorithmEnum;
import exploration.model.enumerations.AlgorithmSelectionEnum;
import exploration.model.enumerations.ExecutionAlternativeEnum;

public class AlgorithmSelectionFactory {

	public static AlgorithmSelection generateInstance(String string, Parametrizable parametrizable) {
		
		AlgorithmEnum[] algorithms = AlgorithmEnum.values();
		
		ArrayList<AlgorithmEnum> ret = new ArrayList<AlgorithmEnum>();
		
		for (AlgorithmEnum algorithmEnum : algorithms) {
			
			if (parametrizable.containsParameter(ExecutionAlternativeEnum.valueOf(algorithmEnum.name()))){
				
				ret.add(algorithmEnum);
				
			}
			
		}
		
		switch (AlgorithmSelectionEnum.valueOf(string)) {
		case ANY:
			
			return new AnyAlgorithmSelection(ret);

		case FIXED:
			
			return new FixedAlgorithmSelection(ret);
			
		default:
			
			return null;
		}
	}
	
}
