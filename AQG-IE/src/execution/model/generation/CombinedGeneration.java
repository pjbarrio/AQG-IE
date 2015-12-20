package execution.model.generation;

import execution.model.Generation;
import execution.model.Source;
import exploration.model.Combination;
import exploration.model.Configuration;
import exploration.model.Database;

public class CombinedGeneration extends Generation {

	private Source crossableSource;
	private Configuration configuration;

	public CombinedGeneration(Source source,
			Configuration configuration) {
		
		this.crossableSource = source;
		this.configuration = configuration;
		
	}

	@Override
	protected boolean filter(Combination combination, Source generatorSource,
			Database Searchabledatabase) {
		
		if (combination.getCrossSample() == null)
			
			return false;
		
		if (!configuration.equals(combination.getConfiguration())){
			
			return false;
		
		}
		
		if (generatorSource.match(Searchabledatabase, combination.getGeneratorSample().getDatabase(),combination.getGeneratorSample().getVersionSeedPos(),combination.getGeneratorSample().getVersionSeedNeg(),combination.getWorkload().getId())){
			
//			return crossableSource.match(combination.getGeneratorSample().getDatabase(), combination.getCrossSample().getDatabase());
			
			return crossableSource.match(Searchabledatabase, combination.getCrossSample().getDatabase(), combination.getGeneratorSample().getVersionSeedPos(), combination.getGeneratorSample().getVersionSeedNeg(),combination.getWorkload().getId());
			
		}else{
			return false;
		}
		
	}



}
