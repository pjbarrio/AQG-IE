package execution.model.statistics;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Evaluation;
import exploration.model.Query;

public class EvaluationStatisticsCollector implements StatisticsCollector {

	private Hashtable<String, QueryStatisticsCollector> queriesTable;
	private Evaluation eval;

	public EvaluationStatisticsCollector(Evaluation eval){
		this.eval = eval;
		queriesTable = new Hashtable<String, QueryStatisticsCollector>();		
	}
	
	private QueryStatisticsCollector getQueryStatistics(Query query) {
		
		QueryStatisticsCollector ret = queriesTable.get(query.getId());
		
		if (ret == null){
			
			ret = new QueryStatisticsCollector(query);
			
			queriesTable.put(query.getId(), ret);
			
		}
		
		return ret;
	}

	@Override
	public double getProcessedQueries() {
		double ret = 0;
		
		for (Enumeration<QueryStatisticsCollector> e = queriesTable.elements(); e.hasMoreElements();){
			ret += e.nextElement().getProcessedQueries();
		}
		
		return ret;
	}

	@Override
	public List<Query> getRunningQueries() {
		ArrayList<Query> ret = new ArrayList<Query>();
		
		for (Enumeration<QueryStatisticsCollector> e = queriesTable.elements(); e.hasMoreElements();){
			ret.addAll(e.nextElement().getRunningQueries());
		}
		
		return ret;
	}

	@Override
	public double getNumberOfWords() {
		double ret = 0;
		
		for (Enumeration<QueryStatisticsCollector> e = queriesTable.elements(); e.hasMoreElements();){
			ret += e.nextElement().getNumberOfWords();
		}
		
		return ret;
	}

	@Override
	public double getUsefulDocuments() {
		double ret = 0;
		
		for (Enumeration<QueryStatisticsCollector> e = queriesTable.elements(); e.hasMoreElements();){
			ret += e.nextElement().getUsefulDocuments();
		}
		
		return ret;
	}

	@Override
	public double getProcessedDocuments() {
		double ret = 0;
		
		for (Enumeration<QueryStatisticsCollector> e = queriesTable.elements(); e.hasMoreElements();){
			ret += e.nextElement().getProcessedDocuments();
		}
		
		return ret;
	}

	@Override
	public double getUselessDocuments() {
		
		double ret = 0;
		
		for (Enumeration<QueryStatisticsCollector> e = queriesTable.elements(); e.hasMoreElements();){
			ret += e.nextElement().getUselessDocuments();
		}
		
		return ret;
	}

	@Override
	public void notifyUpdated() {
		
		for (Enumeration<QueryStatisticsCollector> e = queriesTable.elements(); e.hasMoreElements();){
			e.nextElement().notifyUpdated();
		}
		
	}

	@Override
	public StatisticsCollector filter(Evaluation evaluation) {

		if (eval.equals(evaluation)){
			return this;
		}
		
		return new EmptyStatisticsCollector();
	}

	@Override
	public void setProcessedDocument(Query query, Evaluation eval,
			Document document, int usefulTuples) {
		
		if (this.eval.equals(eval)){
			getQueryStatistics(query).setProcessedDocument(query, eval, document, usefulTuples);
		}
		
	}

	@Override
	public void setAsProcessed(Evaluation eval, Query query) {
		
		if (this.eval.equals(eval)){
			getQueryStatistics(query).setAsProcessed(query);
		}
		
	}

	@Override
	public void setAsProcessed(Evaluation eval) {
		
		if (this.eval.equals(eval)){
			for (Enumeration<QueryStatisticsCollector> e = queriesTable.elements(); e.hasMoreElements();){
				e.nextElement().setAsProcessed(eval);
			}
		}
		
	}

	@Override
	public StatisticsCollector filter(Database database) {
		return new EmptyStatisticsCollector();
	}

	@Override
	public StatisticsCollector filter(Query query) {
		return getQueryStatistics(query);
	}

	@Override
	public void setAsProcessed(Query query) {
		
		getQueryStatistics(query).setAsProcessed(query);
		
	}

	@Override
	public void clean() {
		
		for (Enumeration<QueryStatisticsCollector> e = queriesTable.elements(); e.hasMoreElements();){
			e.nextElement().clean();
		}
		
	}

}
