package execution.model.documentCollector;

import java.util.List;

import execution.model.updateStrategy.UpdateStrategy;
import exploration.model.Evaluation;
import exploration.model.Query;

public interface DocumentCollector {

	public void addDocument(String document, Query query, Evaluation eval, boolean useful);

	public void update(UpdateStrategy updateStrategy);

	public void setInitialSample(Evaluation eval, List<String> documents);
	
	public List<String> getInitialSample(Evaluation eval);
	
	public DocumentCollector filter(Evaluation eval);
	
	public DocumentCollector filter(Query query);

	public void saveIteration();

	public void clear();

	public void clean();

}
