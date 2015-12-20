package exploration.model.clusterfunction;

import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.source.similarity.SimilarityFunction;

public class K_Means extends ClusterFunction {

	private int k_value;
	private SimilarityFunction similarity;

	public K_Means(ClusterFunctionEnum kMeans, int k_Value,
			SimilarityFunction similarity){
		super(kMeans);
		this.k_value = k_Value;
		this.similarity = similarity;
	}

}
