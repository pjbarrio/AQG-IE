package execution.model.finishingstrategy;

import execution.model.FinishingStrategy;
import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;
import exploration.model.Query;

public class HybridFinishingStrategy extends FinishingStrategy {

	private FinishingStrategy localFinishingStrategy;
	private FinishingStrategy globalFinishingStrategy;

	public HybridFinishingStrategy(
			int afterNDocuments, FinishingStrategy localFinishingStrategy,
			FinishingStrategy globalFinishingStrategy) {
		super(afterNDocuments);
		this.localFinishingStrategy = localFinishingStrategy;
		this.globalFinishingStrategy = globalFinishingStrategy;
	}

	@Override
	public boolean keepProcessingEvaluationProtected(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector) {
		return globalFinishingStrategy.keepProcessingEvaluation(evaluation,statistics.filter(evaluation),documentsCollector.filter(evaluation));
	}

	@Override
	public boolean keepProcessingDocumentsProtected(Evaluation evaluation, Query query, StatisticsCollector statistics, DocumentCollector documentsCollector) {
		return localFinishingStrategy.keepProcessingDocuments(evaluation,query,statistics.filter(evaluation).filter(query),documentsCollector.filter(evaluation).filter(query));
	}


}
