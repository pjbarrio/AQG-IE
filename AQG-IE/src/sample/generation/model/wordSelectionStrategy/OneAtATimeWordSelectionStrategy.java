package sample.generation.model.wordSelectionStrategy;

import java.util.List;

import exploration.model.Database;
import exploration.model.Document;

import sample.generation.model.WordSelectionStrategy;
import utils.persistence.persistentWriter;

public class OneAtATimeWordSelectionStrategy extends WordSelectionStrategy {

	private SimpleWordSelectionStrategy initialWordSelection;
	private SimpleWordSelectionStrategy currentWordSelection;

	public OneAtATimeWordSelectionStrategy(
			SimpleWordSelectionStrategy initialWordSelection,
			SimpleWordSelectionStrategy currentWordSelection) {
		
		this.initialWordSelection = initialWordSelection;
		this.currentWordSelection = currentWordSelection;
		
	}

	@Override
	public String _selectWord() {
		
		String word = currentWordSelection.selectWord();
		
		if (word == null){
			return initialWordSelection.selectWord();
		}
		return word;
		
	}

	@Override
	public void _initialize(Database database, persistentWriter pW, int version_seed) {
		initialWordSelection.initialize(database, pW, version_seed);
		currentWordSelection.initialize(database, pW, version_seed);
	}

	@Override
	public void update(Document document) {
		currentWordSelection.update(document);		
	}

	@Override
	protected int getNumberOfWordsToChooseFrom() {
		return initialWordSelection.getNumberOfWordsToChooseFrom() + currentWordSelection.getNumberOfWordsToChooseFrom();
	}

	@Override
	public void _initialize(List<String> initialList) {
		initialWordSelection.initialize(initialList);
		currentWordSelection.initialize(initialList);
	}

}
