package execution.model.adaptivestrategy.condition.performance;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Query;

public class UselessQueriesCalculator extends PerformanceChecker {

	private UselessQueryCondition uselessQueryCondition;

	public UselessQueriesCalculator(UselessQueryCondition uselessQueryCondition) {
		this.uselessQueryCondition = uselessQueryCondition;
	}

	@Override
	public double calculatePerformance(StatisticsCollector statistics, DocumentCollector documentCollector) {
		
		double ret = 0;
		
		for (Query query : statistics.getRunningQueries()) {
			
			if (uselessQueryCondition.isUseless(query,statistics.filter(query))){
				
				ret++;
			
			}
			
		}
		
		return ret;
	
	}


}
