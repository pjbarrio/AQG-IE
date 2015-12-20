package execution.model.adaptivestrategy.condition.sampleevaluator;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;


public class PowerLawChecker extends ProbabilisticDistributionChecker {

	private double alpha;
	private double beta;
	private double epsilon;

	public PowerLawChecker(double alpha, double beta, double epsilon) {
		
		
		this.alpha = alpha;
		this.beta = beta;
		this.epsilon = epsilon;
		
	}

	@Override
	public double calculateAlignment(StatisticsCollector statistics,
			DocumentCollector documentsCollector) {
		// TODO Auto-generated method stub
		return 0;
	}



}
