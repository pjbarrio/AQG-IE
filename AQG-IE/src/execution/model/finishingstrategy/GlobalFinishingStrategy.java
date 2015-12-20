package execution.model.finishingstrategy;

import execution.model.FinishingStrategy;
import execution.model.adaptivestrategy.condition.performance.PerformanceChecker;
import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;
import exploration.model.Query;

public class GlobalFinishingStrategy extends FinishingStrategy {

	private PerformanceChecker performanceChecker;
	private double threshold;

	public GlobalFinishingStrategy(int afterNDocuments, PerformanceChecker performanceChecker, double threshold) {
		super(afterNDocuments);
		this.performanceChecker = performanceChecker;
		this.threshold = threshold;
		
	}

	@Override
	public boolean keepProcessingEvaluationProtected(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector) {
		return performanceChecker.calculatePerformance(statistics.filter(evaluation),documentsCollector.filter(evaluation)) > threshold;
	}

	@Override
	public boolean keepProcessingDocumentsProtected(Evaluation evaluation, Query query, StatisticsCollector statistics, DocumentCollector documentsCollector) {
		return true;

	}





}
