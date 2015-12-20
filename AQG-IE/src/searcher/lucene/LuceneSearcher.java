package searcher.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.enumerations.ExperimentEnum;

import searcher.Searcher;
import searcher.lucene.analyzer.JavaSegmenterAnalyzer;
import searcher.lucene.demo.SearchFiles;
import utils.word.extraction.WordExtractor;



public class LuceneSearcher extends Searcher{
	
	private static final String MUST_SYMBOL = "+";
	private String index;
	private long hitsPerPage;
	private String field;
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private QueryParser parser;
	private SearchFiles sf;
	private Hashtable<Long, Integer> performedQueries;
	private Hashtable<Long, ArrayList<Document>> querySortedDocs;
	private long maxRetrievable;

	public LuceneSearcher(Database db,String Index,long l, long maxRetrievable, String stopWords){
		super(0,db);
		this.index = Index;
		this.hitsPerPage = l;
		this.maxRetrievable = maxRetrievable;
		this.field = "contents";
		
		try {
			
			Directory d = FSDirectory.open(new File(index));
			
			reader = IndexReader.open(d, true);
		
			d.close();
		
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} // only searching, so read-only=true
		
		searcher = new IndexSearcher(reader);
		
		try {
			
//			analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT,new File(stopWords));
		
			analyzer = new JavaSegmenterAnalyzer(Version.LUCENE_CURRENT,new File(stopWords),new WordExtractor(),false,true,false);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		parser = new QueryParser(Version.LUCENE_CURRENT, field, analyzer);
	
		sf = new SearchFiles();
		
		performedQueries = new Hashtable<Long, Integer>();
		
//		queryScores = new Hashtable<Long, Hashtable<String,Float>>();
		
		querySortedDocs = new Hashtable<Long, ArrayList<Document>>();
		
	}	
	
	@Override
	protected boolean executeSearch(long queryId, List<String> must_words, List<String> must_not_words,boolean createBatch, long waitbetweensearch){
				
		Integer session = performedQueries.get(new Long(queryId));

		if (session == null){ //First Time
			
			Query query;
			
			try {
				
				query = createBooleanQuery(must_words,must_not_words);				
				
				System.out.println("Searching for: " + query.toString(field));
	
			    sf.doStreamingSearch(searcher, query);
			    
			    performedQueries.put(new Long(queryId), new Integer(1));
						
//			    queryScores.put(new Long(queryId),sf.getScores());
			    
			    querySortedDocs.put(new Long(queryId),sf.getSortedDocuments());
			
			    return true;
			    
			} catch (IOException e) {
				
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			
			session = new Integer(session.intValue() + 1);
			
			performedQueries.put(new Long(queryId), session);
			
			return true;
			
		}
		
		return false;
		
	}
	
	private Query createBooleanQuery(List<String> must_words, List<String> must_not_words) throws ParseException {
		
		BooleanQuery bq = new BooleanQuery();
		
		for (String word : must_words) {
			
			Query tq = parser.parse(word);
			
			bq.add(tq,BooleanClause.Occur.MUST);
			
		}
		
		for (String word : must_not_words) {
			
			Query tq = parser.parse(word);
			
			bq.add(tq,BooleanClause.Occur.MUST_NOT);
			
		}
		  
		return bq;
	  }
	
	
	@Override
	protected List<Document> retrieveNextDocuments(long queryId){
				
		int session = performedQueries.get(new Long(queryId)).intValue();
		
		if (hitsPerPage<=0){
			if (session == 1)
				return retrieveMaxAllowedDocuments(getMustWords(queryId),getMustNotWords(queryId));
		}
		
		ArrayList<Document> arr = querySortedDocs.get(new Long(queryId));
				
		if ((session-1)*hitsPerPage >= Math.min(maxRetrievable,arr.size())){
			return new ArrayList<Document>(0);
		}
		
//		if (session*hitsPerPage > maxRetrievable)
//			already.put(queryId, true);
		
		return arr.subList((int) ((session-1)*hitsPerPage), (int)Math.min(session*hitsPerPage,Math.min(arr.size(),maxRetrievable)));
		
	}

	@Override
	protected List<Document> retrieveAllowedDocuments(long id) {
		
		return querySortedDocs.get(id);
		
	}

	public static ArrayList<String> getMustWords(String query) {
		
		ArrayList<String> ret = new ArrayList<String>();
		
		String[] spl = query.split(" ");
		for (String string : spl) {
			
			if (string.startsWith(MUST_SYMBOL));
				ret.add(string.substring(1));
			
		}
		
		return ret;
		
	}

	public static ArrayList<String> getMustNotWords(String query) {
		
		ArrayList<String> ret = new ArrayList<String>();
		
		String[] spl = query.split(" ");
		for (String string : spl) {
			
			if (string.startsWith(MUST_SYMBOL));
				ret.add(string.substring(1));
			
		}
		
		return ret;
	}

	@Override
	protected void cleanQueryInternal(long i) {
		
		performedQueries.remove(i);
		
		querySortedDocs.get(i).clear();
		
		querySortedDocs.put(i,new ArrayList<Document>(0));
		
		querySortedDocs.remove(i);
		
	}

	@Override
	protected void cleanSearcherInternal() {
		
		performedQueries.clear();
		
		querySortedDocs.clear();
		
	}

	@Override
	protected void storeAllowedDocuments(long id) {
		throw new UnsupportedOperationException("Must Implement");	//TODO Implement if necesary
	}

	@Override
	public void finishBatchDownloader(ExperimentEnum experimentConsistensyId,
			int databaseId, String computerName, int goodState) {
		throw new UnsupportedOperationException("Must Implement");		
	}

	@Override
	public void finishNegativeBatchDownloader(
			ExperimentEnum experimentConsistensyId, int databaseId,
			String computerName, int goodState, int split) {
		throw new UnsupportedOperationException("Must Implement");
	}

}
