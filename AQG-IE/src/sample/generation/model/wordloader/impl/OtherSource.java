package sample.generation.model.wordloader.impl;

import java.util.List;
import java.util.Map;

import online.sample.wordsDistribution.WordsDistributionLoader;

import exploration.model.Database;
import sample.generation.model.wordloader.WordLoader;
import utils.persistence.persistentWriter;

public class OtherSource extends WordLoader {

	private String source;

	public OtherSource(String source) {
		this.source = source;
	}

	@Override
	public Map<String,Long> loadInitialWords(persistentWriter pW, Database database, int version_seed) {
		return WordsDistributionLoader.loadFile(pW.getSourceWords(source,version_seed));
	}

}
