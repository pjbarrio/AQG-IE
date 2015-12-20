package sample.generation.model.wordSelectionStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;
import exploration.model.Database;
import exploration.model.Document;

public class SimpleUpdatableRandomWordSelection implements
		SimpleWordSelectionStrategy {

	private WordExtractor wordExtractor;
	private List<String> words;
	private Set<String> wordsSet;
	private persistentWriter pW;
	
	
	public SimpleUpdatableRandomWordSelection(WordExtractor wordExtractor) {
		this.wordExtractor = wordExtractor;
	}

	@Override
	public String selectWord() {
		
		if (words.isEmpty())
			return null;
		
		return words.get((int)(Math.random()*(double)words.size()));
	}

	@Override
	public void initialize(Database database, persistentWriter pW, int version_seed) {
		wordsSet = new HashSet<String>();
		words = new ArrayList<String>();
		this.pW = pW;
	}

	@Override
	public void update(Document document) {
		
		String[] words = wordExtractor.getWords(document, true, true, false,pW);
		
		updateList(words);

	}

	private void updateList(String[] words) {
		
		for (int i = 0; i < words.length; i++) {
			if (wordsSet.add(words[i])){
				this.words.add(words[i]);
			}
		}
			
	}

	@Override
	public int getNumberOfWordsToChooseFrom() {
		return words.size();
	}

	@Override
	public void initialize(List<String> initialList) {
		wordsSet = new HashSet<String>();
		words = new ArrayList<String>();		
	}

}
