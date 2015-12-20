package sample.generation.factory;

import execution.workload.impl.wordextraction.RDFWordExtractor;
import exploration.model.enumerations.WordExtractorEnum;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordExtractorAbs;

public class WordExtractorFactory {

	public static WordExtractorAbs generateInstance(String string, ContentLoader cl,
			ContentExtractor ce) {
		
		switch (WordExtractorEnum.valueOf(string)){
		
		case ALL: return new WordExtractor(ce, cl);
		
		default: return null;
		
		}
		
	}

}
