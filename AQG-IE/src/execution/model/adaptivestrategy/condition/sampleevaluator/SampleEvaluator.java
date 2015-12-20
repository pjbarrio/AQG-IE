package execution.model.adaptivestrategy.condition.sampleevaluator;

import execution.model.documentCollector.DocumentCollector;
import execution.model.parameters.Parametrizable;
import execution.model.statistics.StatisticsCollector;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.SampleEvaluatorEnum;

public abstract class SampleEvaluator {

	public static SampleEvaluator getInstance(String string,
			Parametrizable parametrizable) {
		
		switch (SampleEvaluatorEnum.valueOf(string)) {
		
		case NUMBER_OF_DOCUMENTS:
			
			return new NumberOfDocumentsSampleEvaluator(Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.NUMBER_OF_DOCUMENTS).getString()));
			
		case NUMBER_OF_WORDS:
			
			return new NumberOfWordsSampleEvaluator(Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.NUMBER_OF_WORDS).getString()));
			
		case DISTRIBUTION_OF_WORDS:
			
			return new DistributionOfWordsSampleEvaluator(ProbabilisticDistributionChecker.generateInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.PROBABILISTIC_DISTRIBUTION).getString(),parametrizable.loadParameter(ExecutionAlternativeEnum.PROBABILISTIC_DISTRIBUTION_PARAMETERS)),
					Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.PROBABILISTIC_DISTRIBUTION_SIMILARITY_THRESHOLD).getString()));
		
		default:
			
			return null;
		
		}
		
	}

	public abstract boolean fitsGatheredSample(StatisticsCollector statistics, DocumentCollector documentsCollector);

}
