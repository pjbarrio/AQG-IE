package execution.model.documentCollector;

import java.util.ArrayList;
import java.util.List;

import execution.model.updateStrategy.UpdateStrategy;
import exploration.model.Evaluation;
import exploration.model.Query;

public class EmptyDocumentCollector implements DocumentCollector {

	@Override
	public void addDocument(String document, Query query, Evaluation eval,
			boolean useful) {
		;

	}

	@Override
	public void update(UpdateStrategy updateStrategy) {
		;
	}

	@Override
	public void setInitialSample(Evaluation eval, List<String> documents) {
		;
	}

	@Override
	public DocumentCollector filter(Evaluation eval) {
		return this;
	}

	@Override
	public List<String> getInitialSample(Evaluation eval) {
		return new ArrayList<String>();
	}

	@Override
	public DocumentCollector filter(Query query) {
		return this;
	}

	@Override
	public void saveIteration() {
		;
	}

	@Override
	public void clear() {
		;
	}

	@Override
	public void clean() {
		;
		
	}

}
