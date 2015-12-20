package execution.model.adaptivestrategy.condition.performance;

import execution.model.adaptivestrategy.condition.performance.queryCondition.LowPrecisionQuery;
import execution.model.adaptivestrategy.condition.performance.queryCondition.NUsefulDocuments;
import execution.model.adaptivestrategy.condition.performance.queryCondition.NUselessDocuments;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Query;
import exploration.model.enumerations.UselessQueryConditionEnum;

public abstract class UselessQueryCondition {

	protected double threshold;

	public UselessQueryCondition(double threshold) {
		this.threshold = threshold;
	}

	public static UselessQueryCondition generateInstance(String string, double threshold) {
		
		switch (UselessQueryConditionEnum.valueOf(string)) {
		
		case LOW_PRECISION:
			
			return new LowPrecisionQuery(threshold);

		case N_USELESS_DOCUMENTS:
			
			return new NUselessDocuments(threshold);
			
		case N_USEFUL_DOCUMENTS:	
		
			return new NUsefulDocuments(threshold);

		default:
			
			return null;
		
		}
		
	}

	public abstract boolean isUseless(Query query, StatisticsCollector statistics);

}
