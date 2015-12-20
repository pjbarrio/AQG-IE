package execution.model.collector;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public abstract class StatisticsForSampleSelector {

	public abstract StatisticsCollector selectStatisticsCollector(
			Evaluation evaluation,
			StatisticsCollector statisticsCollector);

	public abstract DocumentCollector selectDocumentCollector(Evaluation evaluation, 
			DocumentCollector documentCollector);
}
