package plot.generator.impl;

import plot.data.Series;
import utils.persistence.persistentWriter;
import exploration.model.enumerations.EntropyEnum;

public class IndependentUniqueTuplesBySampledDocuments extends
		UniqueTuplesBySampledDocuments {

	public IndependentUniqueTuplesBySampledDocuments(persistentWriter pW,
			EntropyEnum which, String yNormalization, String xNormalization) {
		super(pW, which, yNormalization, xNormalization);
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.independentAverage();
	}
	
}
