package execution.model.adaptivestrategy.condition.performance;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;


public class UselessDocumentsCalculator extends PerformanceChecker {

	@Override
	public double calculatePerformance(StatisticsCollector statistics, DocumentCollector documentCollector) {
		return statistics.getUselessDocuments();
	}





}
