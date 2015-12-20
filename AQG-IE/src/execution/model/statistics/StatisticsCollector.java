package execution.model.statistics;

import java.util.List;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Evaluation;
import exploration.model.Query;

public interface StatisticsCollector {

	public void setProcessedDocument(Query query, Evaluation eval, Document document, int usefulTuples);

	public double getProcessedQueries();

	public List<Query> getRunningQueries();

	public double getNumberOfWords();

	public double getUsefulDocuments();

	public double getProcessedDocuments();

	public double getUselessDocuments();

	public void notifyUpdated();
	
	public StatisticsCollector filter(Database database);
	
	public StatisticsCollector filter(Evaluation evaluation);
	
	public StatisticsCollector filter(Query query);

	public void setAsProcessed(Evaluation eval, Query query);
	
	public void setAsProcessed(Evaluation eval);
	
	public void setAsProcessed(Query query);

	public void clean();
		
}
