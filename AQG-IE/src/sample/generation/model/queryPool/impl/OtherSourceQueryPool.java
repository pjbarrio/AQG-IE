package sample.generation.model.queryPool.impl;

import java.util.List;

import sample.generation.model.WordSelectionStrategy;
import sample.generation.model.queryPool.QueryPool;
import utils.persistence.persistentWriter;
import execution.workload.querygeneration.QueryGenerator;
import exploration.model.Database;
import exploration.model.Document;

public class OtherSourceQueryPool extends QueryPool<String> {

	private WordSelectionStrategy wordSelectionStrategy;

	public OtherSourceQueryPool(boolean isForUseful, WordSelectionStrategy wordSelectionStrategy, QueryGenerator<String> queryGenerator) {
		
		super(queryGenerator,isForUseful);
		
		this.wordSelectionStrategy = wordSelectionStrategy;
		

	}

	@Override
	public void updateQueries(Document document) {
		
		wordSelectionStrategy.update(document);
				
	}

	@Override
	public void _initialize(Database database, persistentWriter pW, int version_seed, boolean reverse, int numberOfQueries) {
		
		wordSelectionStrategy.initialize(database,pW, version_seed);		
				
	}

	@Override
	public void fetchQuery() {
		
		String word;
		
		do {
			
			word = wordSelectionStrategy.selectWord();
		
		} while (word != null && !addQuery(word));
				
	
	}

	@Override
	protected void _initialize(List<String> initialList) {
		wordSelectionStrategy.initialize(initialList);		
	}

}
