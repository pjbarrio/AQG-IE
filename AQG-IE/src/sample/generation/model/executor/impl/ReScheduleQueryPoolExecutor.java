package sample.generation.model.executor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import exploration.model.Database;
import exploration.model.Document;
import sample.generation.model.executor.QueryPoolExecutor;
import sample.generation.model.performanceChecker.QueryPerformanceChecker;
import sample.generation.model.performanceChecker.QueryPoolPerformanceChecker;
import sample.generation.model.queryPool.QueryPool;
import searcher.interaction.formHandler.TextQuery;
import utils.algorithms.ListBasedPositionComparator;
import utils.persistence.persistentWriter;

public abstract class ReScheduleQueryPoolExecutor<T> extends QueryPoolExecutor {

	private QueryPool<T> queryPool;
	private Map<T,Integer> storedQueries;
	private Integer currentIndexQuery;
	private Map<Integer, List<Boolean>> documents;
	private QueryPoolPerformanceChecker queryPoolPerformanceChecker;
	private QueryPerformanceChecker queryPerformanceChecker;
	private int memory;
	private Set<Integer> exhaustedQueries;
	private List<Integer> orderedQueries;
	private TextQuery nextQuery;


	public ReScheduleQueryPoolExecutor(QueryPool<T> queryPool, QueryPoolPerformanceChecker queryPoolPerformanceChecker, QueryPerformanceChecker queryPerformanceChecker, int memory){
	
		this.queryPool = queryPool;
		
		this.queryPoolPerformanceChecker = queryPoolPerformanceChecker;
		
		this.queryPerformanceChecker = queryPerformanceChecker;
		
		this.memory = memory;
		
	}
	
	@Override
	public boolean _hasMoreQueries() {
		
		nextQuery = generateNextQuery();
		
		return nextQuery != null;
		
	}

	private Map<T,Integer> getStoredQueries() {
		
		if (storedQueries == null){
		
			storedQueries = new HashMap<T,Integer>();
		
		}
		
		return storedQueries;
	}

	@Override
	public TextQuery _getNextQuery() {
	
		return nextQuery;
	
	}
	
	
	private TextQuery generateNextQuery() {
		
		TextQuery query = null;
		
		if (keepProcessingQueryPool()){
		
			queryPool.fetchQuery();
			
			if (queryPool.hasQueries()){
				
				query = queryPool.getNextQuery();
				
				T rawQuery = queryPool.getNextQueryRaw();
				
				currentIndexQuery = getQueryIndex(rawQuery);
				
				if (getDocuments().get(currentIndexQuery) == null)
					getDocuments().put(currentIndexQuery, new ArrayList<Boolean>());
				
				getOrderedQueries().add(currentIndexQuery);
				
				return query;
				
			}

		}
			
		if (!getOrderedQueries().isEmpty()){
			regenerateQueryPool();
			if (hasMoreQueries()){
				return getNextQuery();
			}
		}
				
		return query;
	
	}

	private List<Integer> getOrderedQueries() {
		
		if (orderedQueries == null){
			
			orderedQueries = new ArrayList<Integer>();
			
		}
		
		return orderedQueries;
	}

	private int getQueryIndex(T query) {
		
		Integer index = getStoredQueries().get(query);
		
		if (index == null){
			index = getStoredQueries().size() + 1;
			getStoredQueries().put(query, index);
			
		}
		
		return index;
	}

	protected void regenerateQueryPool(){
		
		Map<Integer,T> queries = new HashMap<Integer, T>();
		
		List<T> queriesToRemove = new ArrayList<T>();
		
		List<Integer> queriesTokeep = getQueriesToKeep(getOrderedQueries(),queries,queriesToRemove);				
		
		queryPool.initialize(regenerateQueryPool(queriesTokeep,queries,getDocuments()));
		
		for (int i = 0; i < queriesToRemove.size(); i++) {
			
			Integer index = getStoredQueries().remove(queriesToRemove.get(i));
			
			if (getDocuments().containsKey(index)){
				
				getDocuments().remove(index).clear();
				
			}
			
			
			getExhasutedQueries().remove(index);
			
		}
		
		for (int i = 0; i < queriesTokeep.size(); i++) {
			
			List<Boolean> documents = getDocuments().get(queriesTokeep.get(i));
			
			while (documents.size() > memory){
				
				documents.remove(0);
				
			}
			
		}
	
		getOrderedQueries().clear();
		
		currentIndexQuery = -1;
		
	}

	private List<Integer> getQueriesToKeep(List<Integer> orderedQueries, Map<Integer, T> queries, List<T> queriesToRemove){
		
		List<Integer> list = new ArrayList<Integer>();
		
		for (Entry<T,Integer> query : getStoredQueries().entrySet()) {
			
			if (!getExhasutedQueries().contains(query.getValue()) && queryPerformanceChecker.isStillProcessable(getDocuments().get(query.getValue()))){
				list.add(query.getValue());
				queries.put(query.getValue(), query.getKey());
			}else{
				queriesToRemove.add(query.getKey());
			}
			
		}
		
		Collections.sort(list, new ListBasedPositionComparator<Integer>(orderedQueries));
		
		return list;
		
	}

	protected abstract  List<T> regenerateQueryPool(
			List<Integer> queriesTokeep, Map<Integer, T> queries,
			Map<Integer, List<Boolean>> documents);

	private boolean keepProcessingQueryPool(){
		
		return queryPoolPerformanceChecker.isStillProcessable(getOrderedQueries(),getDocuments());
		
	}

	@Override
	public void informDocument() {
		
		getDocuments().get(currentIndexQuery).add(false);
		
	}

	private Map<Integer,List<Boolean>> getDocuments() {
		
		if (documents == null){
			documents = new HashMap<Integer, List<Boolean>>();
		}
		return documents;
	}

	@Override
	public void informHit() {
		
		getDocuments().get(currentIndexQuery).add(true);

	}

	@Override
	public void _initialize(Database database, persistentWriter pW,
			int version_seed) {
		
		queryPool.initialize(database, pW, version_seed, false,-1);

		getStoredQueries().clear();
		
		getDocuments().clear();
		
		getOrderedQueries().clear();
		
		currentIndexQuery = -1;
		
	}

	public boolean retrievesUseful(){
		
		return queryPool.retrievesUseful();
		
	}

	public void updateQueries(Document document){
		
		queryPool.updateQueries(document);
		
	}
	
	public void informExhausted(){
		
		getExhasutedQueries().add(currentIndexQuery);
		
	}

	private Set<Integer> getExhasutedQueries() {
		
		if (exhaustedQueries == null){
			exhaustedQueries = new HashSet<Integer>();
		}
		return exhaustedQueries;
	}
	
}
