package sample.generation.model.queryPool;

import java.util.ArrayList;
import java.util.List;

import searcher.interaction.formHandler.TextQuery;
import utils.persistence.persistentWriter;

import execution.workload.querygeneration.QueryGenerator;
import exploration.model.Database;
import exploration.model.Document;

public abstract class QueryPool<T> {

	private List<T> queries;
	private QueryGenerator<T> queryGenerator;
	private T lastRaw;
	private boolean retrievesUseful;

	public QueryPool(QueryGenerator<T> queryGenerator, boolean isForUseful) {
		this.queryGenerator = queryGenerator;
		this.retrievesUseful = isForUseful;
	}

	public TextQuery getNextQuery(){
		if (getQueries().size() == 0){
			System.err.println("AHA!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + Thread.currentThread());
		}
		lastRaw = getQueries().remove(0);
		
		return queryGenerator.generateQuery(lastRaw);
	}

	public abstract void updateQueries(Document document);

	public boolean retrievesUseful(){
		
		return retrievesUseful;
		
	}

	protected boolean addQuery(T query) {
		
		if (getQueries().contains(query))
			return false;
		
		return getQueries().add(query);
		
		
	}
	
	private List<T> getQueries() {
		
		if (queries == null){
			queries = new ArrayList<T>();
		}
		return queries;
	}

	public void initialize(Database database,persistentWriter pW, int version_seed, boolean reverse, int numberOfQueries){
		getQueries().clear();
		_initialize(database,pW,version_seed,reverse,numberOfQueries);
	}

	public void initialize(List<T> initialList){
		getQueries().clear();
		_initialize(initialList);
	}
	
	protected abstract void _initialize(List<T> initialList);

	protected abstract void _initialize(Database database, persistentWriter pW, int version_seed, boolean reverse, int numberOfQueries);

	public boolean hasQueries(){
		return getQueries().size() > 0;
	}

	public abstract void fetchQuery();
	
	public QueryGenerator<T> getQueryGenerator(){
		return queryGenerator;
	}

	public T getNextQueryRaw() {
		return lastRaw;
	}
	
}
