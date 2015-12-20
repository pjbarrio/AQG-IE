package exploration.model.clusterfunction;

import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.source.similarity.SimilarityFunction;

public class FuzzyCMeans extends ClusterFunction {

	private int k_values;
	private SimilarityFunction similarity;

	public FuzzyCMeans(ClusterFunctionEnum enumValue, int k_values,
			SimilarityFunction similarity) {
		super(enumValue);
		this.k_values = k_values;
		this.similarity = similarity;
	
	}

}
