package execution.model.adaptivestrategy.algorithm;

import utils.persistence.persistentWriter;
import execution.model.Generation;
import execution.model.Source;
import execution.model.adaptivestrategy.databaseSelection.DatabaseSelection;
import execution.model.algorithmSelection.AlgorithmSelection;
import execution.model.documentCollector.DocumentCollector;
import execution.model.factory.GenerationFactory;
import execution.model.parameters.Parametrizable;
import execution.model.policy.ExecutionPolicy;
import execution.model.statistics.StatisticsCollector;
import exploration.model.DatabasesModel;
import exploration.model.Evaluation;
import exploration.model.enumerations.AdaptationAlgorithmEnum;
import exploration.model.enumerations.ExecutionAlternativeEnum;

public abstract class AdaptationAlgorithm {

	public static AdaptationAlgorithm generateInstance(String string,
			Parametrizable parametrizable,persistentWriter pW,DatabasesModel dm) {
		
		switch (AdaptationAlgorithmEnum.valueOf(string)) {
		
		case RETRAIN:
			
			return new RetrainAdaptationAlgorithm();
		
		case NEWGENERATION:
			
			return new newGenerationAdaptationAlgorithm(parametrizable.loadParameter(ExecutionAlternativeEnum.NEXT_GENERATION_ALGORITHM).toString(),
					GenerationFactory.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.NEXT_GENERATION).toString(), 
							parametrizable.loadParameter(ExecutionAlternativeEnum.NEXT_GENERATION_PARAMETERS), dm));
		
		case NEWALGORITHM:
			
			return new newAlgorithmAdaptationAlgorithm(parametrizable.loadParameter(ExecutionAlternativeEnum.NEW_ALGORITHM).toString(),parametrizable.loadParameter(ExecutionAlternativeEnum.NEW_ALGORITHM_PARAMETERS));
			
		case RESCHEDULE:
			
			return new ReScheduleAdaptationAlgorithm();
			
		case NEWDATABASE:
			
			return new newDatabaseAdaptationAlgorithm(pW.getDatabaseByName(parametrizable.loadParameter(ExecutionAlternativeEnum.NEW_DATABASE).toString()));

		case ANY:
			
			return new CostModelBasedAdaptationAlgorithm();
			
		default:
			
			return new NoneAdaptationAlgorithm();
		}
		
	}

	public abstract Evaluation update(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector, 
			 Source source, Generation generation, ExecutionPolicy executionPolicy, DatabaseSelection databaseSelection, AlgorithmSelection algorithmSelection);

	public abstract boolean generatesNewEvaluation();

}
