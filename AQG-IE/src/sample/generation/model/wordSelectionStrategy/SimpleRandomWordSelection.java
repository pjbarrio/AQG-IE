package sample.generation.model.wordSelectionStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import exploration.model.Database;
import exploration.model.Document;

import sample.generation.model.wordloader.WordLoader;
import utils.persistence.persistentWriter;

public class SimpleRandomWordSelection implements SimpleWordSelectionStrategy{

	List<String> words;
	
	private WordLoader wordLoader;

	public SimpleRandomWordSelection(WordLoader wordLoader) {
		this.wordLoader = wordLoader;
	}

	@Override
	public String selectWord() {
		
		if (words.isEmpty())
			return null;
		
		return words.get((int)(Math.random()*(double)words.size()));
		
	}

	@Override
	public void initialize(Database database, persistentWriter pW, int version_seed) {
		
		Map<String, Long> wordsMap = wordLoader.loadInitialWords(pW,database, version_seed);	
		
		words = new ArrayList<String>(wordsMap.keySet());
		
		Collections.shuffle(words);
		
	}

	@Override
	public void update(Document document) {
		;
	}

	@Override
	public int getNumberOfWordsToChooseFrom() {
		return words.size();
	}

	@Override
	public void initialize(List<String> initialList) {
		words = new ArrayList<String>(initialList);		
	}

}
