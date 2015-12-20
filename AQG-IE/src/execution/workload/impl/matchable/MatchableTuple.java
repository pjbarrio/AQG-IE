package execution.workload.impl.matchable;

import java.util.Hashtable;

import execution.workload.tuple.Tuple;



public class MatchableTuple extends Tuple {

	Hashtable<String, Matchable> ht;
	
	public MatchableTuple() {
		super();
		ht = new Hashtable<String, Matchable>();
	}

	  public void setTupleField(String name, Matchable value) {
		    
//		  if (ht.containsKey(name))
//			  name = name + "2";
		
		  super.setTupleField(name, value.toString());
		  
		  ht.put(name, value);
	  
	  }  

	  public Matchable getMatchableFieldValue(String field) {
	    return ht.get(field);
	  }
	
}
