package execution.model.adaptivestrategy.condition.performance;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;

public abstract class PerformanceChecker {

	public abstract double calculatePerformance(StatisticsCollector statistics,DocumentCollector documentCollector);

}
