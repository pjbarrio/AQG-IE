package execution.model.adaptivestrategy.condition;

import execution.model.adaptivestrategy.condition.sampleevaluator.SampleEvaluator;
import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public class GoodSampleAdaptationCondition extends AdaptationCondition {

	private SampleEvaluator sampleEvaluator;

	public GoodSampleAdaptationCondition(SampleEvaluator sampleEvaluator) {
		this.sampleEvaluator = sampleEvaluator;
	}

	@Override
	public boolean updateIsNeeded(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector) {
		return sampleEvaluator.fitsGatheredSample(statistics, documentsCollector);
	}





}
