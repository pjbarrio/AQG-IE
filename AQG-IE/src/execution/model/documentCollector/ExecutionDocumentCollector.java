package execution.model.documentCollector;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import execution.model.updateStrategy.UpdateStrategy;
import exploration.model.Evaluation;
import exploration.model.Query;

public class ExecutionDocumentCollector implements DocumentCollector {

	private Hashtable<String, EvaluationDocumentCollector> evaluationDocumentsCollector;
	private Hashtable<String, QueryDocumentCollector> queryDocumentsCollector;

	private ArrayList<Hashtable<String, EvaluationDocumentCollector>> evaluations = new ArrayList<Hashtable<String,EvaluationDocumentCollector>>();
	private ArrayList<Hashtable<String, QueryDocumentCollector>> queries = new ArrayList<Hashtable<String,QueryDocumentCollector>>();
		
	public ExecutionDocumentCollector(){
		evaluationDocumentsCollector = new Hashtable<String,EvaluationDocumentCollector>();
		queryDocumentsCollector = new Hashtable<String, QueryDocumentCollector>();
		evaluations = new ArrayList<Hashtable<String,EvaluationDocumentCollector>>();
		queries = new ArrayList<Hashtable<String,QueryDocumentCollector>>();
	}
	
	@Override
	public void addDocument(String document, Query query, Evaluation eval,
			boolean useful) {
		
		getEvaluationCollector(eval).addDocument(document,query,eval,useful);
		getQueryCollector(query).addDocument(document,query,eval,useful);
		
	}

	private QueryDocumentCollector getQueryCollector(Query query) {
		
		QueryDocumentCollector ret = queryDocumentsCollector.get(query.getId());
		
		if (ret == null){
			ret = new QueryDocumentCollector(query);
			queryDocumentsCollector.put(query.getId(), ret);
		}
		
		return ret;
	}

	private EvaluationDocumentCollector getEvaluationCollector(Evaluation eval) {
		EvaluationDocumentCollector ret = evaluationDocumentsCollector.get(eval.getId());
		
		if (ret == null){
			
			ret = new EvaluationDocumentCollector(eval);
			
			evaluationDocumentsCollector.put(eval.getId(), ret);
			
		}
		
		return ret;
	}

	public void update(UpdateStrategy updateStrategy, Evaluation eval) {
		
		getEvaluationCollector(eval).update(updateStrategy);
		
	}

	@Override
	public void setInitialSample(Evaluation eval, List<String> documents) {
		
		getEvaluationCollector(eval).setInitialSample(eval, documents);
		
	}

	@Override
	public List<String> getInitialSample(Evaluation eval) {
		return getEvaluationCollector(eval).getInitialSample(eval);
	}

	@Override
	public DocumentCollector filter(Evaluation eval) {
		return getEvaluationCollector(eval);
	}

	@Override
	public DocumentCollector filter(Query query) {
		return getQueryCollector(query);
	}

	@Override
	public void saveIteration() {
		evaluations.add(evaluationDocumentsCollector);
		evaluationDocumentsCollector = new Hashtable<String, EvaluationDocumentCollector>();
		queries.add(queryDocumentsCollector);
		queryDocumentsCollector = new Hashtable<String, QueryDocumentCollector>();
		
	}

	@Override
	public void clear() {
		
		for (Enumeration<EvaluationDocumentCollector> e = evaluationDocumentsCollector.elements(); e.hasMoreElements();){
			e.nextElement().clear();
		}
		
		for (Enumeration<QueryDocumentCollector> e = queryDocumentsCollector.elements(); e.hasMoreElements();){
			e.nextElement().clear();
		}
	}

	@Override
	public void update(UpdateStrategy updateStrategy) {
		;
	}

	@Override
	public void clean() {
		
		for (Enumeration<EvaluationDocumentCollector> e = evaluationDocumentsCollector.elements(); e.hasMoreElements();){
			e.nextElement().clean();
		}
		
		for (Enumeration<QueryDocumentCollector> e = queryDocumentsCollector.elements(); e.hasMoreElements();){
			e.nextElement().clean();
		}
		
	}



}
