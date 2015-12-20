package execution.model;

import execution.model.algorithmSelection.AlgorithmSelection;
import exploration.model.Combination;
import exploration.model.Database;

public abstract class Generation {

	public boolean filter(Combination combination, Source generatorSource, Database Searchabledatabase, AlgorithmSelection algorithmSelection){
		
		if (algorithmSelection.accepts(combination))
			return filter(combination, generatorSource, Searchabledatabase);
		
		return false;
	}

	protected abstract boolean filter(Combination combination, Source generatorSource,
			Database searchabledatabase);

}
