package sample.generation.factory;

import sample.generation.model.executor.QueryPoolExecutor;
import sample.generation.model.executor.impl.CyclicRescheduleQueryPoolExecutor;
import sample.generation.model.executor.impl.FQXtractQueryPoolExecutor;
import sample.generation.model.executor.impl.OpportunityQueryPoolExecutor;
import sample.generation.model.executor.impl.QuotaQueryPoolExecutor;
import sample.generation.model.executor.impl.SimpleCyclicQueryPoolExecutor;
import sample.generation.model.executor.impl.SimpleQueryPoolExecutor;
import sample.generation.model.executor.impl.StratifiedQueryPoolExecutor;
import sample.generation.model.queryPool.QueryPool;
import execution.model.parameters.Parametrizable;
import execution.model.parameters.StringParameters;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.QueryPoolExecutorEnum;

public class QueryPoolExecutorFactory {

	public static <T> QueryPoolExecutor generateInstance(String name,
			Parametrizable parameters, QueryPool<T> qp, int resultsPerQuery) {
		
		boolean reverse = false;
		int numberOfQueries = -1;
		
		if (parameters.containsParameter(ExecutionAlternativeEnum.REVERSE_QUERY_POOL)){
			reverse = Boolean.parseBoolean(parameters.loadParameter(ExecutionAlternativeEnum.REVERSE_QUERY_POOL).getString());
		}

		if (parameters.containsParameter(ExecutionAlternativeEnum.NUMBER_OF_QUERIES_FROM_POOL)){
			numberOfQueries = Integer.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.NUMBER_OF_QUERIES_FROM_POOL).getString());
		}

		

		
		
		switch (QueryPoolExecutorEnum.valueOf(name)) {
		case OPPORTUNITY:
			
			return new OpportunityQueryPoolExecutor<T>(qp, 
					QueryPoolPerformanceCheckerFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER_PARAMETERS)),
					QueryPerformanceCheckerFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER_PARAMETERS)),(int) (resultsPerQuery * Double.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.MEMORY_FOR_QUERIES).getString())));
		case QUOTA:
			
			return new QuotaQueryPoolExecutor<T>(qp,
					QueryPoolPerformanceCheckerFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER_PARAMETERS)),
					QueryPerformanceCheckerFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER_PARAMETERS)),(int) (resultsPerQuery * Double.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.MEMORY_FOR_QUERIES).getString())));
		case STRATIFIED:
			
			return new StratifiedQueryPoolExecutor<T>(qp,
					QueryPoolPerformanceCheckerFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER_PARAMETERS)),
					QueryPerformanceCheckerFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER_PARAMETERS)),(int) (resultsPerQuery * Double.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.MEMORY_FOR_QUERIES).getString())));
		
		case SMARTCYCLIC:
			
			return new CyclicRescheduleQueryPoolExecutor<T>(qp, 
					QueryPoolPerformanceCheckerFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER_PARAMETERS)),
					QueryPerformanceCheckerFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER_PARAMETERS)),(int) (resultsPerQuery * Double.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.MEMORY_FOR_QUERIES).getString())));
			
		case SIMPLE:
			
			return new SimpleQueryPoolExecutor<T>(qp, reverse,numberOfQueries); 
		
		case CYCLIC:
			
			return new SimpleCyclicQueryPoolExecutor<T>(qp, reverse,numberOfQueries);
		
		case FQXTRACT:
			
			return new FQXtractQueryPoolExecutor<T>(qp, 
					QueryPoolPerformanceCheckerFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER_PARAMETERS)),
					QueryPerformanceCheckerFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER_PARAMETERS)),(int) (resultsPerQuery * Double.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.MEMORY_FOR_QUERIES).getString())), Integer.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.MAX_DOCS_PER_QUERY).getString()));
	
			
		default:
			return null;
		}
		
	}

}
