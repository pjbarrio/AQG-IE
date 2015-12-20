package execution.model.factory;

import execution.model.adaptivestrategy.condition.performance.PerformanceChecker;
import execution.model.adaptivestrategy.condition.performance.PrecisionCalculator;
import execution.model.adaptivestrategy.condition.performance.UselessDocumentsCalculator;
import execution.model.adaptivestrategy.condition.performance.UselessQueriesCalculator;
import execution.model.adaptivestrategy.condition.performance.UselessQueryCondition;
import execution.model.parameters.Parametrizable;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.PerformanceCheckerEnum;

public class PerformanceCheckerFactory {

public static PerformanceChecker getInstance(String string,Parametrizable parametrizable) {
		
		switch (PerformanceCheckerEnum.valueOf(string)) {
		
		case LOW_PRECISION:
			
			return new PrecisionCalculator();

		case BAD_DOCUMENTS_PROCESSED:
			
			return new UselessDocumentsCalculator();
			
		case BAD_QUERIES_PROCESSED:
			
			return new UselessQueriesCalculator(UselessQueryCondition.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.USELESS_QUERY_CONDITION).getString(),Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.USELESS_QUERY_THRESHOLD).getString())));
		
		default:
			
			return null;
		
		}
		
	
	}
	
}
