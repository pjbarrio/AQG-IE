package sample.generation.factory;

import online.documentHandler.contentExtractor.ContentExtractor;
import sample.generation.model.SampleGenerator;
import sample.generation.model.executor.QueryPoolExecutor;
import sample.generation.model.impl.DualPoolSampleGenerator;
import sample.generation.model.impl.UseAllSampleGenerator;
import utils.persistence.InteractionPersister;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.QueryPoolEnum;
import exploration.model.enumerations.SampleGeneratorEnum;
import extraction.relationExtraction.RelationExtractionSystem;

public class SampleGeneratorFactory {

	public static SampleGenerator generateInstance(boolean useAll, QueryPoolExecutor positivequeryPool, QueryPoolExecutor negativequeryPool, boolean positiveFirst, InteractionPersister interactionPersister,
			WorkloadModel workload, ContentExtractor contentExtractor, RelationExtractionSystem relationExtractionSystem) {
		
//		switch (SampleGeneratorEnum.valueOf(name)){
//		
//		case DUAL_POOL:
		if (!useAll)
			if (positiveFirst)
				return new DualPoolSampleGenerator(positivequeryPool, negativequeryPool, interactionPersister,workload.getRelations(),contentExtractor,relationExtractionSystem);
			else
				return new DualPoolSampleGenerator(negativequeryPool,positivequeryPool, interactionPersister,workload.getRelations(),contentExtractor,relationExtractionSystem);
//		case USE_ALL:
		else	
			if (positiveFirst)
				return new UseAllSampleGenerator(positivequeryPool, negativequeryPool,contentExtractor,workload.getRelations(),interactionPersister, relationExtractionSystem);
			else
				return new UseAllSampleGenerator(negativequeryPool, positivequeryPool, contentExtractor,workload.getRelations(),interactionPersister,relationExtractionSystem);
			
//		default: 
//			
//			return null;
//		}
		
		
	}
	
}
