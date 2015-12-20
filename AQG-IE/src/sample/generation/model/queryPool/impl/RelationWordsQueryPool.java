package sample.generation.model.queryPool.impl;

import java.util.Collections;
import java.util.List;

import execution.workload.querygeneration.QueryGenerator;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import sample.generation.model.queryPool.QueryPool;
import utils.persistence.persistentWriter;
import weka.attributeSelection.ASEvaluation;

public class RelationWordsQueryPool extends QueryPool<String> {

	private List<String> relationWords;
	private boolean tuplesAsStopwords;
	private ASEvaluation eval;
	private WorkloadModel workload;
	private Version version;
	private int informationExtractionSystem;
	private int relationConf;
	private String collection;
	private int docsInTraining;

	
	public RelationWordsQueryPool(int informationExtractionSystem, int relationConf, String collection, boolean isForUseful, WorkloadModel workload, Version version, QueryGenerator<String> queryGenerator, boolean tuplesAsStopwords,ASEvaluation eval, int docsInTraining) {
		
		super(queryGenerator,isForUseful);
		
		this.workload = workload;
		this.version = version;
		this.tuplesAsStopwords = tuplesAsStopwords;
		this.eval = eval;
		this.informationExtractionSystem = informationExtractionSystem;
		this.relationConf = relationConf;
		this.collection = collection;
		this.docsInTraining = docsInTraining;
	}

	@Override
	public void updateQueries(Document document) {
		;		
	}

	@Override
	public void _initialize(Database database, persistentWriter pW, int version_seed, boolean reverse, int numberOfQueries) {
		
		relationWords = pW.getRelationKeywords(informationExtractionSystem,relationConf,collection,workload,version,tuplesAsStopwords,version_seed,eval,docsInTraining);
		
		if (numberOfQueries > 0){

			relationWords.subList(Math.min(relationWords.size(), numberOfQueries),relationWords.size()).clear();

		}
		
		if (reverse){
			
			Collections.reverse(relationWords);
			
		}
		
		
		
	}

	@Override
	public void fetchQuery() {
		
		while (relationWords.size()>0 && !addQuery(relationWords.remove(0)));
	
	}

	@Override
	protected void _initialize(List<String> initialList) {
		relationWords.clear();
		relationWords.addAll(initialList);		
	}

}
