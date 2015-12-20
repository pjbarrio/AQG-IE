package execution.model.documentCollector;

import java.util.ArrayList;
import java.util.List;

import execution.model.updateStrategy.UpdateStrategy;
import exploration.model.Evaluation;
import exploration.model.Query;

public class QueryDocumentCollector implements DocumentCollector {

	private Query query;
	private ArrayList<String> documents;
	private ArrayList<String> usefulDocuments;
	private ArrayList<String> uselessDocuments;

	private ArrayList<ArrayList<String>> historicDocuments;
	private ArrayList<ArrayList<String>> historicUsefulDocuments;
	private ArrayList<ArrayList<String>> historicUselessDocuments;
	
	public QueryDocumentCollector(Query query){
		
		documents = new ArrayList<String>();
		
		usefulDocuments = new ArrayList<String>();
		
		uselessDocuments = new ArrayList<String>();
				
		this.query = query;
		
		historicDocuments = new ArrayList<ArrayList<String>>();
		
		historicUsefulDocuments = new ArrayList<ArrayList<String>>();
		
		historicUselessDocuments = new ArrayList<ArrayList<String>>();
		
	}
	
	@Override
	public void addDocument(String document, Query query, Evaluation eval,
			boolean useful) {
		
		if (this.query.equals(query)){
			documents.add(document);
			if (useful){
				usefulDocuments.add(document);
			}else{
				uselessDocuments.add(document);
			}
		}

	}

	@Override
	public void update(UpdateStrategy updateStrategy) {
		
		updateStrategy.update(this);

	}

	@Override
	public void setInitialSample(Evaluation eval, List<String> documents) {
		; //Don't care about initial sample at this level
	}

	@Override
	public DocumentCollector filter(Evaluation eval) {
		return new EmptyDocumentCollector();
	}

	@Override
	public List<String> getInitialSample(Evaluation eval) {
		return new ArrayList<String>(0);
	}

	@Override
	public DocumentCollector filter(Query query) {
		if (this.query.equals(query)){
			return this;
		}
		return new EmptyDocumentCollector();
	}

	@Override
	public void saveIteration() {
		historicDocuments.add(documents);
		documents = new ArrayList<String>();
		historicUsefulDocuments.add(usefulDocuments);
		usefulDocuments = new ArrayList<String>();
		historicUselessDocuments.add(uselessDocuments);
		uselessDocuments = new ArrayList<String>();
	}

	@Override
	public void clear() {
		documents.clear();
		usefulDocuments.clear();
		uselessDocuments.clear();
	}

	@Override
	public void clean() {
		
		documents.clear();
		
		documents = new ArrayList<String>(0);
		
		usefulDocuments.clear();
		
		usefulDocuments = new ArrayList<String>(0);
		
		uselessDocuments.clear();
		
		uselessDocuments = new ArrayList<String>(0);
				
		for(ArrayList<String> array : historicDocuments){
			array.clear();
		}
		
		historicDocuments = new ArrayList<ArrayList<String>>();
				
		historicUsefulDocuments = new ArrayList<ArrayList<String>>();
		
		for(ArrayList<String> array : historicUsefulDocuments){
			array.clear();
		}
		
		historicUselessDocuments = new ArrayList<ArrayList<String>>();
		
		for(ArrayList<String> array : historicUselessDocuments){
			array.clear();
		}
		
	}

}
