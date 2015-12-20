package execution.model.policy;

import java.util.HashSet;

import execution.model.scheduler.Schedulable;

public class LimitedNumberPolicy implements ExecutablePolicy {

	private long limit;
	private HashSet<Schedulable> contacted;

	public LimitedNumberPolicy(long limit) {
		this.limit = limit;
		contacted = new HashSet<Schedulable>();
	}

	@Override
	public boolean accepts(Schedulable o) {
		if (contacted.size() == limit && !contacted.contains(o))
			return false;
		return true;
	}

	@Override
	public void addNew(Schedulable o){
		
		contacted.add(o);
		
	}

	@Override
	public boolean willNeverBeAccepted(Schedulable o) {
		return !accepts(o);
	}

	//Checks the number of databases that are to be contacted. 
	
}
