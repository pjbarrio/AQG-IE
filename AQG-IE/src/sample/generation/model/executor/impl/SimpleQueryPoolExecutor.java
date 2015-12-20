package sample.generation.model.executor.impl;

import exploration.model.Database;
import exploration.model.Document;
import sample.generation.model.executor.QueryPoolExecutor;
import sample.generation.model.queryPool.QueryPool;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.persistentWriter;

public class SimpleQueryPoolExecutor<T> extends QueryPoolExecutor {

	private QueryPool<T> queryPool;
	private boolean reverse;
	private int numberOfQueries;

	public SimpleQueryPoolExecutor(QueryPool<T> queryPool, boolean reverse, int numberOfQueries){
		this.queryPool = queryPool;
		this.reverse = reverse;
		this.numberOfQueries = numberOfQueries;
	}
	
	@Override
	public boolean _hasMoreQueries() {
		
		queryPool.fetchQuery();
		
		return queryPool.hasQueries();
	
	}

	@Override
	public TextQuery _getNextQuery() {
		
		return queryPool.getNextQuery();
		
	}

	@Override
	public void informDocument() {
		;
	}

	@Override
	public void informHit() {
		;
	}

	@Override
	public void _initialize(Database database, persistentWriter pW,
			int version_seed) {
		
		this.queryPool.initialize(database, pW, version_seed, reverse, numberOfQueries);

	}

	@Override
	public boolean retrievesUseful() {
		return queryPool.retrievesUseful();
	}

	@Override
	public void updateQueries(Document document) {
		queryPool.updateQueries(document);		
	}

	@Override
	public void informExhausted() {
		;		
	}

}
