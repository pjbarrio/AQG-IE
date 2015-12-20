package execution.model.factory;

import execution.model.Generation;
import execution.model.Source;
import execution.model.generation.CombinedGeneration;
import execution.model.generation.SimpleGeneration;
import execution.model.parameters.Parametrizable;
import exploration.model.Configuration;
import exploration.model.DatabasesModel;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.GenerationEnum;

public class GenerationFactory {

	public static Generation generateInstance(String string,Parametrizable parametrizable,DatabasesModel dm) {
		
		switch (GenerationEnum.valueOf(string)) {
		
		case SIMPLE:
			
			return new SimpleGeneration();

		case COMBINED:
			
			return new CombinedGeneration(SourceFactory.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.CROSSABLE_SOURCE).getString(), parametrizable.loadParameter(ExecutionAlternativeEnum.CROSSABLE_SOURCE_PARAMETERS), dm, Source.SOURCE_UNIQUE),
					Configuration.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.CROSSABLE_CONFIGURATION).getString()));
	
			
		default:
			
			return null;
		
		}
		
	}
	
}
