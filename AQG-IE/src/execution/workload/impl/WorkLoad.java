package execution.workload.impl;

import java.util.ArrayList;

import execution.workload.impl.matchable.MatchableTuple;
import execution.workload.tuple.Tuple;


public class WorkLoad {

	private ArrayList<MatchableTuple> tuples;
	
	private int index;

	public WorkLoad(ArrayList<MatchableTuple> tuples){
		this.tuples = tuples;
	}
	
	public boolean match(Tuple t){
		
		return WorkLoadMatcher.Match(this, t);
		
	}

	public void start() {
		
		index = 0;
		
	}

	public boolean hasNext() {
		return (index < tuples.size());
	}

	public MatchableTuple next() {
		index++;
		return tuples.get(index-1);
	}
	
}
