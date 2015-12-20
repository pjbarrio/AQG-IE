package execution.model.adaptivestrategy.condition.performance.queryCondition;

import execution.model.adaptivestrategy.condition.performance.UselessQueryCondition;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Query;

public class NUselessDocuments extends UselessQueryCondition {

	public NUselessDocuments(double threshold) {
		super(threshold);
	}

	@Override
	public boolean isUseless(Query query, StatisticsCollector statistics) {
		return statistics.filter(query).getUselessDocuments() > threshold;
	}



}
