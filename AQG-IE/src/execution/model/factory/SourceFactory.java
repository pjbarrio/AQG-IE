package execution.model.factory;

import execution.model.Source;
import execution.model.parameters.Parametrizable;
import execution.model.source.ClusteredSource;
import execution.model.source.GlobalSource;
import execution.model.source.LocalSource;
import execution.model.source.SimilarSource;
import exploration.model.DatabasesModel;
import exploration.model.clusterfunction.ClusterFunction;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.SourceEnum;
import exploration.model.source.similarity.SimilarityFunction;

public class SourceFactory {

	public static Source generateInstance(String string,Parametrizable parametrizable, DatabasesModel dm, int maxOrder) {
		
		switch (SourceEnum.valueOf(string)) {
		case LOCAL:
			return new LocalSource(dm,maxOrder);
		case SIMILAR:
			return new SimilarSource(dm,SimilarityFunction.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.DATABASE_SIMILARITY).getString()),maxOrder);
		case CLUSTERED:
			return new ClusteredSource(dm, ClusterFunction.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION).getString(),parametrizable.loadParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION_PARAMETERS)),maxOrder);
		case GLOBAL:
			return new GlobalSource(dm,maxOrder);
		default:
			return null;
		}
		
	}
	
}
