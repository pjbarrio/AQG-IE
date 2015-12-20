package plot.generator.impl;

import plot.data.Series;

public class IndependentAddedDocumentsBySentQueries extends
		AddedDocumentsBySentQueries {

	@Override
	public String getTitle() {
		return "Independent " + super.getTitle();
	}
	
	@Override
	protected Series invokeAverage(Series ret) {
		return ret.independentAverage();
	}
	
}
