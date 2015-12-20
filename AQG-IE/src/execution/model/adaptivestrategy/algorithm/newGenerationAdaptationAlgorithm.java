package execution.model.adaptivestrategy.algorithm;

import execution.model.Generation;
import execution.model.Source;
import execution.model.adaptivestrategy.databaseSelection.DatabaseSelection;
import execution.model.algorithmSelection.AlgorithmSelection;
import execution.model.documentCollector.DocumentCollector;
import execution.model.policy.ExecutionPolicy;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public class newGenerationAdaptationAlgorithm extends AdaptationAlgorithm {

	private String algorithm;
	private Generation generation;

	public newGenerationAdaptationAlgorithm(String algorithm,
			Generation generation) {
		this.algorithm = algorithm;
		this.generation = generation;
	}

	@Override
	public boolean generatesNewEvaluation() {
		return true;
	}

	@Override
	public Evaluation update(Evaluation evaluation,
			StatisticsCollector statistics,
			DocumentCollector documentsCollector, Source source,
			Generation generation, ExecutionPolicy executionPolicy,
			DatabaseSelection databaseSelection, AlgorithmSelection algorithmSelection) {
		// TODO Auto-generated method stub
		return null;
	}





}
