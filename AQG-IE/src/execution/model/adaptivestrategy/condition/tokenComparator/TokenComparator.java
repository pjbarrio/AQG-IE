package execution.model.adaptivestrategy.condition.tokenComparator;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.enumerations.TokenComparatorEnum;

public abstract class TokenComparator {

	public static TokenComparator getInstance(String string) {
		
		switch (TokenComparatorEnum.valueOf(string)) {
		
		case DOCUMENT:
			
			return new DocumentComparator();

		
		case QUERY:
			
			return new QueryComparator();


		default:
			
			return null;
			
		}
	}

	public abstract double calculateNumberOfTokens(StatisticsCollector statistics, DocumentCollector documentsCollector);

}
