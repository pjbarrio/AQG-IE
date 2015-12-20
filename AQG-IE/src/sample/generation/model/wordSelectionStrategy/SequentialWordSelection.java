package sample.generation.model.wordSelectionStrategy;

import java.util.List;

import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Document;

public class SequentialWordSelection implements SimpleWordSelectionStrategy {

	private List<String> initialQueries;
	private int index;
	private String collection;

	public SequentialWordSelection(String collection){
		this.collection = collection;
	}
	
	@Override
	public String selectWord() {
		
		if (index >= initialQueries.size())
			return null;
		
		String word = initialQueries.get(index);
		
		index++;
		
		return word;
	}

	@Override
	public void initialize(Database database, persistentWriter pW,
			int version_seed) {
		
		initialQueries = pW.loadInitialRandomQueries(collection,version_seed);

		index = 0;
		
	}

	@Override
	public void update(Document document) {
		;
	}

	@Override
	public int getNumberOfWordsToChooseFrom() {
		return initialQueries.size();
	}

	@Override
	public void initialize(List<String> initialList) {
		
		initialQueries = initialList;

		index = 0;
		

	}

}
