package execution.model.adaptivestrategy.condition.tokenComparator;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;


public class DocumentComparator extends TokenComparator {

	@Override
	public double calculateNumberOfTokens(StatisticsCollector statistics, DocumentCollector documentsCollector) {
		
		return statistics.getProcessedDocuments();
		
	}



}
