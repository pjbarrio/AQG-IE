package execution.model.finishingstrategy;

import execution.model.FinishingStrategy;
import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;
import exploration.model.Query;

public class NoneFinishingStrategy extends FinishingStrategy {

	public NoneFinishingStrategy(int afterNDocuments) {
		super(afterNDocuments);
	}

	@Override
	public boolean keepProcessingDocumentsProtected(Evaluation evaluation, Query query,
			StatisticsCollector statistics, DocumentCollector documentsCollector) {
		return true;
	}

	@Override
	public boolean keepProcessingEvaluationProtected(Evaluation evaluation,
			StatisticsCollector statistics, DocumentCollector documentsCollector) {
		return true;
	}

}
