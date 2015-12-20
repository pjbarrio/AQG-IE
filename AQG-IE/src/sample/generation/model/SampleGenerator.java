package sample.generation.model;

import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Sample;

public abstract class SampleGenerator {

	public abstract boolean generateSample(Sample sample, Database database, persistentWriter pW, SampleConfiguration sampleConfiguration, int version_seed_pos,int version_seed_neg);

}
