package execution.model.adaptivestrategy.condition;

import execution.model.adaptivestrategy.condition.performance.PerformanceChecker;
import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public class UnderPerformanceAdaptationCondition extends AdaptationCondition {

	private PerformanceChecker performanceChecker;
	private Double threshold;

	public UnderPerformanceAdaptationCondition(PerformanceChecker performanceChecker, Double threshold) {
		
		this.performanceChecker = performanceChecker;
		this.threshold = threshold;
		
	}

	@Override
	public boolean updateIsNeeded(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentCollector) {
		return performanceChecker.calculatePerformance(statistics.filter(evaluation),documentCollector.filter(evaluation)) < threshold;
	}




}
