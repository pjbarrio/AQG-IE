package sample.generation.model.queryPool.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import execution.workload.querygeneration.QueryGenerator;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import sample.generation.model.queryPool.QueryPool;
import utils.persistence.persistentWriter;

public class RulesQueryPool extends QueryPool<String> {

	private boolean tuplesAsStopwords;
	private List<String> queries;
	private WorkloadModel workload;
	private Version version;
	private int informationExtractionSystem;
	private int relationConf;
	private String collection;
	private int docsInTraining;

	public RulesQueryPool(String collection, int docsInTraining, int informationExtractionSystem, int relationConf, boolean isForUseful, QueryGenerator<String> queryGenerator, WorkloadModel workload, Version version, boolean tuplesAsStopwords) {
		super(queryGenerator,isForUseful);
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
	protected void _initialize(Database database, persistentWriter pW,int version_seed, boolean reverse, int numberOfQueries) {
		
		File rulesFile = pW.getRelationRulesFile(informationExtractionSystem,collection,relationConf,workload,version,docsInTraining, version_seed,tuplesAsStopwords);
		
		try {
			queries = transformRulesIntoQueries(rulesFile);
			
			if (numberOfQueries > 0){
				queries.subList(Math.min(queries.size(), numberOfQueries),queries.size()).clear();
			}
			
			if (reverse){
				
				Collections.reverse(queries);
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			queries = new ArrayList<String>();
		}
		
	}

	private List<String> transformRulesIntoQueries(File rulesFile) throws IOException{
		
		List<String> rules = FileUtils.readLines(rulesFile);
		
		List<String> queries = new ArrayList<String>();
		
		for (int i = 0; i < rules.size(); i++) {
		
			if (rules.get(i).startsWith("("))
				queries.add(rules.get(i));
			
		}
	
		return queries;
		
	}

	@Override
	public void fetchQuery() {
		
		while ((queries.size() > 0) && !addQuery(queries.remove(0)));		
	
	}

	@Override
	protected void _initialize(List<String> initialList) {
		queries.clear();
		queries.addAll(initialList);
	}

}
