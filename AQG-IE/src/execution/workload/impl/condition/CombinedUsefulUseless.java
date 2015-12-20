package execution.workload.impl.condition;

import execution.workload.tuple.Tuple;

public class CombinedUsefulUseless extends UsefulCondition {

	private UsefulCondition useful;
	private UsefulCondition useless;

	public CombinedUsefulUseless(UsefulCondition useful,UsefulCondition useless){
		this.useful = useful;
		this.useless = useless;
	}
	
	@Override
	public boolean isItUseful(Tuple[] currTuples) {
		return useful.isItUseful(currTuples);
	}

	@Override
	public boolean isItUseless(Tuple[] currTuples) {
		return useless.isItUseless(currTuples);
	}

	@Override
	public boolean matchCondition(Tuple tuple) {
		return useful.matchCondition(tuple);
	}

}
