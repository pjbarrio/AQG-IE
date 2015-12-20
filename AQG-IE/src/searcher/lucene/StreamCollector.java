package searcher.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;

import exploration.model.Document;

public class StreamCollector extends Collector {

	private Scorer scorer;
    private int docBase;
	private Searcher searcher;
	private Hashtable<Document, Float> scores;
	private ScoreComparator<Document> scoreComparator;
	private ArrayList<Document> sortedDocs;
	
    public StreamCollector(Searcher searcher){
    	
    	this.searcher = searcher;
    
    	scores = new Hashtable<Document, Float>();
    	
    	scoreComparator = new ScoreComparator<Document>(scores);
    	
    	sortedDocs = new ArrayList<Document>();
    }
    
    public void collect(int doc) throws IOException {

    	String docPath = searcher.doc(doc).get("path");
    	
//    	scores.put(docPath, scorer.score());
//    	
//    	sortedDocs.add(docPath);
    	
    	throw new UnsupportedOperationException("FIX!");
    	
    }

    public boolean acceptsDocsOutOfOrder() {
      return true;
    }

    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      this.docBase = docBase;
    }

  
    public void setScorer(Scorer scorer) throws IOException {
      this.scorer = scorer;
    }

	public void sortbyScore() {
		
		Collections.sort(sortedDocs, scoreComparator);
	
		//TODO REMOVE THIS LINE TO PRESERVE SCORES!	
		scores.clear();
		
		scores = new Hashtable<Document, Float>(0);
		
	}

	public Hashtable<Document, Float> getScores() {
		
		return scores;
		
	}

	public ArrayList<Document> getSortedDocuments() {
		
		return sortedDocs;
	
	}

}