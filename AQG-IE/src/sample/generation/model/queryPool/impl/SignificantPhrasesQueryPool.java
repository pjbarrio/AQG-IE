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

public class SignificantPhrasesQueryPool extends QueryPool<String> {
	
	private List<String> significantPhrases;
	private boolean tuplesAsStopwords;
	private WorkloadModel workload;
	private Version version;
	private int informationExtractionSystem;
	private int relationConf;
	private String collection;
	private int docsInTraining;

	
	public SignificantPhrasesQueryPool(int informationExtractionSystem, int relationConf, String collection, boolean isForUseful, WorkloadModel workload, Version version, QueryGenerator<String> queryGenerator, boolean tuplesAsStopwords,int docsInTraining) {
		super(queryGenerator, isForUseful);
		this.workload = workload;
		this.version = version;
		this.tuplesAsStopwords = tuplesAsStopwords;
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
		
		significantPhrases = pW.getSignificantPhrases(informationExtractionSystem,relationConf,collection,workload,version,tuplesAsStopwords,version_seed,docsInTraining);
		
		if (numberOfQueries > 0){
			
			significantPhrases.subList(Math.min(significantPhrases.size(), numberOfQueries),significantPhrases.size()).clear();
			
		}
		
		if (reverse){
						
			Collections.reverse(significantPhrases);
			
		}
		
	}

	@Override
	public void fetchQuery() {
		
		while (significantPhrases.size()>0 && !addQuery(significantPhrases.remove(0)));
	
	}

	@Override
	protected void _initialize(List<String> initialList) {
		significantPhrases.clear();
		significantPhrases.addAll(initialList);		
	}

}
