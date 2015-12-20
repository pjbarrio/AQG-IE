package sample.generation.model;

import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Sample;

public abstract class SampleExecutor {

	public abstract boolean generateSample(persistentWriter pW, Sample sample, SampleConfiguration sampleConfiguration, int version_seed_pos, int  version_seed_neg);

	public abstract boolean samples(Database database);
	
}
