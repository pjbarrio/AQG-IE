package searcher.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.navigation.NavigationHandler;
import online.queryResultPageHandler.QueryResultPageHandler;
import online.resultHandler.ResultDocumentHandler;
import online.tagcleaner.HTMLTagCleaner;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.enumerations.ExperimentEnum;
import searcher.Searcher;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;

public class CachedSearcher extends Searcher {

	private static final List<Document> emptyResults = new ArrayList<Document>(0);
	private InteractionPersister persister;
	private int experimentID;
	private Database database;
	private String navhandler;
	private String pagehandler;
	private String reshandler;
	private Map<Long,List<Document>> qtable;
	private Map<TextQuery,Long> idTable;
	
	public CachedSearcher(Database database, int experimentID, NavigationHandler nh, QueryResultPageHandler qrph, ResultDocumentHandler rdh, InteractionPersister persister) {
		super(0, database);
		this.database = database;
		this.experimentID = experimentID;
		this.persister = persister;
		this.navhandler =nh.getName();
		this.pagehandler = qrph.getName();
		this.reshandler = rdh.getName();
		idTable = new ConcurrentHashMap<TextQuery, Long>();
		qtable = new ConcurrentHashMap<Long, List<Document>>();
	}

	@Override
	protected boolean executeSearch(long id, List<String> must_words,
			List<String> must_not_words, boolean createBatch,
			long waitbetweensearch) {
		return true;
	}

	@Override
	protected List<Document> retrieveNextDocuments(long queryId) {
		throw new UnsupportedOperationException();//Should not be used
	}

	@Override
	public List<Document> retrieveMaxAllowedDocuments(List<String> must_words, List<String> must_not_words){
		
		TextQuery texQuery = new TextQuery(must_words);
		
		long qId = getTextQuery(texQuery);
			
		List<Document> ret = getResults(qId); 
				
		if (ret == null){ //has been processed but no results
			return emptyResults;
		}else
			return ret;
		
	}

	private long getTextQuery(TextQuery texQuery) {
		
		synchronized (idTable) {
			
			Long l = idTable.get(texQuery);
			
			if (l == null){
				l = persister.getBasePersister().getTextQuery(texQuery);
				idTable.put(texQuery, l);
			}
			
			return l;

			
		}
		
	}

	private synchronized List<Document> getResults(long queryId) {
		
		List<Document> l;
		
		synchronized (qtable) {
			
			l = qtable.get(queryId);
			
			if (l == null){
				l = persister.getBasePersister().getQueryResultsTable(experimentID,database.getId(),navhandler,pagehandler,reshandler).get(queryId);
				if (l == null)
					l = new ArrayList<Document>(0);
				qtable.put(queryId, l);
			}
			
		}
		
		return new ArrayList<Document>(l);
	}

	@Override
	protected List<Document> retrieveAllowedDocuments(long id) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void cleanQueryInternal(long id) {
		;
	}

	@Override
	protected void cleanSearcherInternal() {
		;
	}

	@Override
	protected void storeAllowedDocuments(long id) {
		;
	}

	@Override
	public void finishNegativeBatchDownloader(
			ExperimentEnum experimentConsistensyId, int databaseId,
			String computerName, int goodState, int split) {
		;
	}

	@Override
	public void finishBatchDownloader(ExperimentEnum experimentConsistensyId,
			int databaseId, String computerName, int goodState) {
		;
	}

}
