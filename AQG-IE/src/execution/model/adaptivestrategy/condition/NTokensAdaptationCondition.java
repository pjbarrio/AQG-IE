package execution.model.adaptivestrategy.condition;

import execution.model.adaptivestrategy.condition.tokenComparator.TokenComparator;
import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;

public class NTokensAdaptationCondition extends AdaptationCondition {

	private TokenComparator tokenComparator;
	private int N;

	public NTokensAdaptationCondition(TokenComparator tokenComparator, int N) {
		this.tokenComparator = tokenComparator;
		this.N = N;
	}

	@Override
	public boolean updateIsNeeded(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector) {
		return tokenComparator.calculateNumberOfTokens(statistics, documentsCollector) > N;
	}

	

}
