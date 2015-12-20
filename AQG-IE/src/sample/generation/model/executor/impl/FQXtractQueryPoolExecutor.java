package sample.generation.model.executor.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import plot.generator.impl.UsefulDocumentsByProcessedDocuments;

import exploration.model.Database;
import exploration.model.Document;
import sample.generation.model.executor.QueryPoolExecutor;
import sample.generation.model.performanceChecker.QueryPerformanceChecker;
import sample.generation.model.performanceChecker.QueryPoolPerformanceChecker;
import sample.generation.model.queryPool.QueryPool;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.persistentWriter;

public class FQXtractQueryPoolExecutor<T> extends QueryPoolExecutor {

	private QueryPool<T> queryPool;
	private List<TextQuery> sentQueries;
	private List<Integer> sentQueriesIndex;
	private Map<Integer,List<Boolean>> docsByQuery;
	private int nextIndex;
	private TextQuery query;
	private QueryPoolPerformanceChecker queryPoolPerformanceChecker;
	private QueryPerformanceChecker queryPerformanceChecker;
	private int memory;
	private int maxDocsPerQuery;
	private int currentDocPosition;
	private List<Boolean> currentDocs;
	private Map<TextQuery, Integer> indexMap;
	
	public FQXtractQueryPoolExecutor(QueryPool<T> queryPool, QueryPoolPerformanceChecker queryPoolPerformanceChecker, QueryPerformanceChecker queryPerformanceChecker, int memory, int maxDocsPerQuery){
	
		
		this.queryPool = queryPool;
		
		this.queryPoolPerformanceChecker = queryPoolPerformanceChecker;
		
		this.queryPerformanceChecker = queryPerformanceChecker;
		
		this.memory = memory;
		
		this.maxDocsPerQuery = maxDocsPerQuery;
	
		currentDocPosition = Integer.MAX_VALUE;
		
		docsByQuery = new HashMap<Integer, List<Boolean>>();
		
		indexMap = new HashMap<TextQuery, Integer>();
		
	}
	
	@Override
	public boolean _hasMoreQueries() {
		
		boolean stillproc = currentDocs == null ? true : queryPerformanceChecker.isStillProcessable(currentDocs);

		if (currentDocPosition < maxDocsPerQuery && stillproc)
			return true;
		
		if (!stillproc)
			informExhausted();

		boolean haltQueryPool = !queryPoolPerformanceChecker.isStillProcessable(sentQueriesIndex,docsByQuery);

		queryPool.fetchQuery();
		
		return (queryPool.hasQueries() && !haltQueryPool) || (!sentQueries.isEmpty() && haltQueryPool);
		
	}

	@Override
	public TextQuery _getNextQuery() {
		
		if (currentDocPosition < maxDocsPerQuery)
			return query;
		
		query = null;
		
		if (queryPool.hasQueries()){
			query = queryPool.getNextQuery();
			getSentQueries().add(query);
			sentQueriesIndex.add(getQueryIndex(query));
		}else if (!getSentQueries().isEmpty()){
			query = getSentQueries().get(nextIndex);
			nextIndex = (nextIndex + 1) % getSentQueries().size();
		}
		
		currentDocPosition=0;
		currentDocs = new ArrayList<Boolean>();
		int queryIndex = getQueryIndex(query);
		docsByQuery.put(queryIndex, currentDocs);
		
		return query;		
		
	}

	private Integer getQueryIndex(TextQuery quer) {
		
		Integer index = indexMap.get(quer);
		
		if (index == null){
			index = indexMap.size();
			indexMap.put(quer,index);
		}
		
		return index;
		
		
	}

	@Override
	public void informDocument() {
		currentDocPosition++;
		currentDocs.add(false);
		//checkForQuery();
		if (currentDocs.size() > memory)
                        currentDocs.remove(0);

	}

	@Override
	public void informHit() {
		currentDocPosition++;
		currentDocs.add(true);
		//checkForQuery();
		if (currentDocs.size() > memory)
                        currentDocs.remove(0);

	}

	@Override
	public void _initialize(Database database, persistentWriter pW, int version_seed) {
		
		this.queryPool.initialize(database, pW, version_seed,false,-1);
		
		getSentQueries().clear();
		
		sentQueriesIndex.clear();
		
		nextIndex = 0;
	
	}

	private List<TextQuery> getSentQueries() {
		if (sentQueries == null){
			sentQueries = new ArrayList<TextQuery>();
			sentQueriesIndex = new ArrayList<Integer>();
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
		currentDocPosition=Integer.MAX_VALUE;
		getSentQueries().remove(query);
//		sentQueriesIndex.remove(getQueryIndex(query));
		if (nextIndex > 0)
			nextIndex--;
	}

	

}
