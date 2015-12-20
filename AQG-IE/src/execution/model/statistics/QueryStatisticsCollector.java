package execution.model.statistics;

import java.util.ArrayList;
import java.util.List;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Evaluation;
import exploration.model.Query;

public class QueryStatisticsCollector implements StatisticsCollector {

//	private ArrayList<Long> processedDocuments;
	private int usefulDocuments;
	private int uselessDocuments;
	private Query query;
	private ArrayList<Query> list;
	private int processed;
	private double processedDocuments;
	
	
	public QueryStatisticsCollector(Query q){
		processed = 0;
//		processedDocuments = new ArrayList<Long>();
		usefulDocuments = 0;
		uselessDocuments = 0;
		list = new ArrayList<Query>();
		this.query = q;
		list.add(query);
		
	}
	

	@Override
	public double getProcessedQueries() {
		
		return processed;
	
	}

	@Override
	public List<Query> getRunningQueries() {
		
		if (processed == 1){
			return new ArrayList<Query>();
		}
		
		return list;
			
	}

	@Override
	public double getNumberOfWords() {
		// TODO habilitate processed Documents.
		return 0;
	}

	@Override
	public double getUsefulDocuments() {
		return usefulDocuments;
	}

	@Override
	public double getProcessedDocuments() {
//		return processedDocuments.size();
		return processedDocuments;
	}

	@Override
	public double getUselessDocuments() {
		return usefulDocuments;
	}

	@Override
	public void notifyUpdated() {
		
//		processedDocuments.clear();
		processedDocuments = 0;
		usefulDocuments = 0;
		uselessDocuments = 0;

	}

	@Override
	public StatisticsCollector filter(Evaluation evaluation) {
		
		return new EmptyStatisticsCollector();

	}

	@Override
	public void setProcessedDocument(Query query, Evaluation eval,
			Document document, int usefulTuples) {
		
		if (query.equals(this.query)){

//			processedDocuments.add(document);
	
			processedDocuments++;
			
			if (usefulTuples>0){
				
				usefulDocuments++;
			
			}else{
				
				uselessDocuments++;
			
			}

		}
		
		
	}

	@Override
	public void setAsProcessed(Evaluation evaluation, Query query) {
		
		;
		
	}

	@Override
	public void setAsProcessed(Evaluation evaluation) {
		
		setAsProcessed(evaluation, query);

	}

	@Override
	public StatisticsCollector filter(Query query) {
		if (this.query.equals(query)){
			return this;
		}
		return new EmptyStatisticsCollector();
	}


	@Override
	public StatisticsCollector filter(Database database) {
		return new EmptyStatisticsCollector();
	}


	@Override
	public void setAsProcessed(Query query) {
		if (this.query.equals(query)){

			processed = 1;

		}

	}


	@Override
	public void clean() {
		
//		processedDocuments.clear();
//		processedDocuments = new ArrayList<String>(0);
		
		processedDocuments = 0;
		
	}
	
}
