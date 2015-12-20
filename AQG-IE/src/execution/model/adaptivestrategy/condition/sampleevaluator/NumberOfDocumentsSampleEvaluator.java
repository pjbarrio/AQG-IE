package execution.model.adaptivestrategy.condition.sampleevaluator;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;


public class NumberOfDocumentsSampleEvaluator extends SampleEvaluator {

	private int number;

	public NumberOfDocumentsSampleEvaluator(int number) {
		this.number = number;
	}

	@Override
	public boolean fitsGatheredSample(StatisticsCollector statistics,
			DocumentCollector documentsCollector) {
		return statistics.getProcessedDocuments() >= number;
	}


}
