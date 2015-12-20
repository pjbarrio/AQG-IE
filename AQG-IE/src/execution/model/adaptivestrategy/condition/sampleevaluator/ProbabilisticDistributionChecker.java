package execution.model.adaptivestrategy.condition.sampleevaluator;

import execution.model.documentCollector.DocumentCollector;
import execution.model.parameters.Parametrizable;
import execution.model.statistics.StatisticsCollector;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.ProbabilisticDistributionCheckerEnum;

public abstract class ProbabilisticDistributionChecker {

	public static ProbabilisticDistributionChecker generateInstance(String string,
			Parametrizable parametrizable) {
		
		switch (ProbabilisticDistributionCheckerEnum.valueOf(string)) {
		
		case POWER_LAW:
			
			return new PowerLawChecker(Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.POWER_LAW_ALPHA).getString()),
					Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.POWER_LAW_BETA).getString()),
					Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.POWER_LAW_EPSILON).getString()));

		case WEIBULL:
			
			return new WeibullChecker(Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.WEIBULL_NU).getString()),
					Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.WEIBULL_BETA).getString()));
			
		default:
			
			return null;
		}
	
	}

	public abstract double calculateAlignment(StatisticsCollector statistics, DocumentCollector documetsCollector);

}
