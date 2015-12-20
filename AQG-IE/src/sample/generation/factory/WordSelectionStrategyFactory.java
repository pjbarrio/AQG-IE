package sample.generation.factory;

import online.documentHandler.contentExtractor.ContentExtractor;
import exploration.model.enumerations.WordSelectionStrategyEnum;
import sample.generation.model.WordSelectionStrategy;
import sample.generation.model.wordSelectionStrategy.NoSelectionWordSelection;
import sample.generation.model.wordSelectionStrategy.OneAtATimeWordSelectionStrategy;
import sample.generation.model.wordSelectionStrategy.RandomWordSelectionStrategy;
import sample.generation.model.wordSelectionStrategy.SimpleAvgTFWordSelection;
import sample.generation.model.wordSelectionStrategy.SimpleRandomWordSelection;
import sample.generation.model.wordSelectionStrategy.SimpleUpdatableRandomWordSelection;
import sample.generation.model.wordloader.WordLoader;
import utils.word.extraction.WordExtractor;

public class WordSelectionStrategyFactory {

	public static WordSelectionStrategy generateInstance(String name, WordExtractor wordExtractor, WordLoader wordLoader) {
		
		switch (WordSelectionStrategyEnum.valueOf(name)) {
		case RANDOM_RANDOM:
			return new RandomWordSelectionStrategy(new SimpleRandomWordSelection(wordLoader),new SimpleUpdatableRandomWordSelection(wordExtractor));
		case RANDOM_COLLECTED:
			return new OneAtATimeWordSelectionStrategy(new SimpleRandomWordSelection(wordLoader),new SimpleUpdatableRandomWordSelection(wordExtractor));
		case RANDOM_INITIAL: 
			return new RandomWordSelectionStrategy(new SimpleRandomWordSelection(wordLoader),new NoSelectionWordSelection());
		case RANDOM_INITIAL_AVERAGE_COLLECTED:
			return new OneAtATimeWordSelectionStrategy(new SimpleRandomWordSelection(wordLoader),new SimpleAvgTFWordSelection(wordExtractor));
		case RANDOM_RANDOM_AVERAGE:
			return new RandomWordSelectionStrategy(new SimpleRandomWordSelection(wordLoader),new SimpleAvgTFWordSelection(wordExtractor));
		
		default:
			return null;
		}
		
	}

}
