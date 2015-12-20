package sample.generation.factory;

import sample.generation.model.performanceChecker.QueryPoolPerformanceChecker;
import sample.generation.model.performanceChecker.impl.NoUsefulDocumentsInKQueries;
import sample.generation.model.performanceChecker.impl.UnderPrecisionInKQueries;
import execution.model.parameters.Parametrizable;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.QueryPoolPerformanceEnumeration;

public class QueryPoolPerformanceCheckerFactory {

	public static QueryPoolPerformanceChecker generateInstance(String name,
			Parametrizable parameters) {
		
		switch (QueryPoolPerformanceEnumeration.valueOf(name)) {
		
		case K_USELESS_QUERIES:
			
			return new NoUsefulDocumentsInKQueries(Integer.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.NUMBER_OF_USELESS_QUERIES).getString()));

		case UNDER_PRECISION:
			
			return new UnderPrecisionInKQueries(Integer.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.NUMBER_OF_QUERIES).getString()),
					Double.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.PRECISION_OF_QUERY_POOL).getString()));
			
		default:
			return null;
		}
		
	}

}
