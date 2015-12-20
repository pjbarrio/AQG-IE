package techniques.algorithms;

import sample.generation.model.SampleBuilderParameters;
import utils.persistence.persistentWriter;
import weka.core.Instances;

public interface ExecutableAlgorithm {

	public void executeAlgorithm(Instances sample, persistentWriter pW, SampleBuilderParameters sp) throws Exception;

	
}
