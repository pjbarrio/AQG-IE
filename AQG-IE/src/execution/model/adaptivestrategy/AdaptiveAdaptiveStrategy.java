package execution.model.adaptivestrategy;

import execution.model.AdaptiveStrategy;
import execution.model.Generation;
import execution.model.Source;
import execution.model.adaptivestrategy.algorithm.AdaptationAlgorithm;
import execution.model.adaptivestrategy.condition.AdaptationCondition;
import execution.model.adaptivestrategy.databaseSelection.DatabaseSelection;
import execution.model.algorithmSelection.AlgorithmSelection;
import execution.model.documentCollector.DocumentCollector;
import execution.model.policy.ExecutionPolicy;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public class AdaptiveAdaptiveStrategy extends AdaptiveStrategy {

	private AdaptationCondition adaptationCondition;
	private AdaptationAlgorithm adaptationAlgorithm;
	private int afterNDocuments;
	
	public AdaptiveAdaptiveStrategy(int afterNDocuments, AdaptationCondition adaptationCondition, AdaptationAlgorithm adaptationAlgorithm) {
		
		this.afterNDocuments = afterNDocuments;
		
		this.adaptationCondition = adaptationCondition;
		this.adaptationAlgorithm = adaptationAlgorithm;
	
	}

	@Override
	public boolean generatedNewEvaluation() {
		
		return adaptationAlgorithm.generatesNewEvaluation();
		
	}

	@Override
	public Evaluation update(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector, Source source, 
			Generation generation, ExecutionPolicy executionPolicy, DatabaseSelection databaseSelection, AlgorithmSelection algorithmSelection) {
		
		return adaptationAlgorithm.update(evaluation,statistics,documentsCollector,source,generation,executionPolicy, databaseSelection, algorithmSelection);
		
	}

	@Override
	public boolean updateIsNeeded(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector) {
		
		if (statistics.getProcessedDocuments() < afterNDocuments){
			
			return false;
		
		}
		
		return adaptationCondition.updateIsNeeded(evaluation,statistics,documentsCollector);
	}


}
