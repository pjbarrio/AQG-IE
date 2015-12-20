package execution.model.factory;

import utils.persistence.persistentWriter;
import execution.model.AdaptiveStrategy;
import execution.model.adaptivestrategy.AdaptiveAdaptiveStrategy;
import execution.model.adaptivestrategy.StaticAdaptiveStrategy;
import execution.model.adaptivestrategy.algorithm.AdaptationAlgorithm;
import execution.model.adaptivestrategy.condition.AdaptationCondition;
import execution.model.parameters.Parametrizable;
import exploration.model.DatabasesModel;
import exploration.model.enumerations.AdaptiveStrategyEnum;
import exploration.model.enumerations.ExecutionAlternativeEnum;

public class AdaptiveStrategyFactory {

	public static AdaptiveStrategy generateInstance(String string, int afterNDocuments, Parametrizable parametrizable, persistentWriter pW, DatabasesModel dm) {
		
		switch (AdaptiveStrategyEnum.valueOf(string)) {
		
		case STATIC:
			
			return new StaticAdaptiveStrategy();

		case ADAPTIVE:
			
			return new AdaptiveAdaptiveStrategy(afterNDocuments,AdaptationCondition.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.ADAPTATION_CONDITION).getString(),parametrizable.loadParameter(ExecutionAlternativeEnum.ADAPTATION_CONDITION_PARAMETERS)),
					AdaptationAlgorithm.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.ADAPTATION_STRATEGY).getString(),parametrizable.loadParameter(ExecutionAlternativeEnum.ADAPTATION_STRATEGY_PARAMETERS), pW, dm));
		
		default:
			
			return null;
		}
		
	}

	
}
