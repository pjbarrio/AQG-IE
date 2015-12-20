package searcher.lucene;

import java.util.Comparator;
import java.util.Hashtable;

public class ScoreComparator<String> implements Comparator<String> {

	private Hashtable<String, Float> scores;

	public ScoreComparator(Hashtable<String, Float> scores) {
		this.scores = scores;
	}

	public int compare(String doc1, String doc2) {
	
		Float f1 = scores.get(doc1);
		Float f2 = scores.get(doc2);
		
		return f2.compareTo(f1);
	
	}

}
