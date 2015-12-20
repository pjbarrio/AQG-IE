package sample.generation.model.wordloader.impl;

import java.util.List;
import java.util.Map;

import online.sample.wordsDistribution.WordsDistributionLoader;

import exploration.model.Database;
import sample.generation.model.wordloader.WordLoader;
import utils.persistence.persistentWriter;

public class InWebsiteWordLoader extends WordLoader {

	@Override
	public Map<String,Long> loadInitialWords(persistentWriter pW, Database database, int version_seed) {
		
		return WordsDistributionLoader.loadFile(pW.getSourceWords(database.getName()));
		
	}

}
