package execution.model.generation;

import execution.model.Generation;
import execution.model.Source;
import exploration.model.Combination;
import exploration.model.Database;

public class SimpleGeneration extends Generation {

	@Override
	protected boolean filter(Combination combination, Source generatorSource,
			Database Searchabledatabase) {
		
		if (combination.getCrossSample() != null)
			return false;
		
		return generatorSource.match(Searchabledatabase, combination.getGeneratorSample().getDatabase(), combination.getGeneratorSample().getVersionSeedPos(), combination.getGeneratorSample().getVersionSeedNeg(), combination.getWorkload().getId());		
		
	}

}
