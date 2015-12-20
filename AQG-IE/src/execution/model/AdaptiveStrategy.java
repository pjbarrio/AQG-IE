package execution.model;

import execution.model.adaptivestrategy.databaseSelection.DatabaseSelection;
import execution.model.algorithmSelection.AlgorithmSelection;
import execution.model.documentCollector.DocumentCollector;
import execution.model.policy.ExecutionPolicy;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public abstract class AdaptiveStrategy {

	//HAS THE WHEN, WHAT and HOW
	
	public abstract boolean updateIsNeeded(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector);
	
	public abstract Evaluation update(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector, Source source, 
			Generation generation, ExecutionPolicy executionPolicy, DatabaseSelection databaseSelection, AlgorithmSelection algorithmSelection);

	public abstract boolean generatedNewEvaluation();

}
