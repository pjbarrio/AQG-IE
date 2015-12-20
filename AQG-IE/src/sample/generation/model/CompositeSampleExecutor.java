package sample.generation.model;

import java.util.Collections;
import java.util.List;

import sample.generation.model.cardinality.CardinalityFunction;
import sample.generation.model.impl.DummySampleConfiguration;
import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.clusterfunction.ClusterFunction;
import exploration.model.database.GroupDatabase;
import extraction.relationExtraction.RelationExtractionSystem;

public abstract class CompositeSampleExecutor extends SampleExecutor {

	private ClusterFunction clusterFunction;
	private CardinalityFunction cardinalityFunction;
	private SampleGenerator sampleGenerator;
	private boolean aleatorize;

	public CompositeSampleExecutor(ClusterFunction clusterFunction, CardinalityFunction cardinalityFunction, SampleGenerator sampleGenerator, boolean aleatorize){
		this.clusterFunction = clusterFunction;
		this.cardinalityFunction = cardinalityFunction;
		this.sampleGenerator = sampleGenerator;
		this.aleatorize = aleatorize;
	}
	
	@Override
	public boolean generateSample(persistentWriter pW, Sample sample, SampleConfiguration sampleConfiguration, int version_seed_pos,int version_seed_neg) {
		
		List<Database> databases = pW.getDatabasesInGroup(sample.getDatabase().getId(),clusterFunction,sample.getVersion(),sample.getWorkload());
		
		cardinalityFunction.calculateRequests(databases,sample.getSampleConfiguration());
		
		int usefulDocuments = 0;
		
		int uselessDocuments = 0;
		
		if (aleatorize)
			Collections.shuffle(databases);
		
//		boolean done = true;
		
		for (int i = 0; i < databases.size() && sampleConfiguration.keepProcessing(sample); i++) {
			
			System.err.println(i + " out of : " + databases.size());
			
			usefulDocuments += cardinalityFunction.getUsefulDocuments(databases.get(i));
			
			uselessDocuments += cardinalityFunction.getUselessDocuments(databases.get(i));
			
			SampleConfiguration sc = sampleConfiguration.createReducedCopy(usefulDocuments,uselessDocuments);
			
			/*done = */sampleGenerator.generateSample(sample, databases.get(i), pW, sc, version_seed_pos,version_seed_neg);
			
		}
		
		if (sampleConfiguration.ready(sample))
			return true;
		
		return false;
		
	}

	@Override
	public abstract boolean samples(Database database);

}
