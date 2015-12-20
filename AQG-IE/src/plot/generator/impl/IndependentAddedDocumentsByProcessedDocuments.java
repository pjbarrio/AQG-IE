package plot.generator.impl;

import plot.data.Series;

public class IndependentAddedDocumentsByProcessedDocuments extends
		AddedDocumentsByProcessedDocuments {

	public IndependentAddedDocumentsByProcessedDocuments(
			String yNormalization, String xNormalization, double value) {
		super(yNormalization,xNormalization, value);
	}

	@Override
	public String getTitle() {
		return "Independent " + super.getTitle();
	}
	
	@Override
	protected Series invokeAverage(Series ret) {
		return ret.independentAverage();
	}
}
