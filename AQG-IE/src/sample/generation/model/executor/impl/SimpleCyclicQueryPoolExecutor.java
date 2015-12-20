package sample.generation.model.executor.impl;

import java.util.ArrayList;
import java.util.List;

import exploration.model.Database;
import exploration.model.Document;
import sample.generation.model.executor.QueryPoolExecutor;
import sample.generation.model.queryPool.QueryPool;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.persistentWriter;

public class SimpleCyclicQueryPoolExecutor<T> extends QueryPoolExecutor {

	private QueryPool<T> queryPool;
	private List<TextQuery> sentQueries;
	private int nextIndex;
	private TextQuery query;
	private boolean reverse;
	private int numberOfQueries;


	public SimpleCyclicQueryPoolExecutor(QueryPool<T> queryPool, boolean reverse, int numberOfQueries){
		this.queryPool = queryPool;
		this.reverse = reverse;
		this.numberOfQueries = numberOfQueries;
	}
	
	@Override
	public boolean _hasMoreQueries() {
		
		queryPool.fetchQuery();
		
		return queryPool.hasQueries() || !sentQueries.isEmpty();
		
	}

	@Override
	public TextQuery _getNextQuery() {
		
		query = null;
		
		if (queryPool.hasQueries()){
			query = queryPool.getNextQuery();
			getSentQueries().add(query);
			return query;
		}else if (!getSentQueries().isEmpty()){
			query = getSentQueries().get(nextIndex);
			nextIndex = (nextIndex + 1) % getSentQueries().size();
		}
		
		return query;		
		
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
	public void _initialize(Database database, persistentWriter pW, int version_seed) {
		
		this.queryPool.initialize(database, pW, version_seed, reverse, numberOfQueries);
		
		getSentQueries().clear();
		
		nextIndex = 0;
	
	}

	private List<TextQuery> getSentQueries() {
		if (sentQueries == null){
			sentQueries = new ArrayList<TextQuery>();
		}
		return sentQueries;
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
		getSentQueries().remove(query);
		if (nextIndex > 0)
			nextIndex--;
	}

}
