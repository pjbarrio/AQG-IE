package sample.generation.model.impl;

import exploration.model.Database;
import exploration.model.Sample;
import sample.generation.model.SampleConfiguration;
import sample.generation.model.SampleExecutor;
import sample.generation.model.SampleGenerator;
import utils.persistence.persistentWriter;

public class SimpleSampleExecutor extends SampleExecutor {

	private SampleGenerator sampleGenerator;

	public SimpleSampleExecutor(SampleGenerator sampleGenerator) {
		this.sampleGenerator = sampleGenerator;
	}

	@Override
	public boolean generateSample(persistentWriter pW,Sample sample,
			SampleConfiguration sampleConfiguration, int version_seed_pos,int version_seed_neg) {
		return sampleGenerator.generateSample(sample, sample.getDatabase(), pW, sampleConfiguration, version_seed_pos,version_seed_neg);
	}

	@Override
	public boolean samples(Database database) {
		
		return (!database.isGlobal() && !database.isCluster());
		
	}

}
