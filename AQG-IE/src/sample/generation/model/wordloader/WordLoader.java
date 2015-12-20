package sample.generation.model.wordloader;

import java.util.Map;

import utils.persistence.persistentWriter;
import exploration.model.Database;

public abstract class WordLoader {

	public abstract Map<String,Long> loadInitialWords(persistentWriter pW, Database database, int version_seed);

}
