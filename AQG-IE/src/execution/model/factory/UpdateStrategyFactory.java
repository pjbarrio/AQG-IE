package execution.model.factory;

import execution.model.parameters.Parametrizable;
import execution.model.updateStrategy.AccumulativeUpdateStrategy;
import execution.model.updateStrategy.RegenerativeUpdateStrategy;
import execution.model.updateStrategy.UpdateStrategy;
import exploration.model.enumerations.CollectingDocumentStrategyEnum;

public class UpdateStrategyFactory {

	public static UpdateStrategy generateInstance(String string,Parametrizable parametrizable) {
		
		switch (CollectingDocumentStrategyEnum.valueOf(string)) {
		
		case ACCUMULATIVE:
			
			return new AccumulativeUpdateStrategy();

		case REGENERATIVE:
		
			return new RegenerativeUpdateStrategy();
		
		default:
			
			return null;
		
		}
		
	}
	
}
