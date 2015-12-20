package sample.generation.model.impl;

import online.documentHandler.contentExtractor.ContentExtractor;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import extraction.relationExtraction.RelationExtractionSystem;
import sample.generation.model.SampleConfiguration;
import sample.generation.model.SimpleSampleGenerator;
import sample.generation.model.executor.QueryPoolExecutor;
import sample.generation.model.queryPool.QueryPool;
import searcher.Searcher;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;

public class DualPoolSampleGenerator extends SimpleSampleGenerator {

	public DualPoolSampleGenerator(QueryPoolExecutor firstQueryPool, QueryPoolExecutor secondQueryPool, InteractionPersister interactionPersister, String[] relations, 
			ContentExtractor contentExtractor, RelationExtractionSystem relationExtractionSystem) {
		
		super(contentExtractor, relations, interactionPersister,firstQueryPool,secondQueryPool,relationExtractionSystem);
		
	}

	@Override
	protected boolean addRetrievedDocument(Document document, Sample sample, boolean isItUseful, SampleConfiguration sampleConfiguration){
		
		if (isItUseful && getCurrentQueryPool().retrievesUseful()){
			return sample.addUsefulDocument(document, sampleConfiguration);
		}else if (!isItUseful && !getCurrentQueryPool().retrievesUseful()){
			return sample.addUselessDocument(document, sampleConfiguration);
		}
		return false;
	}

}
