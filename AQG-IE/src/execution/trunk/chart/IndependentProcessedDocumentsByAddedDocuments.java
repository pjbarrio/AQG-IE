package execution.trunk.chart;

import plot.data.Series;
import plot.generator.impl.ProcessedDocumentsByAddedDocuments;

public class IndependentProcessedDocumentsByAddedDocuments extends
		ProcessedDocumentsByAddedDocuments {

	public IndependentProcessedDocumentsByAddedDocuments(String yNormalization,
			String xNormalization) {
		super(yNormalization, xNormalization);
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.independentAverage();
	}
	
}
