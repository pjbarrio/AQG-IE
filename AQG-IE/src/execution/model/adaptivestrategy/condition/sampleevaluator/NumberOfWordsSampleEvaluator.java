package execution.model.adaptivestrategy.condition.sampleevaluator;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;


public class NumberOfWordsSampleEvaluator extends SampleEvaluator {

	private int number;

	public NumberOfWordsSampleEvaluator(int number) {
		this.number = number;
	}

	@Override
	public boolean fitsGatheredSample(StatisticsCollector statistics,
			DocumentCollector documentsCollector) {
		return statistics.getNumberOfWords() > number;
	}



}
