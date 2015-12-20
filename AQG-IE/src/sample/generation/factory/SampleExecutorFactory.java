package sample.generation.factory;

import java.util.HashMap;
import java.util.Map;

import sample.generation.model.CachedCompositeSampleExecutor;
import sample.generation.model.CompositeSampleExecutor;
import sample.generation.model.RoundRobinCachedCompositeSampleExecutor;
import sample.generation.model.SampleExecutor;
import sample.generation.model.SampleGenerator;
import sample.generation.model.impl.SimpleSampleExecutor;
import utils.persistence.persistentWriter;
import execution.model.parameters.Parametrizable;
import exploration.model.Database;
import exploration.model.clusterfunction.ClusterFunction;
import exploration.model.clusterfunction.GlobalClusterFunction;
import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.SampleExecutorEnum;
import extraction.relationExtraction.RelationExtractionSystem;

public class SampleExecutorFactory {

	public static SampleExecutor generateInstance(RelationExtractionSystem res, String string, SampleGenerator sampleGenerator,
			Parametrizable parameters, persistentWriter pW, int sampleConfiguration) {
		
		switch (SampleExecutorEnum.valueOf(string)){
		
		case CLUSTER:
			return new CompositeSampleExecutor(ClusterFunction.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION_PARAMETERS)),
					CardinalityFunctionFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_PARAMETERS)),sampleGenerator,Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.ALEATORIZE_DBS).getString())){

						@Override
						public boolean samples(Database database) {
							return database.isCluster();
						}
				
			};
			
		case GLOBAL:
			return new CompositeSampleExecutor(new GlobalClusterFunction(ClusterFunctionEnum.GLOBAL),
					CardinalityFunctionFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_PARAMETERS)),sampleGenerator,Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.ALEATORIZE_DBS).getString())){

						@Override
						public boolean samples(Database database) {
							return database.isGlobal();
						}
				
			};	
		case CACHED_CLUSTER:
			
			return new CachedCompositeSampleExecutor(ClusterFunction.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION_PARAMETERS)),
					CardinalityFunctionFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_PARAMETERS)), 
					sampleConfiguration, res,pW,Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.ALEATORIZE_DBS).getString())){

						@Override
						public boolean samples(Database database) {
							return database.isCluster();
						}
				
			};
			
		case CACHED_GLOBAL:
			
			return new CachedCompositeSampleExecutor(new GlobalClusterFunction(ClusterFunctionEnum.GLOBAL),
					CardinalityFunctionFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_PARAMETERS)), 
					sampleConfiguration, res, pW,Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.ALEATORIZE_DBS).getString())){

						@Override
						public boolean samples(Database database) {
							return database.isGlobal();
						}
				
			};
			
		case ROUND_ROBIN_CACHED_CLUSTER:
			
			return new RoundRobinCachedCompositeSampleExecutor(ClusterFunction.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION_PARAMETERS)),
					CardinalityFunctionFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_PARAMETERS)), 
					sampleConfiguration, pW, res,Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.ALEATORIZE_DBS).getString())){

						@Override
						public boolean samples(Database database) {
							return database.isCluster();
						}
				
			};
			
		case ROUND_ROBIN_CACHED_GLOBAL:
			
			return new RoundRobinCachedCompositeSampleExecutor(new GlobalClusterFunction(ClusterFunctionEnum.GLOBAL),
					CardinalityFunctionFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_PARAMETERS)), 
					sampleConfiguration, pW, res,Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.ALEATORIZE_DBS).getString())){

						@Override
						public boolean samples(Database database) {
							return database.isGlobal();
						}
				
			};	
			
		case SIMPLE:
			return new SimpleSampleExecutor(sampleGenerator);
			
		default:
			return null;
	}
		
	}


}
