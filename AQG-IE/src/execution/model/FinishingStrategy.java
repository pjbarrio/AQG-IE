package execution.model;

import execution.model.documentCollector.DocumentCollector;
import execution.model.statistics.StatisticsCollector;
import exploration.model.Evaluation;
import exploration.model.Query;


public abstract class FinishingStrategy {

	private int afterNDocuments;

	public FinishingStrategy(int afterNDocuments){
		this.afterNDocuments = afterNDocuments;
	}
	
	public final boolean keepProcessingEvaluation(Evaluation evaluation, StatisticsCollector statistics, DocumentCollector documentsCollector){
		if (statistics.getProcessedDocuments() < afterNDocuments){
			
			return true;
			
		}
		
		return keepProcessingEvaluationProtected(evaluation, statistics, documentsCollector);
	}

	protected abstract boolean keepProcessingEvaluationProtected(Evaluation evaluation,
			StatisticsCollector statistics, DocumentCollector documentsCollector);

	public final boolean keepProcessingDocuments(Evaluation evaluation, Query query, StatisticsCollector statistics, DocumentCollector documentsCollector){
		
		if (statistics.getProcessedDocuments() < afterNDocuments){
			
			return true;
			
		}
		
		return keepProcessingDocumentsProtected(evaluation, query, statistics, documentsCollector);
		
	}

	protected abstract boolean keepProcessingDocumentsProtected(Evaluation evaluation,
			Query query, StatisticsCollector statistics,
			DocumentCollector documentsCollector);
	
}
