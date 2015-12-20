package execution.model.adaptivestrategy.condition.sampleevaluator;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;


public class WeibullChecker extends ProbabilisticDistributionChecker {

	private double nu;
	private double beta;

	public WeibullChecker(double nu, double beta) {
		
		this.nu = nu;
		this.beta = beta;
	
	}

	@Override
	public double calculateAlignment(StatisticsCollector statistics,
			DocumentCollector documentsCollector) {
		// TODO Auto-generated method stub
		return 0;
	}



}
