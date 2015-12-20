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

public class UseAllSampleGenerator extends SimpleSampleGenerator {


	public UseAllSampleGenerator(QueryPoolExecutor firstQueryPool, QueryPoolExecutor secondQueryPool, ContentExtractor contentExtractor, String[] relations, 
			InteractionPersister interactionPersister, RelationExtractionSystem relationExtractionSystem) {
		super(contentExtractor, relations, interactionPersister, firstQueryPool, secondQueryPool, relationExtractionSystem);
	}

	@Override
	protected boolean addRetrievedDocument(Document document, Sample sample, boolean isItUseful, SampleConfiguration sampleConfiguration) {
		
		if (isItUseful){
			return sample.addUsefulDocument(document, sampleConfiguration);
		}else{
			return sample.addUselessDocument(document, sampleConfiguration);
		}
	}

}
