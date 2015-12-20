package execution.model.adaptivestrategy.condition;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public class NoneAdaptationCondition extends AdaptationCondition {

	@Override
	public boolean updateIsNeeded(Evaluation evaluation,
			StatisticsCollector statistics, DocumentCollector documentsCollector) {
		return false;
	}

}
