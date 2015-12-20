package sample.generation.model;

import java.util.List;

import sample.generation.model.cardinality.CardinalityFunction;
import sample.generation.model.impl.CachedSampleGenerator;
import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.clusterfunction.ClusterFunction;
import extraction.relationExtraction.RelationExtractionSystem;

public abstract class CachedCompositeSampleExecutor extends CompositeSampleExecutor{
	
	public CachedCompositeSampleExecutor(ClusterFunction clusterFunction, CardinalityFunction cardinalityFunction,int idSampleConfiguration, RelationExtractionSystem res, persistentWriter pW, boolean aleatorize) {

		super(clusterFunction,cardinalityFunction,new CachedSampleGenerator(pW.getSampleConfigution(idSampleConfiguration),res),aleatorize);

	}

	@Override
	public abstract boolean samples(Database database);

}
