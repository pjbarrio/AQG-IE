package sample.generation.model.wordSelectionStrategy;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import exploration.model.Database;
import exploration.model.Document;

import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;

public class SimpleAvgTFWordSelection implements SimpleWordSelectionStrategy {

	private WordExtractor wordExtractor;
	private Map<String, Long> collectedWords;
	private HashMap<String, Long> documentFrequency;
	private Set<String> returned;
	private Set<String> stopWords;
	private persistentWriter pW;

	public SimpleAvgTFWordSelection(WordExtractor wordExtractor) {
		this(wordExtractor,new HashSet<String>());
	}

	public SimpleAvgTFWordSelection(WordExtractor wordExtractor,
			Set<String> stopWords) {
		
		this.wordExtractor = wordExtractor;
		this.stopWords = stopWords;
		
	}

	@Override
	public String selectWord() {
		
		double max = -1;
		String word = null;
		
		for (Entry<String, Long> entry : documentFrequency.entrySet()) {

			if (hasBeenReturned(entry.getKey()))
				continue;
			
			Long ctf = collectedWords.get(entry.getKey());
			
			double avg_tf = (double)ctf / (double)entry.getValue();
			
			if (avg_tf > max){
				max = avg_tf;
				word = entry.getKey();
			}
			
		}
		if (word != null)
			markAsReturned(word);
		
		return word;
		
	}

	private boolean hasBeenReturned(String key) {
		return getReturned().contains(key);
	}

	private Set<String> getReturned() {
		if (returned == null){
			returned = new HashSet<String>();
		}
		return returned;
	}
	

	private void markAsReturned(String word) {
		getReturned().add(word);		
	}

	@Override
	public void initialize(Database database, persistentWriter pW, int version_seed) {
		getReturned().clear();
		collectedWords = new HashMap<String,Long>();
		documentFrequency = new HashMap<String, Long>();
		this.pW= pW;
	}

	@Override
	public void update(Document document) {
		
		String[] words = wordExtractor.getWords(document, false, true, false,pW);
		
		updateFrequencies(words);
		
	}

	private void updateFrequencies(String[] words) {
		
		Set<String> added = new HashSet<String>();
		
		for (int i = 0; i < words.length; i++) {
			
			if (stopWords.contains(words[i]))
				continue;
			
			if (added.add(words[i])){
				
				Long freq = documentFrequency.get(words[i]);
				
				if (freq == null){
					freq = new Long(0);
				}
				
				documentFrequency.put(words[i], freq + 1);
				
			}
				
			
			Long freq = collectedWords.get(words[i]);
			
			if (freq == null){
				freq = new Long(0);
			}
			
			collectedWords.put(words[i], freq + 1);
		}
		
		added.clear();
		
	}

	@Override
	public int getNumberOfWordsToChooseFrom() {
		return collectedWords.size();
	}

	@Override
	public void initialize(List<String> initialList) {
		getReturned().clear();
		collectedWords = new HashMap<String,Long>();
		documentFrequency = new HashMap<String, Long>();		
	}
}
