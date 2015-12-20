package sample.generation.model.queryPool.impl;

import java.util.List;

import execution.workload.querygeneration.QueryGenerator;
import exploration.model.Database;
import exploration.model.Document;
import sample.generation.model.queryPool.QueryPool;
import sample.generation.model.wordloader.WordLoader;
import utils.persistence.persistentWriter;

public class CachedNegativeSampleQueryPool extends QueryPool<String> {

	private List<String> orderedQueries;
	private int experimentId;
	private int processedDocs;

	public CachedNegativeSampleQueryPool(int experimentId, int processedDocs, QueryGenerator<String> queryGenerator,
			boolean isForUseful) {
		super(queryGenerator, isForUseful);
		this.experimentId = experimentId;
		this.processedDocs = processedDocs;
	}

	@Override
	public void updateQueries(Document document) {
		;//Does not have to do anything.
	}

	@Override
	protected void _initialize(List<String> initialList) {
		;//will be initialized with the other method.
	}

	@Override
	protected void _initialize(Database database, persistentWriter pW,
			int version_seed, boolean reverse, int numberOfQueries) {
		
		orderedQueries = pW.getQueriesForNegativeSample(experimentId,processedDocs,database,version_seed);

	}

	@Override
	public void fetchQuery() {
		
		while ((orderedQueries.size() > 0) && !addQuery(orderedQueries.remove(0)));
		
	}

}
