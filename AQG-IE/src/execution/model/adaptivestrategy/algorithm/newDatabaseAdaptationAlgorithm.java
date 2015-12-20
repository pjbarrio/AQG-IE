package execution.model.adaptivestrategy.algorithm;

import execution.model.Generation;
import execution.model.Source;
import execution.model.adaptivestrategy.databaseSelection.DatabaseSelection;
import execution.model.algorithmSelection.AlgorithmSelection;
import execution.model.documentCollector.DocumentCollector;
import execution.model.policy.ExecutionPolicy;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Database;
import exploration.model.Evaluation;

public class newDatabaseAdaptationAlgorithm extends AdaptationAlgorithm {



	private Database database;

	public newDatabaseAdaptationAlgorithm(Database database) {
		this.database = database;
	}

	@Override
	public boolean generatesNewEvaluation() {
		return true;
	}

	@Override
	public Evaluation update(Evaluation evaluation,
			StatisticsCollector statistics, DocumentCollector documentsCollector, Source source,
			Generation generation, ExecutionPolicy executionPolicy,
			DatabaseSelection databaseSelection, AlgorithmSelection algorithmSelection) {
		// TODO Auto-generated method stub
		return null;
	}

}
