package execution.model.adaptivestrategy.condition;

import execution.model.adaptivestrategy.condition.sampleevaluator.SampleEvaluator;
import execution.model.adaptivestrategy.condition.tokenComparator.TokenComparator;
import execution.model.documentCollector.DocumentCollector;
import execution.model.factory.PerformanceCheckerFactory;
import execution.model.parameters.Parametrizable;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;
import exploration.model.enumerations.AdaptationConditionEnum;
import exploration.model.enumerations.ExecutionAlternativeEnum;

public abstract class AdaptationCondition {

	public static AdaptationCondition generateInstance(String string,
			Parametrizable parametrizable) {
		
		switch (AdaptationConditionEnum.valueOf(string)) {
		case UNDERPERFORMANCE:
			
			return new UnderPerformanceAdaptationCondition(PerformanceCheckerFactory.getInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.PERFORMANCE_CHECKER).getString(),parametrizable.loadParameter(ExecutionAlternativeEnum.PERFORMANCE_CHECKER_PARAMETERS)),Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.PERFORMANCE_THRESHOLD).getString()));
			
		case GOODSAMPLE:
			
			return new GoodSampleAdaptationCondition(SampleEvaluator.getInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.SAMPLE_EVALUATOR).getString(),parametrizable.loadParameter(ExecutionAlternativeEnum.SAMPLE_EVALUATOR_PARAMETERS)));
		
		case NTOKENS:
			
			return new NTokensAdaptationCondition(TokenComparator.getInstance(parametrizable.loadParameter(ExecutionAlternativeEnum.TOKEN_COMPARATOR).getString()),Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.NUMBER_OF_TOKENS).getString()));
			
		default:
			
			return new NoneAdaptationCondition();
		}
		
	}
	/*
	 * Already Filtered!
	 */
	public abstract boolean updateIsNeeded(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector);

}
