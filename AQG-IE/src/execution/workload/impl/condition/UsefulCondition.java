package execution.workload.impl.condition;

import execution.workload.tuple.Tuple;


public abstract class UsefulCondition {

	private Tuple[] tSimple;

	public abstract boolean isItUseful(Tuple[] currTuples);
	
	public boolean isItUseful(Tuple tuple){
		
		tSimple = new Tuple[1];
		
		tSimple[0] = tuple;
		
		return isItUseful(tSimple);
	
	}

	public abstract boolean isItUseless(Tuple[] currTuples);
	
	public boolean isItUseless(Tuple tuple){
		
		Tuple[] t = new Tuple[1];
		
		t[0] = tuple;
		
		return isItUseless(t);
	}
	
	public abstract boolean matchCondition(Tuple tuple);
}
