package execution.model.statistics;

import java.util.ArrayList;
import java.util.List;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Evaluation;
import exploration.model.Query;

public class DatabaseStatisticsCollector implements StatisticsCollector {

	private Database database;
	private int processed;
//	private List<Long> processedDocuments;
	private int useful;
	private int useless;

	public DatabaseStatisticsCollector (Database database){
		this.database = database;
		processed = 0;
		useful = 0;
		useless = 0;
//		processedDocuments = new ArrayList<Long>();
	}
	
	@Override
	public void setProcessedDocument(Query query, Evaluation eval,
			Document document, int usefulTuples) {
		
		if (database.equals(eval.getEvaluableDatabase())){
			processed++;
//			processedDocuments.add(document);
			if (usefulTuples>0)
				useful++;
			else
				useless++;
		}
		
	}

	@Override
	public double getProcessedQueries() {
		return 0;
	}

	@Override
	public List<Query> getRunningQueries() {
		return new ArrayList<Query>();
	}

	@Override
	public double getNumberOfWords() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getUsefulDocuments() {
		return useful;
	}

	@Override
	public double getProcessedDocuments() {
		return processed;
	}

	@Override
	public double getUselessDocuments() {
		return useless;
	}

	@Override
	public void notifyUpdated() {
//		processedDocuments.clear();
		processed=0;
		useful=0;
		useless=0;
	}

	@Override
	public StatisticsCollector filter(Database database) {
		if (this.database.equals(database)){
			return this;
		}
		return new EmptyStatisticsCollector();
	}

	@Override
	public StatisticsCollector filter(Evaluation evaluation) {
		return new EmptyStatisticsCollector();
	}

	@Override
	public StatisticsCollector filter(Query query) {
		return new EmptyStatisticsCollector();
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
	public void setAsProcessed(Query query) {
		;
	}

	@Override
	public void clean() {
		
//		processedDocuments.clear();
//		processedDocuments = new ArrayList<String>();

		processed = 0;
		useful=0;
		useless=0;
		
	}

}
