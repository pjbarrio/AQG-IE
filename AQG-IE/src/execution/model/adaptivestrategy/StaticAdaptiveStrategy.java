package execution.model.adaptivestrategy;

import execution.model.AdaptiveStrategy;
import execution.model.Generation;
import execution.model.Source;
import execution.model.adaptivestrategy.databaseSelection.DatabaseSelection;
import execution.model.algorithmSelection.AlgorithmSelection;
import execution.model.documentCollector.DocumentCollector;
import execution.model.policy.ExecutionPolicy;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public class StaticAdaptiveStrategy extends AdaptiveStrategy {

	@Override
	public boolean generatedNewEvaluation() {
		return false;
	}



	@Override
	public Evaluation update(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector, Source source, 
			Generation generation, ExecutionPolicy executionPolicy, DatabaseSelection databaseSelection, AlgorithmSelection algorithmSelection) {
		return null;
	}



	@Override
	public boolean updateIsNeeded(Evaluation evaluation,
			StatisticsCollector statistics, DocumentCollector documentsCollector) {
		return false;
	}

	

}
