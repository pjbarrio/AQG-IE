package searcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import searcher.Searcher.QueryWrap;
import searcher.interaction.formHandler.TextQuery;
import utils.query.QueryParser;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.enumerations.ExperimentEnum;
import exploration.model.enumerations.ExperimentStatusEnum;

public abstract class Searcher {

	public class QueryWrap{
		
		private List<String> must;
		private List<String> must_not;
		private List<String> smust;
		private List<String> smust_not;

		public QueryWrap(List<String> must, List<String> must_not){
			
			this.must = must;
			this.must_not = must_not;
			
			this.smust = new ArrayList<String>(must);
			this.smust_not = new ArrayList<String>(must_not);
			
			Collections.sort(this.smust);
			Collections.sort(this.smust_not);
			
		}
		
		@Override
		public int hashCode() {
			int hashCode = 1;
			
			for (String string : smust) {
				hashCode = 31*hashCode + string.hashCode();
			}
			
			hashCode = 29*hashCode; //XXX make sure this works.
			
			for (String string : smust_not) {
				hashCode = 31*hashCode + string.hashCode();
			}
			
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			return hashCode() == obj.hashCode();
		}

		public List<String> getMustWords() {
			return must;
		}

		public List<String> getMustNotWords() {
			return must_not;
		}
	}
	
	private Hashtable<Long, Boolean> already;
	private Hashtable<QueryWrap, Long> idTable;
	private Hashtable<Long, QueryWrap> tableId;
	private long waitbetweeensearch;
	
	public Searcher(long waitbetweensearch, Database database){
		already = new Hashtable<Long, Boolean>();
		idTable = null;
		tableId = null;
		this.waitbetweeensearch = waitbetweensearch;
	}
	
	public synchronized boolean doSearch(List<String> must_words, List<String> must_not_words){
	
		return doSearch(must_words, must_not_words,false);
		
	}

	public synchronized boolean doSearch(List<String> must_words, List<String> must_not_words,
			boolean createBatch){
		
		long id = getId(must_words,must_not_words);
		
		return executeSearch(id,must_words,must_not_words,createBatch, waitbetweeensearch);
	
		
	}
	
	protected abstract boolean executeSearch(long id, List<String> must_words,
			List<String> must_not_words, boolean createBatch, long waitbetweensearch);

	protected synchronized long getId(List<String> must_words,
			List<String> must_not_words){
		
		QueryWrap qw = new QueryWrap(must_words, must_not_words);
		
		Long id = getIdTable().get(qw);
		
		if (id == null){
			
			id = (long)getIdTable().size();
			
			getIdTable().put(qw,id);
			
			getTableId().put(id,qw);

		}
		
		return id;
	}

	private Hashtable<Long, QueryWrap> getTableId() {
		if (tableId == null){
			tableId = new Hashtable<Long,QueryWrap>();
		}
		return tableId;
	}

	private Hashtable<QueryWrap,Long> getIdTable() {
		
		if (idTable == null){
			idTable = new Hashtable<QueryWrap,Long>();
		}
		return idTable;
	}

	public synchronized List<Document> retrieveDocuments(ArrayList<String> must_words, ArrayList<String> must_not_words){
		
		//this has to be used with no thread safe always.
		
		long queryId = getId(must_words, must_not_words);
		
		if (already.get(queryId) != null && already.get(queryId))
			return new ArrayList<Document>(0);

		List<Document> ret = retrieveNextDocuments(queryId);
		
		if (ret.isEmpty())
			already.put(queryId, true);
		
		return ret;
		
	}

	protected abstract List<Document> retrieveNextDocuments(long queryId);
	
	public synchronized List<Document> retrieveMaxAllowedDocuments(List<String> must_words, List<String> must_not_words){
		
		long id = getId(must_words, must_not_words);
		
		if (already.get(id) != null && already.get(id))
			return new ArrayList<Document>();
			
		already.put(id, true);
			
		return retrieveAllowedDocuments(id);
	}

	protected abstract List<Document> retrieveAllowedDocuments(long id);
	
	public synchronized void cleanQuery(List<String> must, List<String> must_not){
		
		long id = getId(must, must_not);
		
//		getIdTable().remove(getTableId().remove(id));
		
		already.remove(id);
		
		cleanQueryInternal(id);
		
	}

	protected abstract void cleanQueryInternal(long id);

	public synchronized Document[] getDocuments(TextQuery[] queries,
			double documentsPerquery){
		
		for (int i = 0; i < queries.length; i++) {
			
			ArrayList<String> must = new ArrayList<String>();
			
			ArrayList<String> must_not = new ArrayList<String>();

			QueryParser.parseQuery(queries[i], must, must_not);
			
			doSearch(must, must_not);
		
		}
		
		List<Document> ret = new ArrayList<Document>();
		
		for (int i = 0; i < queries.length; i++) {
			
			ArrayList<String> must = new ArrayList<String>();
			
			ArrayList<String> must_not = new ArrayList<String>();
			
			QueryParser.parseQuery(queries[i], must, must_not);
			
			List<Document> docs = retrieveMaxAllowedDocuments(must,must_not);
			
			for (int j = 0; j < docs.size() && (j <= documentsPerquery || documentsPerquery<0); j++) {
				
				ret.add(docs.get(j));
				
			}

			cleanQuery(must,must_not);
			
			System.gc();
		}
		
		return ret.toArray(new Document[ret.size()]);
		
	}

	protected synchronized List<String> getMustWords(long queryId) {
		
		return getTableId().get(queryId).getMustWords();
		
	}

	protected synchronized List<String> getMustNotWords(long queryId) {
		return getTableId().get(queryId).getMustNotWords();
	}

	public synchronized void cleanSearcher() {
		
		if (already != null)
			already.clear();
		if (idTable != null)
			idTable.clear();
		if (tableId != null)
			tableId.clear();
		cleanSearcherInternal();
	}

	protected abstract void cleanSearcherInternal();

	public synchronized void storeMaxAllowedDocuments(List<String> must_words,
			List<String> must_not_words) {
		
		long id = getId(must_words, must_not_words);
		
		if (already.get(id) != null && already.get(id))
			return;
		
		already.put(id, true);

		storeAllowedDocuments(id);
		
	}

	protected abstract void storeAllowedDocuments(long id);

	public abstract void finishNegativeBatchDownloader(ExperimentEnum experimentConsistensyId, int databaseId, String computerName, int goodState, int split);

	public abstract void finishBatchDownloader(ExperimentEnum experimentConsistensyId, int databaseId, String computerName, int goodState);
	
}
