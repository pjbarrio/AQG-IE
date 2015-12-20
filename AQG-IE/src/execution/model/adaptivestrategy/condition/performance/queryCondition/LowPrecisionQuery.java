package execution.model.adaptivestrategy.condition.performance.queryCondition;

import execution.model.adaptivestrategy.condition.performance.UselessQueryCondition;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Query;

public class LowPrecisionQuery extends UselessQueryCondition {

	public LowPrecisionQuery(double threshold) {
		super(threshold);
	}

	@Override
	public boolean isUseless(Query query, StatisticsCollector statistics) {
		return statistics.filter(query).getUsefulDocuments()/statistics.filter(query).getProcessedDocuments() < threshold;
	}

}
