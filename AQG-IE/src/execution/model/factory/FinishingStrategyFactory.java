package execution.model.factory;

import execution.model.FinishingStrategy;
import execution.model.finishingstrategy.GlobalFinishingStrategy;
import execution.model.finishingstrategy.HybridFinishingStrategy;
import execution.model.finishingstrategy.LocalFinishingStrategy;
import execution.model.finishingstrategy.NoneFinishingStrategy;
import execution.model.parameters.Parametrizable;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.FinishingStrategyEnum;

public class FinishingStrategyFactory {

	public static FinishingStrategy generateInstance(String string, int afterNDocuments,Parametrizable parametrizable) {
		
		switch (FinishingStrategyEnum.valueOf(string)) {
			
		case LOCAL:
			
			return new LocalFinishingStrategy(afterNDocuments,PerformanceCheckerFactory.getInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.DOCUMENTS_PERFORMANCE_COMPARATOR).getString(),parametrizable.loadParameter(ExecutionAlternativeEnum.DOCUMENT_PERFORMANCE_COMPARATOR_PARAMETERS)),Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.DOCUMENTS_PERFORMANCE_THRESHOLD).getString()));

		case GLOBAL:
			
			return new GlobalFinishingStrategy(afterNDocuments,PerformanceCheckerFactory.getInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_FINISHING).getString(), parametrizable.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_FINISHING_PARAMETERS)),
					Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.QUERIES_PERFORMANCE_THRESHOLD).getString()));
		
		case HYBRID:
			
			return new HybridFinishingStrategy(afterNDocuments,new LocalFinishingStrategy(afterNDocuments,PerformanceCheckerFactory.getInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.DOCUMENTS_PERFORMANCE_COMPARATOR).getString(),parametrizable.loadParameter(ExecutionAlternativeEnum.DOCUMENT_PERFORMANCE_COMPARATOR_PARAMETERS)),Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.DOCUMENTS_PERFORMANCE_THRESHOLD).getString())),
					new GlobalFinishingStrategy(afterNDocuments,PerformanceCheckerFactory.getInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_FINISHING).getString(),parametrizable.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_FINISHING_PARAMETERS)),
							Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.QUERIES_PERFORMANCE_THRESHOLD).getString())));
			
		default:
			
			return new NoneFinishingStrategy(afterNDocuments);
		}
		
	}

	
}
