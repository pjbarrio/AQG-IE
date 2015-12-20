package exploration.model.clusterfunction;

import execution.model.parameters.Parametrizable;
import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.source.similarity.SimilarityFunction;

public class ClusterFunction {

	public ClusterFunction(ClusterFunctionEnum enumValue) {
		this.enumValue = enumValue;
	}

	public static ClusterFunction generateInstance(String string, Parametrizable parametrizable) {
		
		switch (ClusterFunctionEnum.valueOf(string)) {
		
		case CLOSENESS_CLASSIFICATION:
			
			return new ClassificationClusterFunction(ClusterFunctionEnum.CLOSENESS_CLASSIFICATION);
		
		case CLASSIFICATION:
			
			return new ClassificationClusterFunction(ClusterFunctionEnum.CLASSIFICATION);
		
		case GLOBAL:
			
			return new GlobalClusterFunction(ClusterFunctionEnum.GLOBAL);
			
		case K_MEANS:
			
			return new K_Means(ClusterFunctionEnum.K_MEANS,Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.K_MEANS_K_VALUE).getString()), SimilarityFunction.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.K_MEANS_SIMILARITY_FUNCTION).getString()));

		case FUZZY_C_MEANS:
			
			return new FuzzyCMeans(ClusterFunctionEnum.FUZZY_C_MEANS,Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.FUZZY_C_K_VALUE).getString()), SimilarityFunction.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.FUZZY_C_SIMILARITY_FUNCTION).getString()));
			
		default:
			
			return null;
		}
		
	}

	private ClusterFunctionEnum enumValue;

	public ClusterFunctionEnum getEnum() {
		return enumValue;
	}

}
