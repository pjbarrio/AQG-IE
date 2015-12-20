package execution.model.statistics;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Evaluation;
import exploration.model.Query;

public class ExecutionStatisticsCollector implements StatisticsCollector {

	private Hashtable<Integer, DatabaseStatisticsCollector> databasesTable;
	private Hashtable<String, EvaluationStatisticsCollector> evaluationTable;
	private Hashtable<String, QueryStatisticsCollector> queriesTable;

	public ExecutionStatisticsCollector(){
		
		databasesTable = new Hashtable<Integer, DatabaseStatisticsCollector>();
		evaluationTable = new Hashtable<String, EvaluationStatisticsCollector>();
		queriesTable = new Hashtable<String, QueryStatisticsCollector>();
	
	}
	
	@Override
	public double getProcessedQueries() {
		
		double ret = 0;
		
		for (Enumeration<EvaluationStatisticsCollector> e = evaluationTable.elements();e.hasMoreElements();){
			
			ret += e.nextElement().getProcessedQueries();
			
		}
		
		return ret;
	}

	@Override
	public List<Query> getRunningQueries() {
		
		ArrayList<Query> ret = new ArrayList<Query>();
		
		for (Enumeration<EvaluationStatisticsCollector> e = evaluationTable.elements();e.hasMoreElements();){
			
			ret.addAll(e.nextElement().getRunningQueries());
			
		}
		
		return ret;
		
	}

	@Override
	public double getNumberOfWords() {
		
		double ret = 0;
		
		for (Enumeration<EvaluationStatisticsCollector> e = evaluationTable.elements();e.hasMoreElements();){
			
			ret += e.nextElement().getNumberOfWords();
			
		}
		
		return ret;
		
	}

	@Override
	public double getUsefulDocuments() {
		double ret = 0;
		
		for (Enumeration<EvaluationStatisticsCollector> e = evaluationTable.elements();e.hasMoreElements();){
			
			ret += e.nextElement().getUsefulDocuments();
			
		}
		
		return ret;
	}

	@Override
	public double getProcessedDocuments() {

		double ret = 0;
		
		for (Enumeration<EvaluationStatisticsCollector> e = evaluationTable.elements();e.hasMoreElements();){
			
			ret += e.nextElement().getProcessedDocuments();
			
		}
		
		return ret;
	}

	@Override
	public double getUselessDocuments() {
		double ret = 0;
		
		for (Enumeration<EvaluationStatisticsCollector> e = evaluationTable.elements();e.hasMoreElements();){
			
			ret += e.nextElement().getUselessDocuments();
			
		}
		
		return ret;
	}

	@Override
	public void notifyUpdated() {
		for (Enumeration<EvaluationStatisticsCollector> e = evaluationTable.elements();e.hasMoreElements();){
			
			e.nextElement().notifyUpdated();
			
		}
		
		for (Enumeration<DatabaseStatisticsCollector> e = databasesTable.elements();e.hasMoreElements();){
			
			e.nextElement().notifyUpdated();
			
		}
		
	}

	@Override
	public StatisticsCollector filter(Evaluation evaluation) {
		return getEvaluationStatistics(evaluation);
	}

	@Override
	public void setProcessedDocument(Query query, Evaluation eval,
			Document document, int usefulTuples) {
		
		saveDocumentInDatabase(query,eval,document,usefulTuples,eval.getEvaluableDatabase());
		
		saveDocumentInEvaluation(query,eval,document,usefulTuples);
		
		saveDocumentInQuery(query,eval,document,usefulTuples);
		
	}

	private void saveDocumentInQuery(Query query, Evaluation eval,
			Document document, int usefulTuples) {
		
		getQueryTable(query).setProcessedDocument(query,null,document,usefulTuples);
		
	}

	private QueryStatisticsCollector getQueryTable(Query query) {
		
		QueryStatisticsCollector qu = queriesTable.get(query.getId());
		
		if (qu == null){
			
			qu = new QueryStatisticsCollector(query);
			
			queriesTable.put(query.getId(), qu);
			
		}
		
		return qu;
	
	}

	private void saveDocumentInEvaluation(Query query, Evaluation eval,
			Document document, int usefulTuples) {
		
		
		
		getEvaluationStatistics(eval).setProcessedDocument(query, eval, document, usefulTuples);
		
	}

	private EvaluationStatisticsCollector getEvaluationStatistics(Evaluation eval) {
		EvaluationStatisticsCollector ev = evaluationTable.get(eval.getId());
		
		if (ev == null){
			
			ev = new EvaluationStatisticsCollector(eval);
			
			evaluationTable.put(eval.getId(), ev);
			
		}
		return ev;
	}

	private void saveDocumentInDatabase(Query query, Evaluation eval,
			Document document, int usefulTuples, Database database) {
		
		getDatabaseStatistics(database).setProcessedDocument(query, eval, document, usefulTuples);
		
	}

	private DatabaseStatisticsCollector getDatabaseStatistics(Database database) {

		DatabaseStatisticsCollector db = databasesTable.get(database.getId());
		
		if (db == null){
			
			db = new DatabaseStatisticsCollector(database);
			
			databasesTable.put(database.getId(), db);
			
		}
		
		return db;
	}

	@Override
	public void setAsProcessed(Evaluation eval, Query query) {
		
		getEvaluationStatistics(eval).setAsProcessed(query);

	}

	@Override
	public void setAsProcessed(Evaluation eval) {
		
		getEvaluationStatistics(eval).setAsProcessed(eval);
		
	}

	@Override
	public StatisticsCollector filter(Query query) {
		return getQueryTable(query);
	}

	@Override
	public StatisticsCollector filter(Database database) {
		return getDatabaseStatistics(database);
	}

	@Override
	public void setAsProcessed(Query query) {
		
		getQueryTable(query).setAsProcessed(query);
		
	}

	@Override
	public void clean() {
		
		for(Enumeration<DatabaseStatisticsCollector> e = databasesTable.elements();e.hasMoreElements();){
			e.nextElement().clean();
		}
		
		for(Enumeration<EvaluationStatisticsCollector> e = evaluationTable.elements();e.hasMoreElements();){
			e.nextElement().clean();
		}
		
		for(Enumeration<QueryStatisticsCollector> e = queriesTable.elements();e.hasMoreElements();){
			e.nextElement().clean();
		}
		
	}

}
