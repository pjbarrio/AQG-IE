package sample.generation.factory;

import sample.generation.model.performanceChecker.QueryPerformanceChecker;
import sample.generation.model.performanceChecker.impl.PrecisionPerformanceChecker;
import execution.model.parameters.Parametrizable;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.QueryPerformanceEnumeration;

public class QueryPerformanceCheckerFactory {

	public static QueryPerformanceChecker generateInstance(String name,
			Parametrizable parameters) {
		
		switch (QueryPerformanceEnumeration.valueOf(name)) {
		case MIN_PRECISION:
			
			return new PrecisionPerformanceChecker(Double.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.MINIMUM_PRECISION_IN_RESULTS).getString()));

		default:
			return null;
		}
		
	}

}
