package sample.generation.model.wordSelectionStrategy;

import java.util.List;

import exploration.model.Database;
import exploration.model.Document;

import sample.generation.model.WordSelectionStrategy;
import utils.persistence.persistentWriter;

public class RandomWordSelectionStrategy extends WordSelectionStrategy {

	private SimpleWordSelectionStrategy initialSelection;
	private SimpleWordSelectionStrategy collectedSelection;

	public RandomWordSelectionStrategy(
			SimpleWordSelectionStrategy initialSelection,
			SimpleWordSelectionStrategy collectedSelection) {

		this.initialSelection = initialSelection;
		this.collectedSelection = collectedSelection;
		
	}

	@Override
	public String _selectWord() {

		String word = null;
		
		if (Math.random() < 0.5){
			
			word = initialSelection.selectWord();
			
		}else{
		
			word = collectedSelection.selectWord();
		}
		
		if (word == null){
			word = initialSelection.selectWord();
		}
		
		return word;
		
	}

	@Override
	public void _initialize(Database database, persistentWriter pW, int version_seed) {
		initialSelection.initialize(database,pW, version_seed);
		collectedSelection.initialize(database, pW, version_seed);
	}

	@Override
	public void update(Document document) {
		collectedSelection.update(document);
		
	}

	@Override
	protected int getNumberOfWordsToChooseFrom() {
		return initialSelection.getNumberOfWordsToChooseFrom() + collectedSelection.getNumberOfWordsToChooseFrom();
	}

	@Override
	public void _initialize(List<String> initialList) {
		initialSelection.initialize(initialList);	
		collectedSelection.initialize(initialList);
	}

}
