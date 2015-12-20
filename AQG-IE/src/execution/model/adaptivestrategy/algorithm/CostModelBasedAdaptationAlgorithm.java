package execution.model.adaptivestrategy.algorithm;

import execution.model.Generation;
import execution.model.Source;
import execution.model.adaptivestrategy.databaseSelection.DatabaseSelection;
import execution.model.algorithmSelection.AlgorithmSelection;
import execution.model.documentCollector.DocumentCollector;
import execution.model.policy.ExecutionPolicy;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public class CostModelBasedAdaptationAlgorithm extends AdaptationAlgorithm {

	private AdaptationAlgorithm chosenAdaptationAlgorithm;

	@Override
	public boolean generatesNewEvaluation() {
		
		return chosenAdaptationAlgorithm.generatesNewEvaluation();
	
	}

	@Override
	public Evaluation update(Evaluation evaluation,
			StatisticsCollector statistics, DocumentCollector documentsCollector, Source source,
			Generation generation, ExecutionPolicy executionPolicy,
			DatabaseSelection databaseSelection, AlgorithmSelection algorithmSelection) {
		
		chosenAdaptationAlgorithm = chooseAdaptationAlgorithm(evaluation,statistics,documentsCollector,source,generation,executionPolicy,databaseSelection);
		
		return chosenAdaptationAlgorithm.update(evaluation, statistics,documentsCollector, source, generation, executionPolicy, databaseSelection, algorithmSelection);
		
	}

	private AdaptationAlgorithm chooseAdaptationAlgorithm(Evaluation evaluation,
			StatisticsCollector statistics, DocumentCollector documentsCollector, Source source,
			Generation generation, ExecutionPolicy executionPolicy,
			DatabaseSelection databaseSelection) {
		// TODO Auto-generated method stub
		return null;
	}

}
