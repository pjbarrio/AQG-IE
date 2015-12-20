package execution.model.adaptivestrategy.condition.sampleevaluator;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;


public class DistributionOfWordsSampleEvaluator extends SampleEvaluator {

	private ProbabilisticDistributionChecker probabilisticChecker;
	private double threshold;

	public DistributionOfWordsSampleEvaluator(
			ProbabilisticDistributionChecker probabilisticChecker, double threshold) {
		
		this.probabilisticChecker = probabilisticChecker;
		this.threshold = threshold;
		
	}

	@Override
	public boolean fitsGatheredSample(StatisticsCollector statistics,
			DocumentCollector documentsCollector) {
		return probabilisticChecker.calculateAlignment(statistics,documentsCollector) < threshold;
	}

	

}
