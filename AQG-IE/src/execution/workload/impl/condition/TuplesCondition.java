package execution.workload.impl.condition;

import execution.workload.tuple.Tuple;


public class TuplesCondition extends UsefulCondition {

	@Override
	public boolean isItUseful(Tuple[] currTuples) {
		return ((currTuples != null) && (currTuples.length > 0));
	}

	@Override
	public boolean matchCondition(Tuple tuple) {
		return (tuple!=null);
	}

	@Override
	public boolean isItUseless(Tuple[] currTuples) {
		return (currTuples==null || currTuples.length == 0);
	}

}
