package sample.generation.factory;

import sample.generation.model.wordloader.WordLoader;
import sample.generation.model.wordloader.impl.InWebsiteWordLoader;
import sample.generation.model.wordloader.impl.OtherSource;
import execution.model.parameters.Parametrizable;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.WordLoaderEnum;

public class WordLoaderFactory {

	public static WordLoader generateInstance(String name, Parametrizable parameter) {
		
		switch (WordLoaderEnum.valueOf(name)){
		
		case IN_WEBSITE:
			
			return new InWebsiteWordLoader();
		
		case OTHER_SOURCE:
			
			return new OtherSource(parameter.loadParameter(ExecutionAlternativeEnum.QUERY_OTHER_SOURCE).getString());
			
		default:
			return null;
		}
		
	}

}
