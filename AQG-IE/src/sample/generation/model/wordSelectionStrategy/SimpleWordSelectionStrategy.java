package sample.generation.model.wordSelectionStrategy;

import java.util.List;

import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Document;

public interface SimpleWordSelectionStrategy {

	public String selectWord();

	public void initialize(Database database, persistentWriter pW, int version_seed);

	public void update(Document document);

	public int getNumberOfWordsToChooseFrom();

	public void initialize(List<String> initialList);
	
}
