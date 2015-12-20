package execution.model.statistics;

import java.util.ArrayList;
import java.util.List;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Evaluation;
import exploration.model.Query;

public class EmptyStatisticsCollector implements StatisticsCollector {

	@Override
	public void setProcessedDocument(Query query, Evaluation eval,
			Document document, int usefulTuples) {
		;
	}

	@Override
	public double getProcessedQueries() {
		return 0;
	}

	@Override
	public List<Query> getRunningQueries() {
		return new ArrayList<Query>(0);
	}

	@Override
	public double getNumberOfWords() {
		return 0;
	}

	@Override
	public double getUsefulDocuments() {
		return 0;
	}

	@Override
	public double getProcessedDocuments() {
		return 0;
	}

	@Override
	public double getUselessDocuments() {
		return 0;
	}

	@Override
	public void notifyUpdated() {
		;
	}

	@Override
	public StatisticsCollector filter(Evaluation evaluation) {
		return null;
	}

	@Override
	public void setAsProcessed(Evaluation eval, Query query) {
		;
	}

	@Override
	public void setAsProcessed(Evaluation eval) {
		;
	}

	@Override
	public StatisticsCollector filter(Database database) {
		return null;
	}

	@Override
	public StatisticsCollector filter(Query query) {
		return null;
	}

	@Override
	public void setAsProcessed(Query query) {
		;
	}

	@Override
	public void clean() {
		;
		
	}

}
