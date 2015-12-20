package sample.generation.model.queryPool.impl;

import java.util.List;

import execution.workload.querygeneration.QueryGenerator;
import exploration.model.Database;
import exploration.model.Document;
import sample.generation.model.queryPool.QueryPool;
import utils.persistence.persistentWriter;

public class DummyQueryPool extends QueryPool<String> {

	public DummyQueryPool(QueryGenerator<String> queryGenerator,
			boolean isForUseful) {
		super(queryGenerator, isForUseful);
	}

	@Override
	public void updateQueries(Document document) {
		;
	}

	@Override
	protected void _initialize(List<String> initialList) {
		;
	}

	@Override
	protected void _initialize(Database database, persistentWriter pW,
			int version_seed, boolean reverse, int numberOfQueries) {
		;
	}

	@Override
	public void fetchQuery() {
		;
	}


}
