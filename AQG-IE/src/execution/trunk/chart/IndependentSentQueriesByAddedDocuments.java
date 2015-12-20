package execution.trunk.chart;

import plot.data.Series;
import plot.generator.impl.ProcessedDocumentsByAddedDocuments;
import plot.generator.impl.SentQueriesByAddedDocuments;

public class IndependentSentQueriesByAddedDocuments extends
		SentQueriesByAddedDocuments {

	@Override
	protected Series invokeAverage(Series ret) {
	return ret.independentAverage();
	}
	
}
