package execution.model.documentCollector;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import execution.model.updateStrategy.UpdateStrategy;
import exploration.model.Evaluation;
import exploration.model.Query;

public class EvaluationDocumentCollector implements DocumentCollector {

	private Hashtable<String, QueryDocumentCollector> queryDocumentsTable;
	private Evaluation evaluation;
	private List<String> initialSample;
	
	private ArrayList<Hashtable<String, QueryDocumentCollector>> evaluations;

	public EvaluationDocumentCollector(Evaluation evaluation){
		this.evaluation = evaluation;
		queryDocumentsTable = new Hashtable<String, QueryDocumentCollector>();
		evaluations = new ArrayList<Hashtable<String,QueryDocumentCollector>>();
	}
	
	@Override
	public void addDocument(String document, Query query, Evaluation eval,
			boolean useful) {
		if (this.evaluation.equals(eval)){
			getQueryDocumentCollector(query).addDocument(document,query,eval,useful);
		}
		
	}

	private QueryDocumentCollector getQueryDocumentCollector(Query query) {
		
		QueryDocumentCollector ret = queryDocumentsTable.get(query.getId());
		
		if (ret == null){
			
			ret = new QueryDocumentCollector(query);
			
			queryDocumentsTable.put(query.getId(), ret);
			
		}
		
		return ret;
	}

	@Override
	public void update(UpdateStrategy updateStrategy) {
		
		for (Enumeration<QueryDocumentCollector> e = queryDocumentsTable.elements(); e.hasMoreElements();){
			e.nextElement().update(updateStrategy);
		}
		
	}

	@Override
	public void setInitialSample(Evaluation eval, List<String> documents) {
		
		if (evaluation.equals(eval)){
			initialSample = documents;
		}
	}

	@Override
	public DocumentCollector filter(Evaluation eval) {
		if (evaluation.equals(eval)){
			return this;
		}
		return new EmptyDocumentCollector();
	}

	@Override
	public DocumentCollector filter(Query query) {
		return getQueryDocumentCollector(query);
	}

	@Override
	public List<String> getInitialSample(Evaluation eval) {
		if (evaluation.equals(eval)){
			return initialSample;
		}
		return new ArrayList<String>();
	}

	@Override
	public void saveIteration() {
		evaluations.add(queryDocumentsTable);
		queryDocumentsTable = new Hashtable<String, QueryDocumentCollector>();
	}

	@Override
	public void clear() {
		for (Enumeration<QueryDocumentCollector> e = queryDocumentsTable.elements(); e.hasMoreElements();) {
			e.nextElement().clear();
		}
		queryDocumentsTable.clear();
	}

	@Override
	public void clean() {
		for (Enumeration<QueryDocumentCollector> e = queryDocumentsTable.elements(); e.hasMoreElements();){
			e.nextElement().clean();
		}
		
	}


}
