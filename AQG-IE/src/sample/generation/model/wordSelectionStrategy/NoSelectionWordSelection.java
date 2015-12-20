package sample.generation.model.wordSelectionStrategy;

import java.util.List;
import java.util.Map;

import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Document;

public class NoSelectionWordSelection implements SimpleWordSelectionStrategy {

	@Override
	public String selectWord() {
		return null;
	}

	@Override
	public void initialize(Database database, persistentWriter pW, int version_seed) {
		;
		
	}

	@Override
	public void update(Document document) {
		;
	}

	@Override
	public int getNumberOfWordsToChooseFrom() {
		return 0;
	}

	@Override
	public void initialize(List<String> initialList) {
		;		
	}


}
