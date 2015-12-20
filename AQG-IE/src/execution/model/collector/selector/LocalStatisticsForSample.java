package execution.model.collector.selector;

import execution.model.collector.StatisticsForSampleSelector;
import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public class LocalStatisticsForSample extends StatisticsForSampleSelector {

	@Override
	public StatisticsCollector selectStatisticsCollector(Evaluation evaluation,
			StatisticsCollector statisticsCollector) {
		return statisticsCollector.filter(evaluation);
	}

	@Override
	public DocumentCollector selectDocumentCollector(Evaluation evaluation,
			DocumentCollector documentCollector) {
		return documentCollector.filter(evaluation);
	}


	

}
