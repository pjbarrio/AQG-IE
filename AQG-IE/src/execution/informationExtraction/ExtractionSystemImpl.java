package execution.informationExtraction;

import java.util.ArrayList;

import execution.workload.tuple.Tuple;
import exploration.model.Document;
import exploration.model.DocumentHandle;


public abstract class ExtractionSystemImpl implements ExtractionSystem {

	private String Id;


	public ExtractionSystemImpl(String Id) {

		this.Id = Id;
	}

	public String getId(){
		return Id;
	}
	
	public Tuple[] execute(Document[] handles) {
		
		
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		
		Tuple[] partial;
		
		for (int i = 0; i < handles.length; i++) {
			
			partial = execute(handles[i]);
			
			for (int j = 0; j < partial.length; j++) {
				
				tuples.add(partial[j]);
				
			}
			
		}
				
		return tuples.toArray(new Tuple[0]);
	}

	public abstract Tuple[] execute(Document handle);

	public void init() {
		;
	}

}
