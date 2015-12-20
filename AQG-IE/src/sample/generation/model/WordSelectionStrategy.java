package sample.generation.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Document;

public abstract class WordSelectionStrategy {

	private Set<String> processed;

	public String selectWord(){
		
		String word;
		
		do {

			System.out.println("finding a word");
			
			word = _selectWord();
						
		} while (word != null && hasProcessed(word) && hasMoreToRetrieve());
		
		if (word!=null && hasMoreToRetrieve())
			markAsProcessed(word);

		return word;		
		
	}

	private boolean hasMoreToRetrieve() {
		return getProcessed().size() < getNumberOfWordsToChooseFrom();
	}

	protected abstract int getNumberOfWordsToChooseFrom();

	private void markAsProcessed(String word) {
		getProcessed().add(word);
	}

	private Set<String> getProcessed() {
		
		if (processed == null){
			processed = new HashSet<String>();
		}
		return processed;
	}

	private boolean hasProcessed(String word) {
		return getProcessed().contains(word);
	}

	protected abstract String _selectWord();

	public final void initialize(Database database, persistentWriter pW, int version_seed){
		getProcessed().clear();
		_initialize(database,pW, version_seed);
	}

	protected abstract void _initialize(Database database, persistentWriter pW, int version_seed);

	public abstract void update(Document document);

	public void initialize(List<String> initialList){
		getProcessed().clear();
		_initialize(initialList);
	}
	
	public abstract void _initialize(List<String> initialList);

}
