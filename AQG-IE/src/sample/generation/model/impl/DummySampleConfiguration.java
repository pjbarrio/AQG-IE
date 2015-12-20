package sample.generation.model.impl;

import sample.generation.model.SampleConfiguration;

public class DummySampleConfiguration extends SampleConfiguration {

	public DummySampleConfiguration(int id){
		this(id,0,0,0,0,0,0,0.0,0.0,false,"",0);
	}
	
	public DummySampleConfiguration(int id, int resultsPerQuery, int usefulNumber,
			int uselessNumber, int allowedNumberOfQueries,
			int allowedNumeberOfProcessedDocuments, int features, double minFrequency, double maxFrequency, boolean countsAll, String baseCollection, int docsInTraining) {
		super(id,0,0,0,0,0,0,0,true,resultsPerQuery, usefulNumber, uselessNumber, allowedNumberOfQueries,
				allowedNumeberOfProcessedDocuments, countsAll,baseCollection,docsInTraining);
		
	}

}
